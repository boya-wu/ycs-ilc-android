package com.yuchens.equipinspectandroid.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yuchens.equipinspectandroid.data.mapper.toUiList
import com.yuchens.equipinspectandroid.data.repository.LocalRepository
import com.yuchens.equipinspectandroid.ui.model.EquipUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InspectedViewModel@Inject constructor(
    private val repo: LocalRepository,
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = false,
        val items: List<EquipUi> = emptyList(),
        val error: String? = null
    )

    private val _items = MutableStateFlow<List<EquipUi>>(emptyList())
    val items: StateFlow<List<EquipUi>> = _items

    private val _state = MutableStateFlow<UiState>(UiState(isLoading = false))
    val state: StateFlow<UiState> = _state

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            runCatching { repo.getEquipInspected().toUiList() }
                .onSuccess { list ->
                    _state.value = UiState(isLoading = false, items = list, error = null)
                }
                .onFailure { e ->
                    _state.value = UiState(isLoading = false, items = emptyList(), error = e.message)
                }
        }
    }

}
