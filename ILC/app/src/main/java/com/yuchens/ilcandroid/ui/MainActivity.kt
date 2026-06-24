package com.yuchens.ilcandroid.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.yuchens.ilcandroid.R
import com.yuchens.ilcandroid.data.Screen
import com.yuchens.ilcandroid.data.UserRole
import com.yuchens.ilcandroid.data.WorkflowType
import com.yuchens.ilcandroid.databinding.ActivityMainBinding
import com.yuchens.ilcandroid.ui.fragment.DashboardFragment
import com.yuchens.ilcandroid.ui.fragment.PunchFragment
import com.yuchens.ilcandroid.ui.fragment.WorkflowFragment
import com.yuchens.ilcandroid.ui.viewmodel.AppViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ROLE = "extra_role"
    }

    private lateinit var binding: ActivityMainBinding
    private val appVm: AppViewModel by viewModels()
    private var currentScreen = Screen.HOME

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.containerFragment) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val roleKey = intent.getStringExtra(EXTRA_ROLE) ?: return finish()
        val role = UserRole.fromKey(roleKey) ?: return finish()
        appVm.selectRole(role)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                appVm.toast.collect { msg ->
                    msg?.let {
                        Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show()
                        appVm.clearToast()
                    }
                }
            }
        }

        onBackPressedDispatcher.addCallback(this) {
            if (currentScreen == Screen.HOME) finish()
            else navigateTo(Screen.HOME)
        }

        if (savedInstanceState == null) navigateTo(Screen.HOME)
    }

    fun navigateTo(screen: Screen) {
        currentScreen = screen
        val fragment: Fragment = when (screen) {
            Screen.HOME -> DashboardFragment()
            Screen.PUNCH -> PunchFragment()
            Screen.DISPATCH -> WorkflowFragment.newInstance(WorkflowType.DISPATCH)
            Screen.ENTER -> WorkflowFragment.newInstance(WorkflowType.ENTER)
            Screen.EXIT -> WorkflowFragment.newInstance(WorkflowType.EXIT)
        }
        replaceFragment(fragment, screen.name)
    }

    private fun replaceFragment(fragment: Fragment, tag: String) {
        val current = supportFragmentManager.findFragmentById(R.id.containerFragment)
        if (current?.tag == tag) return
        supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in_pop, R.anim.fade_out_pop)
            .replace(R.id.containerFragment, fragment, tag)
            .commit()
    }

    fun logout() {
        appVm.logout()
        startActivity(Intent(this, LoginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        })
        finish()
    }
}
