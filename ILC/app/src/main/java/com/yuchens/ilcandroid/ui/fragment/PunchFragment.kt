package com.yuchens.ilcandroid.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.yuchens.ilcandroid.R
import com.yuchens.ilcandroid.data.MockData
import com.yuchens.ilcandroid.databinding.FragmentPunchBinding
import com.yuchens.ilcandroid.ui.viewmodel.AppViewModel
import com.yuchens.ilcandroid.ui.widget.VirtualKeyboardDialog
import kotlinx.coroutines.launch

class PunchFragment : Fragment(), VirtualKeyboardDialog.Listener {

    private var _binding: FragmentPunchBinding? = null
    private val binding get() = _binding!!
    private val appVm: AppViewModel by activityViewModels()
    private var punchMode = MODE_CLOCK_IN

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        punchMode = requireArguments().getString(ARG_MODE) ?: MODE_SHIFT
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPunchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.txtPunchTitle.text = getString(R.string.punch_title)

        binding.btnClockIn.setOnClickListener {
            punchMode = MODE_CLOCK_IN
            showIdDialog()
        }
        binding.btnClockOut.setOnClickListener {
            punchMode = MODE_CLOCK_OUT
            showIdDialog()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                appVm.punchRecords.collect { records ->
                    val onDuty = appVm.onDutyStaff()
                    binding.txtOnDutyList.text = if (onDuty.isEmpty()) {
                        getString(R.string.punch_no_staff)
                    } else {
                        getString(R.string.punch_on_duty) + "\n" + onDuty.joinToString("\n")
                    }
                    binding.txtRecentPunch.text = records.take(5).joinToString("\n") {
                        "${it.time} ${it.name} ${it.type}"
                    }
                }
            }
        }

        binding.txtShiftList.text = MockData.shifts.joinToString("\n\n") {
            "${it.date} ${it.shiftType}\n${it.assignedStaff.joinToString("、")}"
        }
    }

    private fun showIdDialog() {
        val title = if (punchMode == MODE_CLOCK_IN) getString(R.string.punch_clock_in) else getString(R.string.punch_clock_out)
        VirtualKeyboardDialog.newInstance(title, MockData.staffList.map { it.id })
            .show(childFragmentManager, "punch_id")
    }

    override fun onConfirmed(value: String) {
        if (punchMode == MODE_CLOCK_IN) appVm.punchIn(value) else appVm.punchOut(value)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val MODE_SHIFT = "shift"
        const val MODE_CLOCK_IN = "clock_in"
        const val MODE_CLOCK_OUT = "clock_out"
        private const val ARG_MODE = "arg_mode"
        fun newInstance(mode: String) = PunchFragment().apply {
            arguments = bundleOf(ARG_MODE to mode)
        }
    }
}
