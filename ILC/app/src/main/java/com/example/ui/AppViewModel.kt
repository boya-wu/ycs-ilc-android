package com.example.ui

import androidx.lifecycle.ViewModel
import com.example.data.Driver
import com.example.data.ParkingSpot
import com.example.data.PunchInRecord
import com.example.data.ShiftAssignment
import com.example.data.StaffMember
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppViewModel : ViewModel() {

    // --- Active User & Navigation Session ---
    private val _currentRole = MutableStateFlow<String?>(null) // "STAFF", "DRIVER", "VENDOR_DRIVER", or null
    val currentRole: StateFlow<String?> = _currentRole

    private val _currentUserId = MutableStateFlow<String>("")
    val currentUserId: StateFlow<String> = _currentUserId

    private val _currentUserName = MutableStateFlow<String>("")
    val currentUserName: StateFlow<String> = _currentUserName

    // Nav navigation: Tab designation for each role
    private val _activeTab = MutableStateFlow("HOME")
    val activeTab: StateFlow<String> = _activeTab

    // --- Master Data State ---
    private val _staffList = MutableStateFlow(listOf(
        StaffMember("9812A", "陳建國", "值班主管", phone = "0912-345-678"),
        StaffMember("9845B", "王志明", "現場操作", phone = "0923-456-789"),
        StaffMember("9867C", "李明哲", "值班主管", phone = "0934-567-890"),
        StaffMember("9823D", "張家豪", "現場操作", phone = "0945-678-901")
    ))
    val staffList: StateFlow<List<StaffMember>> = _staffList

    private val _drivers = MutableStateFlow(listOf(
        Driver("林大宏", "長春化工", "DP1086"),
        Driver("黃世賢", "台聚化工", "CCPU1619"),
        Driver("陳俊生", "東應化", "TH1032"),
        Driver("蔡志強", "關東鑫林", "SUNU91")
    ))
    val drivers: StateFlow<List<Driver>> = _drivers

    private val _parkingSpots = MutableStateFlow(listOf(
        ParkingSpot("P-01", "滿", "Dev-1", "Tank_B", "DP1086", 14.5, "林大宏", "長春化工"),
        ParkingSpot("P-02", "滿", "H2O2_31%", "Tank_B", "CCPU161902-2", 9.2, "黃世賢", "台聚化工"),
        ParkingSpot("P-03", "空", "-", "-", "-", 0.0, "-", "-"),
        ParkingSpot("P-04", "滿", "Thin-1", "Tank_A", "TH1032A", 4.8, "陳俊生", "東應化"),
        ParkingSpot("P-05", "維護", "-", "-", "-", 0.0, "-", "-"),
        ParkingSpot("P-06", "滿", "Dev-1", "Tank_A", "DP1275", 2.1, "劉家慶", "長春化工"),
        ParkingSpot("P-07", "滿", "HF25%", "Tank_A", "SUNU9105510F", 6.8, "蔡志強", "關東鑫林"),
        ParkingSpot("P-08", "滿", "H2SO4-HT", "Tank_B", "970303-5", 8.5, "賴建明", "台灣巴斯夫"),
        ParkingSpot("P-09", "空", "-", "-", "-", 0.0, "-", "-"),
        ParkingSpot("P-10", "空", "-", "-", "-", 0.0, "-", "-"),
        ParkingSpot("P-11", "滿", "Thin-1", "Tank_A", "TH1032B", 5.2, "許志偉", "東應化"),
        ParkingSpot("P-12", "滿", "H2O2_31%", "Tank_A", "CCNU310054-8", 1.5, "吳啟源", "台聚化工")
    ))
    val parkingSpots: StateFlow<List<ParkingSpot>> = _parkingSpots

    val chemicalTypes = listOf("Dev-1", "H2O2_31%", "Thin-1", "HF25%", "H2SO4-HT", "IPA", "NH4OH")
    val tsmcFabs = listOf("F6", "F14A", "F14B", "F18A", "F18B")

    // --- Punch-in & Shifts State ---
    private val _punchRecords = MutableStateFlow(listOf(
        PunchInRecord("陳建國", "值班主管", "9812A", "07:30:12", "上班"),
        PunchInRecord("王志明", "現場操作", "9845B", "07:45:33", "上班")
    ))
    val punchRecords: StateFlow<List<PunchInRecord>> = _punchRecords

    private val _shifts = MutableStateFlow(listOf(
        ShiftAssignment("2026-06-16", "日班", listOf("陳建國", "王志明")),
        ShiftAssignment("2026-06-16", "夜班", listOf("李明哲", "張家豪")),
        ShiftAssignment("2026-06-17", "日班", listOf("陳建國", "李明哲")),
        ShiftAssignment("2026-06-17", "夜班", listOf("王志明", "張家豪"))
    ))
    val shifts: StateFlow<List<ShiftAssignment>> = _shifts

    // --- Active Scanning Flows ---
    // Represents workflow states for Screen 3, 4, 5
    // Each step is represented by a filled string or empty if not yet scanned

    // Screen 3: Start Dispatch
    val dispatchStep1 = MutableStateFlow<String?>(null) // Staff Barcode
    val dispatchStep2 = MutableStateFlow<String?>(null) // Driver Barcode
    val dispatchStep3 = MutableStateFlow<String?>(null) // Tank Barcode
    val dispatchStep4 = MutableStateFlow<String?>(null) // Fab Barcode
    val isDispatchCompleted = MutableStateFlow(false)

    // Screen 4: Enter Parking
    val enterStep1 = MutableStateFlow<String?>(null) // Staff Barcode
    val enterStep2 = MutableStateFlow<String?>(null) // Driver Barcode
    val enterStep3 = MutableStateFlow<String?>(null) // Tank Barcode
    val enterStep4 = MutableStateFlow<String?>(null) // Spot Barcode
    val isEnterCompleted = MutableStateFlow(false)

    // Screen 5: Vendor Exit
    val exitStep1 = MutableStateFlow<String?>(null) // Staff Barcode
    val exitStep2 = MutableStateFlow<String?>(null) // Vendor Driver Barcode
    val exitStep3 = MutableStateFlow<String?>(null) // Tank Barcode
    val isExitCompleted = MutableStateFlow(false)

    // Snackbar Message State
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage

    // --- Action Handlers ---

    fun login(role: String, barcodeInput: String) {
        _currentRole.value = role
        _currentUserId.value = barcodeInput
        _activeTab.value = "HOME"

        // Search in master mock lists
        when (role) {
            "STAFF" -> {
                val match = _staffList.value.find { it.id == barcodeInput || it.name == barcodeInput }
                _currentUserName.value = match?.name ?: "陳建國 (值班人員)"
            }
            "DRIVER" -> {
                val match = _drivers.value.find { it.name == barcodeInput || barcodeInput.contains(it.name) }
                _currentUserName.value = match?.name ?: "林大宏"
            }
            "VENDOR_DRIVER" -> {
                _currentUserName.value = if (barcodeInput.isNotBlank()) barcodeInput else "劉家慶 (長春化工)"
            }
        }
        showToast("登入成功 (Role: ${when(role) { "STAFF" -> "值班人員"; "DRIVER" -> "ILC司機"; else -> "廠商司機" }})")
    }

    fun logout() {
        _currentRole.value = null
        _currentUserId.value = ""
        _currentUserName.value = ""
        _activeTab.value = "HOME"
        resetScanWorkflows()
        showToast("已成功登出系統")
    }

    fun selectTab(tab: String) {
        _activeTab.value = tab
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun resetScanWorkflows() {
        dispatchStep1.value = null
        dispatchStep2.value = null
        dispatchStep3.value = null
        dispatchStep4.value = null
        isDispatchCompleted.value = false

        enterStep1.value = null
        enterStep2.value = null
        enterStep3.value = null
        enterStep4.value = null
        isEnterCompleted.value = false

        exitStep1.value = null
        exitStep2.value = null
        exitStep3.value = null
        isExitCompleted.value = false
    }

    // Workflows completion
    fun submitDispatchAssignment() {
        val tank = dispatchStep3.value ?: "Tank_B"
        val car = "DP1086"
        val driver = dispatchStep2.value ?: "林大宏"
        val fab = dispatchStep4.value ?: "F18A"

        // Put a notification in the screen logs or update spots
        showToast("出工開始登記完成！槽車 $car ($tank) 正在從 $fab 前往轉運站")
        resetScanWorkflows()
    }

    fun submitEnterAssignment() {
        val spotId = enterStep4.value ?: "P-06"
        val tank = enterStep3.value ?: "Tank_B"
        val driver = enterStep2.value ?: "林大宏"
        val staff = enterStep1.value ?: "陳建國"

        // Update the parking spot status in our parking spots array
        val updatedSpots = _parkingSpots.value.map {
            if (it.spotId == spotId) {
                ParkingSpot(
                    spotId = it.spotId,
                    status = "滿",
                    matName = "Dev-1",
                    tankNo = tank,
                    carNo = "DP1086",
                    hours = 0.1, // just arrived
                    driver = driver,
                    vendor = "長春化工"
                )
            } else {
                it
            }
        }
        _parkingSpots.value = updatedSpots
        showToast("進場登記完成！槽車已停放於 $spotId 格")
        resetScanWorkflows()
    }

    fun submitExitAssignment() {
        val tankLetter = exitStep3.value ?: "Tank_A"
        val driver = exitStep2.value ?: "劉家慶"

        val targetSpot = _parkingSpots.value.find { it.tankNo.contains(tankLetter) || it.driver.contains(driver) || it.spotId == "P-01" || it.spotId == "P-06" }
        val spotIdToFree = targetSpot?.spotId ?: "P-06"

        // Update the parking spot status to Empty
        val updatedSpots = _parkingSpots.value.map {
            if (it.spotId == spotIdToFree) {
                ParkingSpot(
                    spotId = it.spotId,
                    status = "空",
                    matName = "-",
                    tankNo = "-",
                    carNo = "-",
                    hours = 0.0,
                    driver = "-",
                    vendor = "-"
                )
            } else {
                it
            }
        }
        _parkingSpots.value = updatedSpots
        showToast("離場登記完成！已釋放車格 $spotIdToFree")
        resetScanWorkflows()
    }

    // Overtime alert vendor notification
    fun notifyVendor(spotId: String) {
        val updatedSpots = _parkingSpots.value.map {
            if (it.spotId == spotId) {
                it.copy(lineNotified = true)
            } else {
                it
            }
        }
        _parkingSpots.value = updatedSpots
        showToast("已發送 Line 通知 & 警報嗨賴給車格 $spotId 主管！")
    }

    // Staff check-in / check-out
    fun punchIn(staffId: String) {
        val staff = _staffList.value.find { it.id == staffId || it.name == staffId }
        if (staff != null) {
            val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val time = format.format(Date())
            val record = PunchInRecord(staff.name, staff.role, staff.id, time, "上班")
            _punchRecords.value = listOf(record) + _punchRecords.value
            showToast("✅ ${staff.name} 已打卡上班 — $time")

            // Add to current shifts assignments if not there
            val today = "2026-06-16"
            val updatedShifts = _shifts.value.map {
                if (it.date == today && it.shiftType == "日班") {
                    if (!it.assignedStaff.contains(staff.name)) {
                        it.copy(assignedStaff = it.assignedStaff + staff.name)
                    } else {
                        it
                    }
                } else {
                    it
                }
            }
            _shifts.value = updatedShifts
        } else {
            showToast("找不到員工條碼: $staffId")
        }
    }

    fun punchOut(staffId: String) {
        val staff = _staffList.value.find { it.id == staffId || it.name == staffId }
        if (staff != null) {
            val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val time = format.format(Date())
            val record = PunchInRecord(staff.name, staff.role, staff.id, time, "下班")
            _punchRecords.value = listOf(record) + _punchRecords.value
            showToast("✅ ${staff.name} 已打卡下班 — $time")
        } else {
            showToast("找不到員工條碼: $staffId")
        }
    }

    // Staff roster operations
    fun addStaffMember(name: String, id: String, role: String, phone: String) {
        val newMember = StaffMember(id, name, role, phone = phone)
        _staffList.value = _staffList.value + newMember
        showToast("成功新增員工: $name ($id)")
    }

    fun batchImportStaff(items: List<StaffMember>) {
        _staffList.value = _staffList.value + items
        showToast("成功批次載入 ${items.size} 筆員工資料！")
    }

    // Assignment Calendar updates
    fun assignShift(date: String, shiftType: String, staffNames: List<String>) {
        val existing = _shifts.value.find { it.date == date && it.shiftType == shiftType }
        val updated = if (existing != null) {
            _shifts.value.map {
                if (it.date == date && it.shiftType == shiftType) {
                    it.copy(assignedStaff = staffNames)
                } else {
                    it
                }
            }
        } else {
            _shifts.value + ShiftAssignment(date, shiftType, staffNames)
        }
        _shifts.value = updated
        showToast("已更新 $date $shiftType 排班名單")
    }
}
