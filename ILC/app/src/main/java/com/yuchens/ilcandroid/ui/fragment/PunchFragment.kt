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
import com.yuchens.ilcandroid.databinding.FragmentPunchBinding
import com.yuchens.ilcandroid.ui.MainActivity
import com.yuchens.ilcandroid.ui.viewmodel.AppViewModel
import com.yuchens.ilcandroid.ui.widget.VirtualKeyboardDialog
import kotlinx.coroutines.launch

class PunchFragment : Fragment(), VirtualKeyboardDialog.Listener {

    private var _binding: FragmentPunchBinding? = null
    private val binding get() = _binding!!
    private val appVm: AppViewModel by activityViewModels()
    private var isClockIn = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPunchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.txtPunchTitle.text = getString(R.string.punch_title)
        binding.btnBack.setOnClickListener { goBackToDashboard() }
        binding.btnHome.setOnClickListener { (activity as MainActivity).logout() }

        binding.btnClockIn.setOnClickListener {
            isClockIn = true
            showIdDialog()
        }
        binding.btnClockOut.setOnClickListener {
            isClockIn = false
            showIdDialog()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                appVm.punchRecords.collect {
                    val onDuty = appVm.onDutyStaff()
                    binding.txtOnDutyList.text = if (onDuty.isEmpty()) {
                        getString(R.string.punch_no_staff)
                    } else {
                        onDuty.joinToString("\n")
                    }
                }
            }
        }
    }

    private fun goBackToDashboard() {
        (activity as MainActivity).navigateTo(Screen.HOME)
    }

    private fun showIdDialog() {
        val title = if (isClockIn) getString(R.string.punch_clock_in) else getString(R.string.punch_clock_out)
        VirtualKeyboardDialog.newInstance(title, getString(R.string.input_card_hint))
            .show(childFragmentManager, "punch_id")
    }

    override fun onConfirmed(value: String) {
        if (isClockIn) appVm.punchIn(value) else appVm.punchOut(value)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
