package com.yuchens.ilcandroid.ui.widget

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.yuchens.ilcandroid.R
import com.yuchens.ilcandroid.databinding.DialogVirtualKeyboardBinding

class VirtualKeyboardDialog : DialogFragment() {

    interface Listener {
        fun onConfirmed(value: String)
    }

    private var _binding: DialogVirtualKeyboardBinding? = null
    private val binding get() = _binding!!
    private val buffer = StringBuilder()

    var title: String = ""
    var presets: List<String> = emptyList()
    var presetOnly: Boolean = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogVirtualKeyboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.txtDialogTitle.text = title
        binding.etInput.showSoftInputOnFocus = false

        if (presetOnly || presets.isNotEmpty()) {
            binding.layoutPresets.visibility = View.VISIBLE
            binding.layoutNumpad.visibility = if (presetOnly) View.GONE else View.VISIBLE
            binding.layoutPresets.removeAllViews()
            presets.forEach { preset ->
                val btn = layoutInflater.inflate(R.layout.item_preset_chip, binding.layoutPresets, false) as Button
                btn.text = preset
                btn.setOnClickListener {
                    buffer.clear()
                    buffer.append(preset)
                    binding.etInput.setText(preset)
                }
                binding.layoutPresets.addView(btn)
            }
        } else {
            binding.layoutPresets.visibility = View.GONE
            binding.layoutNumpad.visibility = View.VISIBLE
        }

        val keys = listOf("1","2","3","4","5","6","7","8","9","0","A","B","C","D","E","F","-")
        binding.gridNumpad.removeAllViews()
        keys.forEach { key ->
            val btn = layoutInflater.inflate(R.layout.item_numpad_key, binding.gridNumpad, false) as Button
            btn.text = key
            btn.setOnClickListener { appendKey(key) }
            val params = android.widget.GridLayout.LayoutParams().apply {
                columnSpec = android.widget.GridLayout.spec(android.widget.GridLayout.UNDEFINED, 1f)
                width = 0
            }
            btn.layoutParams = params
            binding.gridNumpad.addView(btn)
        }

        binding.btnBackspace.setOnClickListener {
            if (buffer.isNotEmpty()) {
                buffer.deleteCharAt(buffer.length - 1)
                binding.etInput.setText(buffer.toString())
            }
        }
        binding.btnClear.setOnClickListener {
            buffer.clear()
            binding.etInput.setText("")
        }
        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnConfirm.setOnClickListener {
            val value = binding.etInput.text.toString().trim()
            if (value.isNotEmpty()) {
                (parentFragment as? Listener ?: activity as? Listener)?.onConfirmed(value)
                dismiss()
            }
        }
    }

    private fun appendKey(key: String) {
        buffer.append(key)
        binding.etInput.setText(buffer.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(title: String, presets: List<String> = emptyList(), presetOnly: Boolean = false) =
            VirtualKeyboardDialog().apply {
                this.title = title
                this.presets = presets
                this.presetOnly = presetOnly
            }
    }
}
