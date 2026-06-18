package com.example.data

data class StaffMember(
    val id: String,
    val name: String,
    val role: String,
    val status: String = "在職",
    val phone: String = ""
)

data class Driver(
    val name: String,
    val vendor: String,
    val carNo: String
)

data class ParkingSpot(
    val spotId: String,
    val status: String, // "滿" (Occupied), "空" (Empty), "維護" (Maintenance)
    val matName: String,
    val tankNo: String,
    val carNo: String,
    val hours: Double,
    val driver: String,
    val vendor: String,
    val lineNotified: Boolean = false
)

data class ShiftAssignment(
    val date: String, // "yyyy-MM-dd"
    val shiftType: String, // "日班", "夜班"
    val assignedStaff: List<String>
)

data class PunchInRecord(
    val name: String,
    val role: String,
    val id: String,
    val time: String,
    val type: String // "上班", "下班"
)
