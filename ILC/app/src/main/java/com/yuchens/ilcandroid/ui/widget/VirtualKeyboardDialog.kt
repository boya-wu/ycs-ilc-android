package com.yuchens.ilcandroid.ui.widget

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.yuchens.ilcandroid.databinding.DialogVirtualKeyboardBinding

class VirtualKeyboardDialog : DialogFragment() {

    interface Listener {
        fun onConfirmed(value: String)
    }

    private var _binding: DialogVirtualKeyboardBinding? = null
    private val binding get() = _binding!!

    private val title: String
        get() = requireArguments().getString(ARG_TITLE) ?: ""

    private val hint: String?
        get() = requireArguments().getString(ARG_HINT)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogVirtualKeyboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.txtDialogTitle.text = title
        hint?.let { binding.etInput.hint = it }

        binding.etInput.requestFocus()
        binding.etInput.post {
            val imm = ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
            imm?.showSoftInput(binding.etInput, InputMethodManager.SHOW_IMPLICIT)
        }

        binding.etInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                confirmInput()
                true
            } else {
                false
            }
        }

        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnConfirm.setOnClickListener { confirmInput() }
    }

    private fun confirmInput() {
        val value = binding.etInput.text.toString().trim()
        if (value.isNotEmpty()) {
            (parentFragment as? Listener ?: activity as? Listener)?.onConfirmed(value)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_TITLE = "arg_title"
        private const val ARG_HINT = "arg_hint"

        fun newInstance(title: String, hint: String? = null) = VirtualKeyboardDialog().apply {
            arguments = bundleOf(
                ARG_TITLE to title,
                ARG_HINT to hint
            )
        }
    }
}
