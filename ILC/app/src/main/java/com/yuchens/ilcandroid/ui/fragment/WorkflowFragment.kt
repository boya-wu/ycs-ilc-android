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
import com.yuchens.ilcandroid.data.WorkflowType
import com.yuchens.ilcandroid.databinding.FragmentWorkflowBinding
import com.yuchens.ilcandroid.ui.MainActivity
import com.yuchens.ilcandroid.ui.viewmodel.AppViewModel
import com.yuchens.ilcandroid.ui.widget.VirtualKeyboardDialog
import kotlinx.coroutines.launch

class WorkflowFragment : Fragment(), VirtualKeyboardDialog.Listener {

    private var _binding: FragmentWorkflowBinding? = null
    private val binding get() = _binding!!
    private val appVm: AppViewModel by activityViewModels()

    private lateinit var workflowType: WorkflowType
    private var pendingStep = 1
    private var showConfirm = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        workflowType = WorkflowType.valueOf(requireArguments().getString(ARG_TYPE)!!)
        appVm.startWorkflow(workflowType)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWorkflowBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.txtWorkflowTitle.text = when (workflowType) {
            WorkflowType.DISPATCH -> getString(R.string.workflow_dispatch_title)
            WorkflowType.ENTER -> getString(R.string.workflow_enter_title)
            WorkflowType.EXIT -> getString(R.string.workflow_exit_title)
        }

        val stepLabels = when (workflowType) {
            WorkflowType.DISPATCH -> getString(R.string.workflow_steps_dispatch)
            WorkflowType.ENTER -> getString(R.string.workflow_steps_enter)
            WorkflowType.EXIT -> getString(R.string.workflow_steps_exit)
        }
        binding.txtStepIndicator.text = stepLabels

        binding.rowStep1.setOnClickListener { openInput(1) }
        binding.rowStep2.setOnClickListener { if (appVm.wfStep1.value != null) openInput(2) }
        binding.rowStep3.setOnClickListener { if (appVm.wfStep2.value != null) openInput(3) }
        binding.rowStep4.setOnClickListener {
            if (workflowType != WorkflowType.EXIT && appVm.wfStep3.value != null) openInput(4)
        }

        binding.btnSubmit.setOnClickListener {
            appVm.submitWorkflow()
            (activity as MainActivity).selectTab(com.yuchens.ilcandroid.data.NavTab.HOME)
        }
        binding.btnReset.setOnClickListener {
            appVm.startWorkflow(workflowType)
            showConfirm = false
            binding.layoutConfirm.visibility = View.GONE
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    appVm.wfStep1.collect { updateSteps() }
                }
                launch {
                    appVm.wfStep2.collect { updateSteps() }
                }
                launch {
                    appVm.wfStep3.collect { updateSteps() }
                }
                launch {
                    appVm.wfStep4.collect { updateSteps() }
                }
            }
        }
    }

    private fun updateSteps() {
        val s1 = appVm.wfStep1.value
        val s2 = appVm.wfStep2.value
        val s3 = appVm.wfStep3.value
        val s4 = appVm.wfStep4.value

        binding.txtStep1Value.text = s1?.let { MockData.resolveStaffLabel(it) } ?: getString(R.string.tap_to_input)
        binding.txtStep2Value.text = when (workflowType) {
            WorkflowType.EXIT -> s2?.let { MockData.resolveVendorLabel(it) }
            else -> s2?.let { MockData.resolveDriverLabel(it) }
        } ?: getString(R.string.tap_to_input)
        binding.txtStep3Value.text = s3?.let { MockData.resolveTankLabel(it) } ?: getString(R.string.tap_to_input)
        binding.txtStep4Value.text = when (workflowType) {
            WorkflowType.DISPATCH -> s4?.let { MockData.resolveFabLabel(it) }
            WorkflowType.ENTER -> s4?.let { MockData.resolveSpotLabel(it) }
            WorkflowType.EXIT -> "-"
        } ?: getString(R.string.tap_to_input)

        binding.rowStep4.visibility = if (workflowType == WorkflowType.EXIT) View.GONE else View.VISIBLE
        binding.txtStep2Label.text = if (workflowType == WorkflowType.EXIT) {
            getString(R.string.step_vendor)
        } else {
            getString(R.string.step_driver)
        }
        binding.txtStep4Label.text = when (workflowType) {
            WorkflowType.DISPATCH -> getString(R.string.step_fab)
            WorkflowType.ENTER -> getString(R.string.step_spot)
            WorkflowType.EXIT -> ""
        }

        val complete = if (workflowType == WorkflowType.EXIT) {
            s1 != null && s2 != null && s3 != null
        } else {
            s1 != null && s2 != null && s3 != null && s4 != null
        }
        binding.layoutConfirm.visibility = if (complete) View.VISIBLE else View.GONE
        if (complete) {
            binding.txtSummary.text = buildSummary(s1, s2, s3, s4)
        }
    }

    private fun buildSummary(s1: String?, s2: String?, s3: String?, s4: String?): String {
        return buildString {
            append("值班 Staff: ${MockData.resolveStaffLabel(s1 ?: "")}\n")
            append(when (workflowType) {
                WorkflowType.EXIT -> "廠商 Vendor: ${MockData.resolveVendorLabel(s2 ?: "")}\n"
                else -> "司機 Driver: ${MockData.resolveDriverLabel(s2 ?: "")}\n"
            })
            append("槽體 Tank: ${MockData.resolveTankLabel(s3 ?: "")}\n")
            if (workflowType == WorkflowType.DISPATCH) append("廠區 Fab: ${MockData.resolveFabLabel(s4 ?: "")}\n")
            if (workflowType == WorkflowType.ENTER) append("停車格 Spot: ${MockData.resolveSpotLabel(s4 ?: "")}\n")
        }
    }

    private fun openInput(step: Int) {
        pendingStep = step
        val (title, presets, presetOnly) = when (step) {
            1 -> Triple(getString(R.string.step_staff), MockData.staffList.map { it.id }, false)
            2 -> when (workflowType) {
                WorkflowType.EXIT -> Triple(getString(R.string.step_vendor), MockData.vendorDrivers.map { it.id }, false)
                else -> Triple(getString(R.string.step_driver), MockData.drivers.map { it.id }, false)
            }
            3 -> Triple(getString(R.string.step_tank), MockData.tankNos, true)
            4 -> when (workflowType) {
                WorkflowType.DISPATCH -> Triple(getString(R.string.step_fab), MockData.tsmcFabs, true)
                WorkflowType.ENTER -> Triple(getString(R.string.step_spot), MockData.parkingSpots.map { it.spotId }, true)
                WorkflowType.EXIT -> Triple("", emptyList(), true)
            }
            else -> Triple("", emptyList(), false)
        }
        VirtualKeyboardDialog.newInstance(title, presets, presetOnly)
            .show(childFragmentManager, "wf_step_$step")
    }

    override fun onConfirmed(value: String) {
        appVm.setWorkflowStep(pendingStep, value)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_TYPE = "arg_type"
        fun newInstance(type: WorkflowType) = WorkflowFragment().apply {
            arguments = bundleOf(ARG_TYPE to type.name)
        }
    }
}
