package com.yuchens.ilcandroid.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.yuchens.ilcandroid.R
import com.yuchens.ilcandroid.data.Screen
import com.yuchens.ilcandroid.data.UserRole
import com.yuchens.ilcandroid.databinding.FragmentDashboardBinding
import com.yuchens.ilcandroid.ui.MainActivity
import com.yuchens.ilcandroid.ui.viewmodel.AppViewModel
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val appVm: AppViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener { (activity as MainActivity).logout() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                appVm.role.collect { role ->
                    when (role) {
                        UserRole.STAFF -> showStaffHome()
                        UserRole.DRIVER -> showDriverHome()
                        UserRole.VENDOR -> showVendorHome()
                        null -> {}
                    }
                }
            }
        }
    }

    private fun showStaffHome() {
        binding.txtDashboardTitle.text = getString(R.string.home_staff_title)
        binding.layoutQuickActions.removeAllViews()
        addQuickAction(getString(R.string.quick_punch)) {
            (activity as MainActivity).navigateTo(Screen.PUNCH)
        }
    }

    private fun showDriverHome() {
        binding.txtDashboardTitle.text = getString(R.string.home_driver_title)
        binding.layoutQuickActions.removeAllViews()
        addQuickAction(getString(R.string.quick_start_dispatch)) {
            (activity as MainActivity).navigateTo(Screen.DISPATCH)
        }
        addQuickAction(getString(R.string.quick_report_enter)) {
            (activity as MainActivity).navigateTo(Screen.ENTER)
        }
    }

    private fun showVendorHome() {
        binding.txtDashboardTitle.text = getString(R.string.home_vendor_title)
        binding.layoutQuickActions.removeAllViews()
        addQuickAction(getString(R.string.quick_register_exit)) {
            (activity as MainActivity).navigateTo(Screen.EXIT)
        }
    }

    private fun addQuickAction(label: String, onClick: () -> Unit) {
        val btn = layoutInflater.inflate(R.layout.item_quick_action, binding.layoutQuickActions, false)
        btn.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnQuick).apply {
            text = label
            setOnClickListener { onClick() }
        }
        binding.layoutQuickActions.addView(btn)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
