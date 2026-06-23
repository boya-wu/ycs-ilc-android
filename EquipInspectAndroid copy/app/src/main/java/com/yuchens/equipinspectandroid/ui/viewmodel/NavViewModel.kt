package com.yuchens.equipinspectandroid.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NavViewModel : ViewModel() {

    enum class Tab { UNINSPECTED, INSPECTED, INSPECT, DOWNLOAD, UPLOAD }

    private val _current = MutableStateFlow(Tab.INSPECT)
    val current: StateFlow<Tab> = _current

    fun select(tab: Tab) { _current.value = tab }
}
