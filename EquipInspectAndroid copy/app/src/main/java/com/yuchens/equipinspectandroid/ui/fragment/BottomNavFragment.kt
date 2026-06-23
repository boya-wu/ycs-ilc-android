package com.yuchens.equipinspectandroid.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.yuchens.equipinspectandroid.ui.viewmodel.NavViewModel
import com.yuchens.equipinspectandroid.databinding.FragmentBottomNavBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BottomNavFragment : Fragment() {

    private var _binding: FragmentBottomNavBinding? = null
    private val binding get() = _binding!!

    // 取得 Activity 作用域的 ViewModel（關鍵）
    private val vm: NavViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBottomNavBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 點擊只「發送選擇」
        binding.btnUninspected.setOnClickListener { vm.select(NavViewModel.Tab.UNINSPECTED) }
        binding.btnInspected  .setOnClickListener { vm.select(NavViewModel.Tab.INSPECTED) }
        binding.btnInspect    .setOnClickListener { vm.select(NavViewModel.Tab.INSPECT) }
        binding.btnDownload   .setOnClickListener { vm.select(NavViewModel.Tab.DOWNLOAD) }
        binding.btnUpload     .setOnClickListener { vm.select(NavViewModel.Tab.UPLOAD) }

        // 觀察目前 Tab，高亮選中
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.current.collect { tab -> highlight(tab) }
            }
        }
    }

    private fun highlight(tab: NavViewModel.Tab) {
        with(binding) {
            btnUninspected.isSelected = (tab == NavViewModel.Tab.UNINSPECTED)
            btnInspected  .isSelected = (tab == NavViewModel.Tab.INSPECTED)
            btnInspect    .isSelected = (tab == NavViewModel.Tab.INSPECT)
            btnDownload   .isSelected = (tab == NavViewModel.Tab.DOWNLOAD)
            btnUpload     .isSelected = (tab == NavViewModel.Tab.UPLOAD)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


