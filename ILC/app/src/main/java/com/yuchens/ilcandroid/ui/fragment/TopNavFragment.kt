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
import com.yuchens.ilcandroid.databinding.FragmentTopNavBinding
import com.yuchens.ilcandroid.ui.MainActivity
import com.yuchens.ilcandroid.ui.viewmodel.AppViewModel
import kotlinx.coroutines.launch

class TopNavFragment : Fragment() {

    private var _binding: FragmentTopNavBinding? = null
    private val binding get() = _binding!!
    private val appVm: AppViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTopNavBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnMenu.visibility = View.GONE
        binding.btnLogout.setOnClickListener { (activity as? MainActivity)?.logout() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                appVm.userName.collect { name ->
                    binding.txtTopTitle.text = if (name.isNotBlank()) {
                        getString(R.string.top_nav_user, name)
                    } else {
                        getString(R.string.app_name)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
