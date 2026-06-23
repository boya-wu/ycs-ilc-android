package com.yuchens.equipinspectandroid.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yuchens.equipinspectandroid.data.repository.ApiRepository.upload
import com.yuchens.equipinspectandroid.data.repository.LocalRepository
import com.yuchens.equipinspectandroid.util.LogHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class UploadViewModel @Inject constructor(
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

    // 上傳筆數
    private val _uploadCount = MutableStateFlow(0)
    val uploadCount: StateFlow<Int> = _uploadCount

    init {
        // 初始化就撈一次
        refreshUploadCount()
    }

    fun refreshUploadCount() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val count = repo.getEquipInspected().count()
                _uploadCount.value = count
            } catch (e: Exception) {
                LogHelper.write(appContext, "UploadCount failed: ${e.message}", e)
            }
        }
    }

    fun upload() {
        if (_state.value is UiState.Loading) return
        _state.value = UiState.Loading

        viewModelScope.launch {
            val result = upload(appContext, repo)
            _state.value = if (result.isSuccess) {
                UiState.Success
            } else {
                UiState.Error(result.exceptionOrNull()?.message ?: "未知錯誤")
            }

            refreshUploadCount()
        }
    }

    fun reset() { _state.value = UiState.Idle }
}

