package com.yuchens.equipinspectandroid.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yuchens.equipinspectandroid.data.local.entity.UserEntity
import com.yuchens.equipinspectandroid.data.repository.LocalRepository
import com.yuchens.equipinspectandroid.util.LogHelper
import com.yuchens.equipinspectandroid.util.UserPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val repo: LocalRepository,
    private val userPrefs: UserPrefs
) : ViewModel() {

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Error(val message: String) : UiState()
        data class Success(val username: String) : UiState()
    }

    private val _state = MutableStateFlow<UiState>(UiState.Idle)
    val state: StateFlow<UiState> = _state

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _state.value = UiState.Error("請輸入帳號與密碼")
            return
        }
        if (_state.value is UiState.Loading) return

        _state.value = UiState.Loading
        viewModelScope.launch {
            try {
                // validateUser 回傳 UserEntity?（null = 失敗）
                val user: UserEntity? = repo.validateUser(username, password)
                if (user != null) {
                    // 存入 DataStore
                    userPrefs.setUsername(user.userNo ?: username)
                    userPrefs.setIntervalLimit(user.intervalLimit == true)

                    _state.value = UiState.Success(user.userNo ?: username)
                } else {
                    _state.value = UiState.Error("帳號或密碼錯誤")
                }
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "登入失敗")
            }
        }
    }

    fun reset() { _state.value = UiState.Idle }
}

