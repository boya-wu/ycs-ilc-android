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
import com.yuchens.ilcandroid.data.NavTab
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

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                appVm.role.collect { role ->
                    when (role) {
                        UserRole.STAFF -> showStaffDashboard()
                        UserRole.DRIVER -> showDriverDashboard()
                        UserRole.VENDOR -> showVendorDashboard()
                        null -> {}
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                appVm.userName.collect { binding.txtUserName.text = it }
            }
        }
    }

    private fun showStaffDashboard() {
        binding.txtDashboardTitle.text = getString(R.string.dashboard_staff_title)
        binding.txtInfoLine1.text = getString(R.string.dashboard_staff_shift, "日班 08:00–20:00")
        binding.txtInfoLine2.text = getString(R.string.dashboard_staff_status, "值班中 On Duty")
        binding.layoutQuickActions.removeAllViews()
        addQuickAction(getString(R.string.quick_dispatch)) { (activity as MainActivity).selectTab(NavTab.TRANSFER) }
        addQuickAction(getString(R.string.quick_enter)) { (activity as MainActivity).selectTab(NavTab.TRANSFER) }
        addQuickAction(getString(R.string.quick_exit)) { (activity as MainActivity).selectTab(NavTab.TRANSFER) }
        addQuickAction(getString(R.string.quick_punch)) { (activity as MainActivity).selectTab(NavTab.SHIFT) }
    }

    private fun showDriverDashboard() {
        binding.txtDashboardTitle.text = getString(R.string.dashboard_driver_title)
        binding.txtInfoLine1.text = getString(R.string.dashboard_driver_task, "Tank_B → F18A P1")
        binding.txtInfoLine2.text = getString(R.string.dashboard_driver_status, "待出工 Pending")
        binding.layoutQuickActions.removeAllViews()
        addQuickAction(getString(R.string.quick_start_dispatch)) { (activity as MainActivity).selectTab(NavTab.DISPATCH) }
        addQuickAction(getString(R.string.quick_report_enter)) { (activity as MainActivity).selectTab(NavTab.ENTER) }
    }

    private fun showVendorDashboard() {
        binding.txtDashboardTitle.text = getString(R.string.dashboard_vendor_title)
        binding.txtInfoLine1.text = getString(R.string.dashboard_vendor_company, "長春化工")
        binding.txtInfoLine2.text = getString(R.string.dashboard_vendor_tank, "Tank_A @ P-06")
        binding.layoutQuickActions.removeAllViews()
        addQuickAction(getString(R.string.quick_register_exit)) { (activity as MainActivity).selectTab(NavTab.EXIT) }
    }

    private fun addQuickAction(label: String, onClick: () -> Unit) {
        val btn = layoutInflater.inflate(R.layout.item_quick_action, binding.layoutQuickActions, false)
        btn.findViewById<android.widget.Button>(R.id.btnQuick).apply {
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
