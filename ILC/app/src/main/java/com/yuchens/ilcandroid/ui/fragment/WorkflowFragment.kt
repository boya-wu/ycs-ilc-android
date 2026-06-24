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
import com.yuchens.ilcandroid.data.Screen
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

        binding.btnBack.setOnClickListener { goBackToDashboard() }
        binding.btnHome.setOnClickListener { (activity as MainActivity).logout() }

        binding.rowStep1.setOnClickListener { openInput(1) }
        binding.rowStep2.setOnClickListener { openInput(2) }
        binding.rowStep3.setOnClickListener { openInput(3) }
        binding.rowStep4.setOnClickListener { openInput(4) }

        binding.btnSubmit.setOnClickListener {
            appVm.submitWorkflow()
            goBackToDashboard()
        }
        binding.btnReset.setOnClickListener {
            appVm.startWorkflow(workflowType)
            binding.layoutConfirm.visibility = View.GONE
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { appVm.wfStep1.collect { updateSteps() } }
                launch { appVm.wfStep2.collect { updateSteps() } }
                launch { appVm.wfStep3.collect { updateSteps() } }
                launch { appVm.wfStep4.collect { updateSteps() } }
            }
        }
    }

    private fun goBackToDashboard() {
        (activity as MainActivity).navigateTo(Screen.HOME)
    }

    private fun tapHint(label: String) = getString(R.string.tap_to_input, label)

    private fun stepLabel(step: Int): String = when (step) {
        1 -> getString(R.string.step_staff)
        2 -> if (workflowType == WorkflowType.EXIT) getString(R.string.step_vendor) else getString(R.string.step_driver)
        3 -> getString(R.string.step_tank)
        4 -> when (workflowType) {
            WorkflowType.DISPATCH -> getString(R.string.step_fab)
            WorkflowType.ENTER -> getString(R.string.step_spot)
            WorkflowType.EXIT -> ""
        }
        else -> ""
    }

    private fun stepValue(step: Int): String? = when (step) {
        1 -> appVm.wfStep1.value
        2 -> appVm.wfStep2.value
        3 -> appVm.wfStep3.value
        4 -> appVm.wfStep4.value
        else -> null
    }

    private fun updateSteps() {
        val s1 = appVm.wfStep1.value
        val s2 = appVm.wfStep2.value
        val s3 = appVm.wfStep3.value
        val s4 = appVm.wfStep4.value

        binding.txtStep1Value.text = s1?.let { appVm.resolveStaffLabel(it) } ?: tapHint(stepLabel(1))
        binding.txtStep2Value.text = when (workflowType) {
            WorkflowType.EXIT -> s2?.let { appVm.resolveVendorLabel(it) }
            else -> s2?.let { appVm.resolveDriverLabel(it) }
        } ?: tapHint(stepLabel(2))
        binding.txtStep3Value.text = s3?.let { appVm.resolveTankLabel(it) } ?: tapHint(stepLabel(3))
        binding.txtStep4Value.text = when (workflowType) {
            WorkflowType.DISPATCH -> s4?.let { appVm.resolveFabLabel(it) }
            WorkflowType.ENTER -> s4?.let { appVm.resolveSpotLabel(it) }
            WorkflowType.EXIT -> "-"
        } ?: tapHint(stepLabel(4))

        binding.rowStep4.visibility = if (workflowType == WorkflowType.EXIT) View.GONE else View.VISIBLE
        binding.txtStep2Label.text = stepLabel(2)
        binding.txtStep4Label.text = stepLabel(4)

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
            append("值班 Staff: ${appVm.resolveStaffLabel(s1 ?: "")}\n")
            append(when (workflowType) {
                WorkflowType.EXIT -> "廠商 Vendor: ${appVm.resolveVendorLabel(s2 ?: "")}\n"
                else -> "司機 Driver: ${appVm.resolveDriverLabel(s2 ?: "")}\n"
            })
            append("槽體 Tank: ${appVm.resolveTankLabel(s3 ?: "")}\n")
            if (workflowType == WorkflowType.DISPATCH) append("廠區 Fab: ${appVm.resolveFabLabel(s4 ?: "")}\n")
            if (workflowType == WorkflowType.ENTER) append("停車格 Spot: ${appVm.resolveSpotLabel(s4 ?: "")}\n")
        }
    }

    private fun openInput(step: Int) {
        pendingStep = step
        val label = stepLabel(step)
        VirtualKeyboardDialog.newInstance(label, tapHint(label))
            .show(childFragmentManager, "wf_step_$step")
    }

    private fun findNextEmptyStep(afterStep: Int): Int? {
        val maxStep = if (workflowType == WorkflowType.EXIT) 3 else 4
        for (step in (afterStep + 1)..maxStep) {
            if (stepValue(step) == null) return step
        }
        return null
    }

    override fun onConfirmed(value: String) {
        appVm.setWorkflowStep(pendingStep, value)
        val nextStep = findNextEmptyStep(pendingStep)
        if (nextStep != null) {
            binding.root.post { openInput(nextStep) }
        }
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
