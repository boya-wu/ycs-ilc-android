package com.yuchens.equipinspectandroid.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yuchens.equipinspectandroid.data.repository.LocalRepository
import com.yuchens.equipinspectandroid.ui.model.EquipUi
import com.yuchens.equipinspectandroid.data.mapper.toUi
import com.yuchens.equipinspectandroid.ui.model.Option
import com.yuchens.equipinspectandroid.util.ConfigStore
import com.yuchens.equipinspectandroid.util.LogHelper
import com.yuchens.equipinspectandroid.util.ScanRateLimiter
import com.yuchens.equipinspectandroid.util.UserPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class InspectViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val repo: LocalRepository,
    private val prefs: UserPrefs,
    private val limiter: ScanRateLimiter
) : ViewModel() {

    // 多筆候選（轉成 UI）
    private val _candidates = MutableStateFlow<List<EquipUi>>(emptyList())
    val candidates: StateFlow<List<EquipUi>> = _candidates

    // 目前所選設備（顯示細節用）
    private val _equip = MutableStateFlow<EquipUi?>(null)
    val equip: StateFlow<EquipUi?> = _equip

    private val _options = MutableStateFlow<List<Option>>(emptyList())
    val options: StateFlow<List<Option>> = _options

    private val _message = MutableSharedFlow<String>(replay = 0, extraBufferCapacity = 16)
    val message: SharedFlow<String> = _message

    fun tryLoadWithRateLimit(barcode: String) {
        viewModelScope.launch {
            val limitOn = prefs.intervalLimit.firstOrNull() == true
            val windowMs = ConfigStore.getLimitWindowMs(appContext)
            val remaining = if (limitOn) limiter.remainingIfBlocked(barcode, windowMs) else 0L
            if (remaining > 0L) {
                val remainSec = (remaining / 1000).coerceAtLeast(1)
                postMessage("限制：請等 $remainSec 秒後，再刷不同條碼")
                return@launch
            }
            limiter.mark(barcode)
            loadCandidates(barcode)
        }
    }

    private fun postMessage(msg: String) { _message.tryEmit(msg) }

    /** 依 key 載入多筆候選，預設選第一筆 */
    private suspend fun loadCandidates(barcode: String) {
        val list = repo.findAllByKeyPreferGuidFirst(barcode)
        if (list.isEmpty()) {
            _candidates.value = emptyList()
            _equip.value = null
            _options.value = emptyList()
            postMessage("查無條碼/設備資料：$barcode")
            return
        }

        val uiList = list.map { it.toUi() }
        _candidates.value = uiList

        // 預設第一筆（以 guid 指定）
        selectCandidateByGuid(uiList.first().guid)
    }

    /** 切換目前選取的候選（由 Fragment 的設備下拉選單呼叫） */
    fun selectCandidateByGuid(guid: String?) {
        val uiList = _candidates.value
        val selected = uiList.firstOrNull { it.guid.equals(guid, ignoreCase = true) }
            ?: return
        _equip.value = selected

        // 同步載入該設備的異常清單
        viewModelScope.launch {
            val abns = repo.getEquipAbnormalByItem(selected.itemId ?: "")
            val opts = buildList {
                add(Option("", ""))
                addAll(abns.map { Option(value = it.guid, text = it.abnormalName ?: "") })
            }
            _options.value = opts
        }
    }

    /** 送出確認，對目前所選設備執行更新 */
    suspend fun confirm(abnormalNote: String?): Boolean {
        val current = _equip.value ?: return false
        val username = prefs.username.firstOrNull()
        val nowIso = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

        return repo.updateEquipAbnormal(
            guid = current.guid ?: "",
            abnormal = abnormalNote,
            updateUser = username,
            updateTime = nowIso
        )
    }

    /** 清除目前設備的異常與更新資訊（全部設回 NULL） */
    suspend fun clearAbnormal(): Boolean {
        val current = _equip.value ?: return false
        val ok = repo.updateEquipAbnormal(
            guid = current.guid ?: "",
            abnormal = null,
            updateUser = null,
            updateTime = null
        )
        if (ok) {
            // 同步把 ViewModel 內的狀態也清掉，讓 UI 立刻反映
            _equip.value = current.copy(
                abnormal = null,
                inspected = false
            )
        }
        return ok
    }

    fun clear() {
        _candidates.value = emptyList()
        _equip.value = null
        _options.value = emptyList()
    }
}
