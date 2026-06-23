package com.yuchens.equipinspectandroid.data.remote

import kotlinx.serialization.json.Json

val json by lazy {
    Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }
}
