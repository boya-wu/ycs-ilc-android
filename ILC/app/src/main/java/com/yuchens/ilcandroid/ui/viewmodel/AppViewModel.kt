package com.yuchens.ilcandroid.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.yuchens.ilcandroid.data.MockData
import com.yuchens.ilcandroid.data.NavTab
import com.yuchens.ilcandroid.data.PunchInRecord
import com.yuchens.ilcandroid.data.UserRole
import com.yuchens.ilcandroid.data.WorkflowType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppViewModel : ViewModel() {

    private val _role = MutableStateFlow<UserRole?>(null)
    val role: StateFlow<UserRole?> = _role.asStateFlow()

    private val _userId = MutableStateFlow("")
    val userId: StateFlow<String> = _userId.asStateFlow()

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _toast = MutableStateFlow<String?>(null)
    val toast: StateFlow<String?> = _toast.asStateFlow()

    private val _punchRecords = MutableStateFlow(
        listOf(
            PunchInRecord("陳建國", "值班主管", "H5406340", "07:30:12", "上班"),
            PunchInRecord("王志明", "現場操作", "H5406341", "07:45:33", "上班")
        )
    )
    val punchRecords: StateFlow<List<PunchInRecord>> = _punchRecords.asStateFlow()

    val parkingSpots = MockData.parkingSpots
    val shifts = MockData.shifts

    val staffList get() = MockData.staffList
    val drivers get() = MockData.drivers
    val vendorDrivers get() = MockData.vendorDrivers
    val tankNos get() = MockData.tankNos
    val tsmcFabs get() = MockData.tsmcFabs

    fun resolveStaffLabel(id: String) = MockData.resolveStaffLabel(id)
    fun resolveVendorLabel(id: String) = MockData.resolveVendorLabel(id)
    fun resolveDriverLabel(id: String) = MockData.resolveDriverLabel(id)
    fun resolveTankLabel(id: String) = MockData.resolveTankLabel(id)
    fun resolveFabLabel(id: String) = MockData.resolveFabLabel(id)
    fun resolveSpotLabel(id: String) = MockData.resolveSpotLabel(id)

    // Workflow steps
    private val _wfStep1 = MutableStateFlow<String?>(null)
    val wfStep1: StateFlow<String?> = _wfStep1.asStateFlow()
    private val _wfStep2 = MutableStateFlow<String?>(null)
    val wfStep2: StateFlow<String?> = _wfStep2.asStateFlow()
    private val _wfStep3 = MutableStateFlow<String?>(null)
    val wfStep3: StateFlow<String?> = _wfStep3.asStateFlow()
    private val _wfStep4 = MutableStateFlow<String?>(null)
    val wfStep4: StateFlow<String?> = _wfStep4.asStateFlow()

    private val _activeWorkflow = MutableStateFlow<WorkflowType?>(null)
    val activeWorkflow: StateFlow<WorkflowType?> = _activeWorkflow.asStateFlow()

    fun login(role: UserRole, employeeId: String) {
        _role.value = role
        _userId.value = employeeId
        _userName.value = when (role) {
            UserRole.STAFF -> MockData.findStaff(employeeId)?.name ?: "陳建國"
            UserRole.DRIVER -> MockData.findDriver(employeeId)?.name ?: "林大宏"
            UserRole.VENDOR -> MockData.findVendorDriver(employeeId)?.name ?: "劉家慶"
        }
        showToast("登入成功 Login OK")
    }

    fun logout() {
        _role.value = null
        _userId.value = ""
        _userName.value = ""
        resetWorkflow()
    }

    fun showToast(msg: String) { _toast.value = msg }
    fun clearToast() { _toast.value = null }

    fun startWorkflow(type: WorkflowType) {
        resetWorkflow()
        _activeWorkflow.value = type
    }

    fun resetWorkflow() {
        _wfStep1.value = null
        _wfStep2.value = null
        _wfStep3.value = null
        _wfStep4.value = null
        _activeWorkflow.value = null
    }

    fun setWorkflowStep(step: Int, value: String) {
        when (step) {
            1 -> _wfStep1.value = value
            2 -> _wfStep2.value = value
            3 -> _wfStep3.value = value
            4 -> _wfStep4.value = value
        }
    }

    fun submitWorkflow(): Boolean {
        val type = _activeWorkflow.value ?: return false
        val now = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        when (type) {
            WorkflowType.DISPATCH -> {
                showToast("出工登記完成 Dispatch OK — ${MockData.resolveFabLabel(_wfStep4.value ?: "")}")
            }
            WorkflowType.ENTER -> {
                val spotId = _wfStep4.value ?: "P-06"
                val idx = parkingSpots.indexOfFirst { it.spotId == spotId }
                if (idx >= 0) {
                    parkingSpots[idx] = parkingSpots[idx].copy(
                        status = "滿",
                        matName = "Dev-1",
                        tankNo = _wfStep3.value ?: "Tank_B",
                        carNo = "DP1086",
                        hours = 0.1,
                        driver = MockData.findDriver(_wfStep2.value ?: "")?.name ?: "林大宏",
                        vendor = "長春化工"
                    )
                }
                showToast("進場登記完成 Enter OK $spotId — $now，已通知值班人員")
            }
            WorkflowType.EXIT -> {
                val tank = _wfStep3.value ?: "Tank_A"
                val spot = parkingSpots.find { it.tankNo.contains(tank, true) }
                    ?: parkingSpots.find { it.spotId == "P-06" }
                spot?.let { s ->
                    val idx = parkingSpots.indexOf(s)
                    parkingSpots[idx] = s.copy(
                        status = "空", matName = "-", tankNo = "-", carNo = "-",
                        hours = 0.0, driver = "-", vendor = "-"
                    )
                    showToast("離場登記完成 Exit OK — 已釋放 ${s.spotId}")
                }
            }
        }
        resetWorkflow()
        return true
    }

    fun punchIn(staffId: String) {
        val staff = MockData.findStaff(staffId)
        if (staff == null) {
            showToast("找不到工號 Staff not found: $staffId")
            return
        }
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        _punchRecords.value = listOf(
            PunchInRecord(staff.name, staff.role, staff.id, time, "上班")
        ) + _punchRecords.value
        showToast("${staff.name} 上班打卡 Clock-in — $time")
    }

    fun punchOut(staffId: String) {
        val staff = MockData.findStaff(staffId)
        if (staff == null) {
            showToast("找不到工號 Staff not found: $staffId")
            return
        }
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        _punchRecords.value = listOf(
            PunchInRecord(staff.name, staff.role, staff.id, time, "下班")
        ) + _punchRecords.value
        showToast("${staff.name} 下班打卡 Clock-out — $time")
    }

    fun onDutyStaff(): List<String> =
        _punchRecords.value
            .groupBy { it.id }
            .mapNotNull { (_, records) ->
                val latest = records.first()
                if (latest.type == "上班") latest.name else null
            }
            .distinct()
}
