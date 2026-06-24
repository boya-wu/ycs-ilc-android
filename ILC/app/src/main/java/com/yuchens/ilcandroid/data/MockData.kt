package com.yuchens.ilcandroid.data

object MockData {

    val staffList = listOf(
        StaffMember("H5406340", "吳柏亞", "值班主管", "0912-345-678"),
        StaffMember("H5406341", "王志明", "現場操作", "0923-456-789"),
        StaffMember("H5406342", "李明哲", "值班主管", "0934-567-890"),
        StaffMember("H5406343", "張家豪", "現場操作", "0945-678-901")
    )

    val drivers = listOf(
        Driver("D001", "林大宏", "ILC", "DP1086"),
        Driver("D002", "黃世賢", "ILC", "CCPU1619"),
        Driver("D003", "陳俊生", "ILC", "TH1032")
    )

    val vendorDrivers = listOf(
        Driver("V001", "劉家慶", "長春化工", "DP1275"),
        Driver("V002", "蔡志強", "關東鑫林", "SUNU91"),
        Driver("V003", "賴建明", "台灣巴斯夫", "970303-5")
    )

    val parkingSpots = mutableListOf(
        ParkingSpot("P-01", "滿", "Dev-1", "Tank_B", "DP1086", 14.5, "林大宏", "長春化工"),
        ParkingSpot("P-02", "滿", "H2O2_31%", "Tank_B", "CCPU161902-2", 9.2, "黃世賢", "台聚化工"),
        ParkingSpot("P-03", "空", "-", "-", "-", 0.0, "-", "-"),
        ParkingSpot("P-04", "滿", "Thin-1", "Tank_A", "TH1032A", 4.8, "陳俊生", "東應化"),
        ParkingSpot("P-05", "維護", "-", "-", "-", 0.0, "-", "-"),
        ParkingSpot("P-06", "滿", "Dev-1", "Tank_A", "DP1275", 2.1, "劉家慶", "長春化工"),
        ParkingSpot("P-07", "滿", "HF25%", "Tank_A", "SUNU9105510F", 6.8, "蔡志強", "關東鑫林"),
        ParkingSpot("P-08", "空", "-", "-", "-", 0.0, "-", "-")
    )

    val tsmcFabs = listOf("F6", "F14A", "F14B", "F18A P1", "F18B")
    val tankNos = listOf("Tank_A", "Tank_B", "Tank_C")

    fun findStaff(id: String): StaffMember? =
        staffList.find { it.id.equals(id, true) || it.name == id }

    fun findDriver(id: String): Driver? =
        drivers.find { it.id.equals(id, true) || it.name == id }

    fun findVendorDriver(id: String): Driver? =
        vendorDrivers.find { it.id.equals(id, true) || it.name == id }

    fun resolveStaffLabel(id: String): String {
        val s = findStaff(id)
        return if (s != null) "${s.name} (${s.id})" else id
    }

    fun resolveDriverLabel(id: String): String {
        val d = findDriver(id)
        return if (d != null) "${d.name} — ${d.carNo}" else id
    }

    fun resolveVendorLabel(id: String): String {
        val d = findVendorDriver(id)
        return if (d != null) "${d.name} — ${d.vendor}" else id
    }

    fun resolveTankLabel(id: String): String {
        val tank = tankNos.find { it.equals(id, true) } ?: id
        return tank
    }

    fun resolveFabLabel(id: String): String = id

    fun resolveSpotLabel(id: String): String {
        val spot = parkingSpots.find { it.spotId.equals(id, true) }
        return if (spot != null) "${spot.spotId} (${spot.status})" else id
    }
}
