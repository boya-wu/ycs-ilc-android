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
import com.yuchens.ilcandroid.databinding.FragmentBottomNavBinding
import com.yuchens.ilcandroid.ui.viewmodel.AppViewModel
import com.yuchens.ilcandroid.ui.viewmodel.NavViewModel
import kotlinx.coroutines.launch

class BottomNavFragment : Fragment() {

    private var _binding: FragmentBottomNavBinding? = null
    private val binding get() = _binding!!
    private val navVm: NavViewModel by activityViewModels()
    private val appVm: AppViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBottomNavBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                appVm.role.collect { role -> configureForRole(role) }
            }
        }

        binding.btnTab1.setOnClickListener { navVm.select(tabForIndex(0)) }
        binding.btnTab2.setOnClickListener { navVm.select(tabForIndex(1)) }
        binding.btnTab3.setOnClickListener { navVm.select(tabForIndex(2)) }
        binding.btnTab4.setOnClickListener { navVm.select(tabForIndex(3)) }
        binding.btnTab5.setOnClickListener { navVm.select(tabForIndex(4)) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                navVm.currentTab.collect { tab -> highlight(tab) }
            }
        }
    }

    private var roleTabs: List<NavTab> = emptyList()

    private fun configureForRole(role: UserRole?) {
        roleTabs = when (role) {
            UserRole.STAFF -> listOf(NavTab.HOME, NavTab.TRANSFER, NavTab.ALARM, NavTab.SHIFT, NavTab.SETTING)
            UserRole.DRIVER -> listOf(NavTab.HOME, NavTab.DISPATCH, NavTab.ENTER, NavTab.RECORDS)
            UserRole.VENDOR -> listOf(NavTab.HOME, NavTab.EXIT, NavTab.HISTORY)
            null -> emptyList()
        }
        val labels = when (role) {
            UserRole.STAFF -> listOf(R.string.tab_home, R.string.tab_transfer, R.string.tab_alarm, R.string.tab_shift, R.string.tab_setting)
            UserRole.DRIVER -> listOf(R.string.tab_home, R.string.tab_dispatch, R.string.tab_enter, R.string.tab_records)
            UserRole.VENDOR -> listOf(R.string.tab_home, R.string.tab_exit, R.string.tab_history)
            null -> emptyList()
        }
        val buttons = listOf(binding.btnTab1, binding.btnTab2, binding.btnTab3, binding.btnTab4, binding.btnTab5)
        buttons.forEachIndexed { i, btn ->
            if (i < roleTabs.size) {
                btn.visibility = View.VISIBLE
                btn.text = getString(labels[i])
            } else {
                btn.visibility = View.GONE
            }
        }
    }

    private fun tabForIndex(index: Int): NavTab = roleTabs.getOrElse(index) { NavTab.HOME }

    private fun highlight(tab: NavTab) {
        val buttons = listOf(binding.btnTab1, binding.btnTab2, binding.btnTab3, binding.btnTab4, binding.btnTab5)
        buttons.forEachIndexed { i, btn ->
            btn.isSelected = roleTabs.getOrNull(i) == tab
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
