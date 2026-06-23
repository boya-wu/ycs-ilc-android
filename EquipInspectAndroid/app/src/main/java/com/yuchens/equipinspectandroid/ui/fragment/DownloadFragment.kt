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
import com.yuchens.equipinspectandroid.databinding.FragmentDownloadBinding
import com.yuchens.equipinspectandroid.ui.base.BaseFragment
import com.yuchens.equipinspectandroid.ui.viewmodel.DownloadViewModel
import com.yuchens.equipinspectandroid.ui.widget.WifiReading
import com.yuchens.equipinspectandroid.util.LogHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DownloadFragment : BaseFragment() {

    private var _binding: FragmentDownloadBinding? = null
    private val binding get() = _binding!!

    private val vm: DownloadViewModel by viewModels()

    private var isDownloading = false
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
        _binding = FragmentDownloadBinding.inflate(inflater, container, false)
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

        // 監聽下載狀態
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.state.collect { st ->
                    when (st) {
                        is DownloadViewModel.UiState.Idle -> {
                            isDownloading = false
                            restoreButtonByWifi()
                        }
                        is DownloadViewModel.UiState.Loading -> {
                            isDownloading = true
                            setButtonLoading(true)
                        }
                        is DownloadViewModel.UiState.Success -> {
                            Toast.makeText(requireContext(), getString(R.string.desc_download_success), Toast.LENGTH_SHORT).show()
                            LogHelper.write(requireContext(), "Download success")
                            setButtonLoading(false)
                            vm.reset()
                        }
                        is DownloadViewModel.UiState.Error -> {
                            val msg = st.message
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.desc_download_failed_fmt, msg),
                                Toast.LENGTH_LONG
                            ).show()
                            LogHelper.write(requireContext(), "Download failed: $msg")
                            setButtonLoading(false)
                            vm.reset()
                        }
                    }
                }
            }
        }

        // 未上傳筆數
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.needUploadCount.collect { count ->
                    binding.txtNeedUpload.text =
                        getString(R.string.desc_download_need_upload_fmt, count)
                }
            }
        }

        binding.btnDownload.setOnClickListener {
            if (!isDownloading) vm.download()
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
        // View 仍存在時再停止監聽
        _binding?.wifiSignal?.stopListening()
    }

    // --- UI helpers ---

    private fun renderWifi(level: Int, rssi: Int?) {
        val connected = rssi != null
        binding.txtStatus.text =
            if (connected) getString(R.string.desc_download_connected)
            else getString(R.string.desc_download_disconnected)

        binding.txtRSSI.text =
            if (connected) getString(R.string.desc_wifi_level_text, levelToText(level), rssi)
            else getString(R.string.desc_wifi_dash)

        if (!isDownloading) {
            val enabled = connected && level >= 2
            binding.btnDownload.isEnabled = enabled
            binding.btnDownload.alpha = if (enabled) 1f else 0.5f
        }
    }

    private fun restoreButtonByWifi() {
        val reading = lastReading
        val connected = reading?.rssi != null
        val enabled = connected && reading.level >= 2
        binding.btnDownload.isEnabled = enabled
        binding.btnDownload.alpha = if (enabled) 1f else 0.5f
    }

    private fun setButtonLoading(loading: Boolean) {
        binding.btnDownload.isEnabled = !loading
        binding.btnDownload.alpha = if (loading) 0.5f else 1f
        binding.btnDownload.text =
            if (loading) getString(R.string.desc_download_downloading)
            else getString(R.string.desc_download)
    }

    private fun levelToText(level: Int): String = when (level) {
        0 -> getString(R.string.desc_wifi_level_0)
        1 -> getString(R.string.desc_wifi_level_1)
        2 -> getString(R.string.desc_wifi_level_2)
        3 -> getString(R.string.desc_wifi_level_3)
        else -> getString(R.string.desc_wifi_level_4)
    }

    // --- Permissions ---

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
        fun lack(p: String) = ContextCompat.checkSelfPermission(requireContext(), p) != PackageManager.PERMISSION_GRANTED

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (lack(Manifest.permission.NEARBY_WIFI_DEVICES)) {
                need += Manifest.permission.NEARBY_WIFI_DEVICES
            }
        } else {
            if (lack(Manifest.permission.ACCESS_FINE_LOCATION)) {
                need += Manifest.permission.ACCESS_FINE_LOCATION
            }
            if (lack(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                need += Manifest.permission.ACCESS_COARSE_LOCATION
            }
        }

        if (need.isNotEmpty()) {
            permissionsLauncher.launch(need.toTypedArray())
        } else {
            // 既有權限就直接開始
            binding.wifiSignal.startListening()
        }
    }
}
