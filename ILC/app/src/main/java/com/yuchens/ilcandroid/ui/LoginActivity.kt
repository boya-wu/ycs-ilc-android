package com.yuchens.ilcandroid.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.yuchens.ilcandroid.BuildConfig
import com.yuchens.ilcandroid.R
import com.yuchens.ilcandroid.data.UserRole
import com.yuchens.ilcandroid.databinding.ActivityLoginBinding
import com.yuchens.ilcandroid.ui.widget.VirtualKeyboardDialog

class LoginActivity : AppCompatActivity(), VirtualKeyboardDialog.Listener {

    private lateinit var binding: ActivityLoginBinding
    private var selectedRole: UserRole? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val vn = BuildConfig.APP_VERSION_NAME
        val vc = BuildConfig.APP_VERSION_CODE
        val showType = BuildConfig.SHOW_BUILD_TYPE
        binding.txtVersion.text = if (showType) "v$vn ($vc) · ${BuildConfig.BUILD_TYPE}" else "v$vn"

        binding.btnRoleStaff.setOnClickListener { showEmployeeIdDialog(UserRole.STAFF) }
        binding.btnRoleDriver.setOnClickListener { showEmployeeIdDialog(UserRole.DRIVER) }
        binding.btnRoleVendor.setOnClickListener { showEmployeeIdDialog(UserRole.VENDOR) }
    }

    private fun showEmployeeIdDialog(role: UserRole) {
        selectedRole = role
        val title = when (role) {
            UserRole.STAFF -> getString(R.string.login_staff_id_title)
            UserRole.DRIVER -> getString(R.string.login_driver_id_title)
            UserRole.VENDOR -> getString(R.string.login_vendor_id_title)
        }
        val presets = when (role) {
            UserRole.STAFF -> listOf("H5406340", "H5406341")
            UserRole.DRIVER -> listOf("D001", "D002")
            UserRole.VENDOR -> listOf("V001", "V002")
        }
        VirtualKeyboardDialog.newInstance(title, presets).show(supportFragmentManager, "employee_id")
    }

    override fun onConfirmed(value: String) {
        val role = selectedRole ?: return
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_ROLE, role.key)
            putExtra(MainActivity.EXTRA_USER_ID, value)
        }
        startActivity(intent)
        finish()
    }
}
