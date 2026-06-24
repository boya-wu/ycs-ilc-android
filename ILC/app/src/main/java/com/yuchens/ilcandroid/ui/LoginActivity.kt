package com.yuchens.ilcandroid.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.yuchens.ilcandroid.BuildConfig
import com.yuchens.ilcandroid.databinding.ActivityLoginBinding
import com.yuchens.ilcandroid.data.UserRole

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val vn = BuildConfig.APP_VERSION_NAME
        val vc = BuildConfig.APP_VERSION_CODE
        val showType = BuildConfig.SHOW_BUILD_TYPE
        binding.txtVersion.text = if (showType) "v$vn ($vc) · ${BuildConfig.BUILD_TYPE}" else "v$vn"

        binding.btnRoleStaff.setOnClickListener { navigateWithRole(UserRole.STAFF) }
        binding.btnRoleDriver.setOnClickListener { navigateWithRole(UserRole.DRIVER) }
        binding.btnRoleVendor.setOnClickListener { navigateWithRole(UserRole.VENDOR) }
    }

    private fun navigateWithRole(role: UserRole) {
        startActivity(Intent(this, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_ROLE, role.key)
        })
        finish()
    }
}
