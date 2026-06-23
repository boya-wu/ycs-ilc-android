package com.yuchens.equipinspectandroid.ui.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.yuchens.equipinspectandroid.R
import com.yuchens.equipinspectandroid.databinding.FragmentUploadBinding
import com.yuchens.equipinspectandroid.ui.base.BaseFragment
import com.yuchens.equipinspectandroid.ui.viewmodel.UploadViewModel
import com.yuchens.equipinspectandroid.ui.widget.WifiReading
import com.yuchens.equipinspectandroid.util.LogHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UploadFragment : BaseFragment() {

    private var _binding: FragmentUploadBinding? = null
    private val binding get() = _binding!!

    private val vm: UploadViewModel by viewModels()

    private var isUploading = false
    private var lastReading: WifiReading? = null

    private val permissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            // 任一必要權限通過就啟動監聽
            val granted = results.values.any { it }
            if (granted) {
                _binding?.wifiSignal?.startListening()
            } else {
                Toast.makeText(requireContext(), getString(R.string.desc_wifi_perm_denied), Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestWifiPermissionsIfNeeded()

        // 監聽 Wi-Fi 訊號
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                binding.wifiSignal.reading.collect { reading ->
                    lastReading = reading
                    renderWifi(reading.level, reading.rssi)
                }
            }
        }

        // 監聽上傳狀態
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.state.collect { st ->
                    when (st) {
                        is UploadViewModel.UiState.Idle -> {
                            isUploading = false
                            restoreButtonByWifi()
                        }
                        is UploadViewModel.UiState.Loading -> {
                            isUploading = true
                            setButtonLoading(true)
                        }
                        is UploadViewModel.UiState.Success -> {
                            Toast.makeText(requireContext(), getString(R.string.desc_upload_success), Toast.LENGTH_SHORT).show()
                            LogHelper.write(requireContext(), "Upload success")
                            setButtonLoading(false)
                            vm.reset()
                        }
                        is UploadViewModel.UiState.Error -> {
                            val msg = st.message
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.desc_upload_failed_fmt, msg),
                                Toast.LENGTH_LONG
                            ).show()
                            LogHelper.write(requireContext(), "Upload failed: $msg")
                            setButtonLoading(false)
                            vm.reset()
                        }
                    }
                }
            }
        }

        // 待上傳筆數
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.uploadCount.collect { count ->
                    binding.txtRecordQty.text =
                        getString(R.string.desc_upload_record_qty_fmt, count)
                }
            }
        }

        binding.btnUpload.setOnClickListener {
            if (!isUploading) vm.upload()
        }
    }

    override fun onStart() {
        super.onStart()
        // 權限允許才開始監聽
        if (hasWifiPermission()) {
            binding.wifiSignal.startListening(1_000L)
        }
    }

    override fun onStop() {
        super.onStop()
        _binding?.wifiSignal?.stopListening()
    }

    private fun renderWifi(level: Int, rssi: Int?) {
        val connected = rssi != null
        binding.txtStatus.text =
            if (connected) getString(R.string.desc_download_connected)
            else getString(R.string.desc_download_disconnected)

        binding.txtRSSI.text =
            if (connected) getString(R.string.desc_wifi_level_text, levelToText(level), rssi)
            else getString(R.string.desc_wifi_dash)

        if (!isUploading) {
            val enabled = connected && level >= 2
            binding.btnUpload.isEnabled = enabled
            binding.btnUpload.alpha = if (enabled) 1f else 0.5f
        }
    }

    private fun restoreButtonByWifi() {
        val reading = lastReading
        val connected = reading?.rssi != null
        val enabled = connected && reading.level >= 2
        binding.btnUpload.isEnabled = enabled
        binding.btnUpload.alpha = if (enabled) 1f else 0.5f
    }

    private fun setButtonLoading(loading: Boolean) {
        binding.btnUpload.isEnabled = !loading
        binding.btnUpload.alpha = if (loading) 0.5f else 1f
        binding.btnUpload.text =
            if (loading) getString(R.string.desc_upload_uploading)
            else getString(R.string.desc_upload)
    }

    private fun levelToText(level: Int): String = when (level) {
        0 -> getString(R.string.desc_wifi_level_0)
        1 -> getString(R.string.desc_wifi_level_1)
        2 -> getString(R.string.desc_wifi_level_2)
        3 -> getString(R.string.desc_wifi_level_3)
        else -> getString(R.string.desc_wifi_level_4)
    }

    private fun hasWifiPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.NEARBY_WIFI_DEVICES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestWifiPermissionsIfNeeded() {
        val need = mutableListOf<String>()
        fun lack(p: String) =
            ContextCompat.checkSelfPermission(requireContext(), p) != PackageManager.PERMISSION_GRANTED

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (lack(Manifest.permission.NEARBY_WIFI_DEVICES)) need += Manifest.permission.NEARBY_WIFI_DEVICES
        } else {
            if (lack(Manifest.permission.ACCESS_FINE_LOCATION)) need += Manifest.permission.ACCESS_FINE_LOCATION
            if (lack(Manifest.permission.ACCESS_COARSE_LOCATION)) need += Manifest.permission.ACCESS_COARSE_LOCATION
        }
        if (need.isNotEmpty()) {
            permissionsLauncher.launch(need.toTypedArray())
        } else {
            // 既有權限就直接開始
            binding.wifiSignal.startListening()
        }
    }
}
