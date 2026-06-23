package com.yuchens.ilcandroid.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.yuchens.ilcandroid.R
import com.yuchens.ilcandroid.data.NavTab
import com.yuchens.ilcandroid.data.UserRole
import com.yuchens.ilcandroid.data.WorkflowType
import com.yuchens.ilcandroid.databinding.ActivityMainBinding
import com.yuchens.ilcandroid.ui.fragment.DashboardFragment
import com.yuchens.ilcandroid.ui.fragment.PlaceholderFragment
import com.yuchens.ilcandroid.ui.fragment.PunchFragment
import com.yuchens.ilcandroid.ui.fragment.WorkflowFragment
import com.yuchens.ilcandroid.ui.viewmodel.AppViewModel
import com.yuchens.ilcandroid.ui.viewmodel.NavViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ROLE = "extra_role"
        const val EXTRA_USER_ID = "extra_user_id"
    }

    private lateinit var binding: ActivityMainBinding
    private val navVm: NavViewModel by viewModels()
    private val appVm: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val roleKey = intent.getStringExtra(EXTRA_ROLE) ?: return finish()
        val userId = intent.getStringExtra(EXTRA_USER_ID) ?: return finish()
        val role = UserRole.fromKey(roleKey) ?: return finish()
        appVm.login(role, userId)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                navVm.currentTab.collect { tab -> navigateToTab(tab, role) }
            }
        }

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

        if (savedInstanceState == null) {
            navVm.select(NavTab.HOME)
        }
    }

    private fun navigateToTab(tab: NavTab, role: UserRole) {
        val fragment: Fragment = when (tab) {
            NavTab.HOME -> DashboardFragment()
            NavTab.DISPATCH -> WorkflowFragment.newInstance(WorkflowType.DISPATCH)
            NavTab.ENTER -> WorkflowFragment.newInstance(WorkflowType.ENTER)
            NavTab.EXIT -> WorkflowFragment.newInstance(WorkflowType.EXIT)
            NavTab.SHIFT -> PunchFragment.newInstance(PunchFragment.MODE_SHIFT)
            NavTab.TRANSFER -> PlaceholderFragment.newInstance(
                getString(R.string.tab_transfer), getString(R.string.placeholder_transfer)
            )
            NavTab.ALARM -> PlaceholderFragment.newInstance(
                getString(R.string.tab_alarm), getString(R.string.placeholder_alarm)
            )
            NavTab.SETTING -> PlaceholderFragment.newInstance(
                getString(R.string.tab_setting), getString(R.string.placeholder_setting)
            )
            NavTab.RECORDS -> PlaceholderFragment.newInstance(
                getString(R.string.tab_records), buildRecordsSummary()
            )
            NavTab.HISTORY -> PlaceholderFragment.newInstance(
                getString(R.string.tab_history), getString(R.string.placeholder_history)
            )
        }
        replaceFragment(fragment, tag = tab.name)
    }

    private fun buildRecordsSummary(): String {
        val spots = appVm.parkingSpots.joinToString("\n") {
            "${it.spotId} | ${it.status} | ${it.tankNo} | ${it.driver}"
        }
        return getString(R.string.placeholder_records) + "\n\n" + spots
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
        navVm.reset()
        startActivity(Intent(this, LoginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        })
        finish()
    }

    fun selectTab(tab: NavTab) = navVm.select(tab)
}
