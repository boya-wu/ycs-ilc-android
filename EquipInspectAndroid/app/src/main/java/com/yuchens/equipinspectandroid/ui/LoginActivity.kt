package com.yuchens.equipinspectandroid.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.yuchens.equipinspectandroid.R
import com.yuchens.equipinspectandroid.databinding.ActivityLoginBinding
import com.yuchens.equipinspectandroid.ui.viewmodel.LoginViewModel
import com.yuchens.equipinspectandroid.BuildConfig
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private val vm: LoginViewModel by viewModels()
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // === 顯示版本資訊 ===
        val vn = BuildConfig.APP_VERSION_NAME
        val vc = BuildConfig.APP_VERSION_CODE
        val showType = BuildConfig.SHOW_BUILD_TYPE && !vn.contains("-")
        val versionLabel = if (showType) "v$vn ($vc) · ${BuildConfig.BUILD_TYPE}" else "v$vn"
        binding.txtVersion.text = versionLabel

        // 讓 prod 使用者也能取得 versionCode（長按顯示/複製）
        binding.txtVersion.setOnLongClickListener {
            val full = "v$vn ($vc) · ${BuildConfig.BUILD_TYPE}"
            Toast.makeText(this, full, Toast.LENGTH_SHORT).show()
            val cm = getSystemService(android.content.ClipboardManager::class.java)
            cm.setPrimaryClip(android.content.ClipData.newPlainText("App Version", full))
            true
        }

        binding.btnLogin.setOnClickListener {
            vm.login(
                binding.etUsername.text.toString().trim(),
                binding.etPassword.text.toString().trim()
            )
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.state.collect { st ->
                    when (st) {
                        is LoginViewModel.UiState.Idle -> setLoading(false)
                        is LoginViewModel.UiState.Loading -> setLoading(true)
                        is LoginViewModel.UiState.Error -> {
                            setLoading(false)
                            Toast.makeText(this@LoginActivity, st.message, Toast.LENGTH_SHORT).show()
                            vm.reset()
                        }
                        is LoginViewModel.UiState.Success -> {
                            setLoading(false)
                            Toast.makeText(this@LoginActivity, getString(R.string.desc_login_success), Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        }
                    }
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.btnLogin.isEnabled = !loading
        binding.etUsername.isEnabled = !loading
        binding.etPassword.isEnabled = !loading
        binding.btnLogin.alpha = if (loading) 0.6f else 1f
        binding.btnLogin.text = if (loading) {
            getString(R.string.desc_login_logging_in)
        } else {
            getString(R.string.desc_login)
        }
    }
}

