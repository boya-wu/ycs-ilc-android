package com.yuchens.equipinspectandroid.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.yuchens.equipinspectandroid.R
import com.yuchens.equipinspectandroid.databinding.FragmentSettingBinding
import com.yuchens.equipinspectandroid.ui.base.BaseFragment
import com.yuchens.equipinspectandroid.ui.viewmodel.SettingViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingFragment : BaseFragment() {

    private val vm: SettingViewModel by viewModels()

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 初始帶入目前設定
        viewLifecycleOwner.lifecycleScope.launch {
            val currentUrl = vm.currentBaseUrl()
            val currentSec = vm.currentLimitWindowSeconds()
            binding.etApiUrl.setText(currentUrl)
            binding.etLimitWindowSec.setText(currentSec.toString())
        }

        binding.btnSave.setOnClickListener {
            val urlRaw = binding.etApiUrl.text?.toString().orEmpty()
            val secRaw = binding.etLimitWindowSec.text?.toString().orEmpty()
            binding.etApiUrl.error = null
            binding.etLimitWindowSec.error = null

            viewLifecycleOwner.lifecycleScope.launch {
                // 先驗證/儲存 URL
                val urlOk = vm.setBaseUrlNormalized(urlRaw)
                if (!urlOk) {
                    binding.etApiUrl.error = "儲存失敗：URL 無效"
                    Toast.makeText(requireContext(), "儲存失敗：URL 無效", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // 再儲存間隔秒數
                val secOk = vm.setLimitWindowSeconds(secRaw)
                if (!secOk) {
                    binding.etLimitWindowSec.error = "請輸入整數秒數"
                    Toast.makeText(requireContext(), "儲存失敗：請輸入整數秒數", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // 測試 API（你原本流程）
                vm.ping()
            }
        }

        // 觀察狀態，更新按鈕可用性/文字
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.state.collect { st ->
                    when (st) {
                        is SettingViewModel.UiState.Idle -> {
                            binding.btnSave.isEnabled = true
                            binding.btnSave.text = getString(R.string.desc_setting_test_save)
                        }
                        is SettingViewModel.UiState.Loading -> {
                            binding.btnSave.isEnabled = false
                            binding.btnSave.text = getString(R.string.desc_setting_testing)
                        }
                        is SettingViewModel.UiState.Success -> {
                            val current = vm.currentBaseUrl()
                            binding.etApiUrl.setText(current)
                            Toast.makeText(requireContext(), "測試連線成功，設定已生效", Toast.LENGTH_SHORT).show()
                            vm.reset()
                        }
                        is SettingViewModel.UiState.Error -> {
                            Toast.makeText(requireContext(), "測試失敗：${st.message}", Toast.LENGTH_SHORT).show()
                            vm.reset()
                        }
                    }
                }
            }
        }
    }
}
