package com.yuchens.equipinspectandroid.ui.model

data class Option(val value: String, val text: String) {
    override fun toString(): String {
        return text // Spinner 會顯示這個
    }
}
