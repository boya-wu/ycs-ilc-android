package com.yuchens.equipinspectandroid.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.yuchens.equipinspectandroid.databinding.FragmentTopNavBinding
import com.yuchens.equipinspectandroid.ui.LoginActivity
import com.yuchens.equipinspectandroid.ui.MainActivity
import com.yuchens.equipinspectandroid.util.ScanRateLimiter
import com.yuchens.equipinspectandroid.util.UserPrefs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TopNavFragment : Fragment() {

    @Inject lateinit var userPrefs: UserPrefs
    @Inject lateinit var limiter: ScanRateLimiter

    private var _binding: FragmentTopNavBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTopNavBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 登出
        binding.btnLogout.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                //limiter.clear()
                userPrefs.clear()

                // 回登入頁並清掉返回棧
                val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                startActivity(intent)
                requireActivity().finish()
            }
        }

        // 點左上角 Menu 開啟 Drawer
        binding.btnMenu.setOnClickListener {
            (activity as? MainActivity)?.openDrawer()
        }
    }
}
