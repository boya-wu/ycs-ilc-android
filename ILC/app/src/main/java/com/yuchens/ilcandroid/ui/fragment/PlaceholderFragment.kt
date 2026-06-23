package com.yuchens.ilcandroid.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.yuchens.ilcandroid.databinding.FragmentPlaceholderBinding

class PlaceholderFragment : Fragment() {

    private var _binding: FragmentPlaceholderBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPlaceholderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.txtPlaceholderTitle.text = requireArguments().getString(ARG_TITLE)
        binding.txtPlaceholderBody.text = requireArguments().getString(ARG_BODY)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_TITLE = "arg_title"
        private const val ARG_BODY = "arg_body"
        fun newInstance(title: String, body: String) = PlaceholderFragment().apply {
            arguments = bundleOf(ARG_TITLE to title, ARG_BODY to body)
        }
    }
}
