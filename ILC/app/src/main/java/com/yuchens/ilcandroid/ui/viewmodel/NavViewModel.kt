package com.yuchens.ilcandroid.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.yuchens.ilcandroid.data.NavTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NavViewModel : ViewModel() {
    private val _currentTab = MutableStateFlow(NavTab.HOME)
    val currentTab: StateFlow<NavTab> = _currentTab.asStateFlow()

    fun select(tab: NavTab) { _currentTab.value = tab }
    fun reset() { _currentTab.value = NavTab.HOME }
}
