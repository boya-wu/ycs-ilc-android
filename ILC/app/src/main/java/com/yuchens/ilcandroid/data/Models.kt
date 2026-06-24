package com.yuchens.ilcandroid.data

enum class UserRole(val key: String) {
    STAFF("STAFF"),
    DRIVER("DRIVER"),
    VENDOR("VENDOR");

    companion object {
        fun fromKey(key: String): UserRole? = entries.find { it.key == key }
    }
}

data class StaffMember(
    val id: String,
    val name: String,
    val role: String,
    val phone: String = ""
)

data class Driver(
    val id: String,
    val name: String,
    val vendor: String,
    val carNo: String
)

data class ParkingSpot(
    val spotId: String,
    val status: String,
    val matName: String,
    val tankNo: String,
    val carNo: String,
    val hours: Double,
    val driver: String,
    val vendor: String
)

data class PunchInRecord(
    val name: String,
    val role: String,
    val id: String,
    val time: String,
    val type: String
)

enum class WorkflowType { DISPATCH, ENTER, EXIT }

enum class Screen { HOME, PUNCH, DISPATCH, ENTER, EXIT }
