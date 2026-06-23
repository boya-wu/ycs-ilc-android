package com.yuchens.equipinspectandroid.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yuchens.equipinspectandroid.data.repository.ApiRepository
import com.yuchens.equipinspectandroid.data.repository.LocalRepository
import com.yuchens.equipinspectandroid.util.ConfigStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val repo: LocalRepository,
) : ViewModel() {

    sealed class UiState {
        data object Idle : UiState()
        data object Loading : UiState()
        data object Success : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _state = MutableStateFlow<UiState>(UiState.Idle)
    val state: StateFlow<UiState> = _state

    /** 取得目前 BaseUrl（一次性） */
    suspend fun currentBaseUrl(): String =
        ConfigStore.getBaseUrl(appContext)

    /** 寫入，成功回傳 true */
    suspend fun setBaseUrlNormalized(input: String): Boolean {
        return ConfigStore.setBaseUrl(appContext, input)
    }

    // 讀取目前秒數（轉為 Int 方便畫面用）
    suspend fun currentLimitWindowSeconds(): Int {
        return (ConfigStore.getLimitWindowMs(appContext) / 1000L).toInt()
    }

    // 寫入（字串解析）
    suspend fun setLimitWindowSeconds(input: String): Boolean {
        val sec = input.trim().toIntOrNull() ?: return false
        ConfigStore.setLimitWindowMs(appContext, sec * 1000L)
        return true
    }

    fun ping() {
        if (_state.value is UiState.Loading) return
        _state.value = UiState.Loading

        viewModelScope.launch {
            val result = ApiRepository.ping(appContext, repo)
            _state.value = if (result.isSuccess) {
                UiState.Success
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "未知錯誤")
            }
        }
    }

    fun reset() { _state.value = UiState.Idle }
}
