package com.yuchens.equipinspectandroid.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yuchens.equipinspectandroid.data.repository.ApiRepository.download
import com.yuchens.equipinspectandroid.data.repository.LocalRepository
import com.yuchens.equipinspectandroid.util.LogHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class DownloadViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val repo: LocalRepository,
) : ViewModel() {

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        object Success : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _state = MutableStateFlow<UiState>(UiState.Idle)
    val state: StateFlow<UiState> = _state

    // 未傳筆數
    private val _needUploadCount = MutableStateFlow(0)
    val needUploadCount: StateFlow<Int> = _needUploadCount

    init {
        // 初始化就撈一次
        refreshNeedUploadCount()
    }

    fun refreshNeedUploadCount() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val count = repo.getEquipInspected().count()
                _needUploadCount.value = count
            } catch (e: Exception) {
                LogHelper.write(appContext, "NeedUploadCount failed: ${e.message}", e)
            }
        }
    }

    fun download() {
        if (_state.value is UiState.Loading) return

        // 檢查未傳筆數，非 0 則提醒並中止下載
        val pending = _needUploadCount.value
        if (pending > 0) {
            _state.value = UiState.Error("尚有 $pending 筆巡檢資料未上傳，請先完成上傳作業")
            return
        }

        _state.value = UiState.Loading

        viewModelScope.launch {
            val result = download(appContext, repo)
            _state.value = if (result.isSuccess) {
                UiState.Success
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "未知錯誤")
            }

            refreshNeedUploadCount()
        }
    }

    fun reset() { _state.value = UiState.Idle }
}

