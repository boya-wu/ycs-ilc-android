package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.StaffMember
import kotlinx.coroutines.delay

// --- OVERTIME ALARM PANEL (Screen 6) ---
@Composable
fun AlarmsScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val spots by viewModel.parkingSpots.collectAsState()
    val alarmSpots = spots.filter { it.status == "滿" && it.hours > 8.0 }
        .sortedByDescending { it.hours }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "車道與車格超時通報",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = "超過 8 小時黃色預警; 超過 12 小時紅色超時警報",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
            Text(
                text = "${alarmSpots.size} 個警報",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(Color(0xFFDC2626), RoundedCornerShape(999.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        if (alarmSpots.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircleOutline,
                        contentDescription = "empty",
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("目前無任何超時泊放警報", fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                    Text("轉運站車流狀態維持正常 (All Sync)", fontSize = 12.sp, color = Color(0xFF64748B))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(alarmSpots) { spot ->
                    val isCritical = spot.hours > 12.0
                    val alarmColor = if (isCritical) Color(0xFFDC2626) else Color(0xFFCA8A04)

                    // Infinite transition for Pulse alert card background
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse_card")
                    val pulseAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.85f,
                        targetValue = 1.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = EaseInOutSine),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "card_alpha"
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer { alpha = if (isCritical) pulseAlpha else 1.0f }
                            .border(
                                width = if (isCritical) 2.dp else 1.dp,
                                color = alarmColor.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(20.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCritical) Color(0xFFFFF1F2) else Color(0xFFFFFBEB)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(alarmColor, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = spot.spotId,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 12.sp
                                        )
                                    }
                                    
                                    LedIndicator(color = alarmColor)
                                    
                                    Text(
                                        text = if (isCritical) "CRITICAL 超時過久" else "WARNING 預警提醒",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp,
                                        color = alarmColor
                                    )
                                }

                                Text(
                                    text = "${spot.hours} 小時",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = alarmColor
                                )
                            }

                            HorizontalDivider(color = alarmColor.copy(alpha = 0.15f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("槽車車牌: ${spot.carNo}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
                                    Text("承裝原料: ${spot.matName} (${spot.tankNo})", fontSize = 12.sp, color = Color(0xFF475569))
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("聯絡司機: ${spot.driver}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
                                    Text("所屬承商: ${spot.vendor}", fontSize = 12.sp, color = Color(0xFF475569))
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (spot.lineNotified) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier
                                            .background(Color(0xFFDCFCE7), RoundedCornerShape(999.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Done, null, tint = Color(0xFF16A34A), modifier = Modifier.size(12.dp))
                                        Text("已發送 LINE 警報通知", color = Color(0xFF16A34A), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFFEF3C7), RoundedCornerShape(999.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("待通知 LINE 朝會主管", color = Color(0xFFD97706), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Button(
                                    onClick = { viewModel.notifyVendor(spot.spotId) },
                                    colors = ButtonDefaults.buttonColors(containerColor = alarmColor),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("notify_line_${spot.spotId}")
                                ) {
                                    Icon(Icons.Default.NotificationsActive, null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("通知廠商", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SHIFT CHECK-IN/OUT PANEL (Screen 7) ---
@Composable
fun PunchScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val punches by viewModel.punchRecords.collectAsState()
    val staffList by viewModel.staffList.collectAsState()
    var selectedStaffIdForPunch by remember { mutableStateOf<String?>(null) }
    var punchType by remember { mutableStateOf("IN") } // "IN" or "OUT"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "值班簽到與打卡核備",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color(0xFF1E293B)
        )

        // Row of large buttons: 上班打卡 vs 下班打卡
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable {
                        punchType = "IN"
                        selectedStaffIdForPunch = "9812A" // Default staff
                    }
                    .testTag("punch_in_shortcut_btn")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Fingerprint, null, tint = Color(0xFF16A34A), modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("上班打卡", fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                    Text("Scan Duty IN", fontSize = 11.sp, color = Color(0xFF16A34A))
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE4E6)),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable {
                        punchType = "OUT"
                        selectedStaffIdForPunch = "9812A"
                    }
                    .testTag("punch_out_shortcut_btn")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Fingerprint, null, tint = Color(0xFFDC2626), modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("下班簽約", fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                    Text("Scan Duty OUT", fontSize = 11.sp, color = Color(0xFFDC2626))
                }
            }
        }

        // Active shift staff running roster
        Text("當前在站值班人員 (Terminal Residents)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val activePunchList = punches.filter { it.type == "上班" }.distinctBy { it.id }
                if (activePunchList.isEmpty()) {
                    Text("目前無人員打卡值班中", color = Color(0xFF64748B), modifier = Modifier.padding(4.dp))
                } else {
                    activePunchList.forEach { active ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                LedIndicator(color = Color(0xFF16A34A))
                                Text(active.name, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                Text(
                                    text = active.role,
                                    fontSize = 11.sp,
                                    color = Color(0xFF1460A5),
                                    modifier = Modifier
                                        .background(Color(0xFFEFF6FF), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = "進站時間: ${active.time}",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Historic punch records scrolling
        Text("值班申報歷史日誌 (Historical Logs)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(punches) { r ->
                val workedIn = r.type == "上班"
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(r.name, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                Text(
                                    text = r.type,
                                    color = if (workedIn) Color(0xFF16A34A) else Color(0xFFDC2626),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp
                                )
                            }
                            Text("編號 ID: ${r.id} | ${r.role}", fontSize = 11.sp, color = Color(0xFF64748B))
                        }
                        Text(
                            text = r.time,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569)
                        )
                    }
                }
            }
        }
    }

    // Camera scanner prompt
    selectedStaffIdForPunch?.let { id ->
        SimulatedScanner(
            title = if (punchType == "IN") "上班值班打卡掃描" else "下班接退值班打卡",
            instruction = "請對準員工識別證一維條碼辦理上退班簽備",
            defaultOutputCode = id,
            onCodeScanned = { code ->
                if (punchType == "IN") {
                    viewModel.punchIn(code)
                } else {
                    viewModel.punchOut(code)
                }
                selectedStaffIdForPunch = null
            },
            onCancel = { selectedStaffIdForPunch = null }
        )
    }
}

// --- STAFF ROSTER DIRECTORY VIEW (Screen 8) ---
@Composable
fun RosterScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val staffList by viewModel.staffList.collectAsState()
    var searchField by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showImportExcelPreview by remember { mutableStateOf(false) }

    // Dialog inputs
    var inputName by remember { mutableStateOf("") }
    var inputId by remember { mutableStateOf("") }
    var inputRole by remember { mutableStateOf("現場操作") }
    var inputPhone by remember { mutableStateOf("") }

    val filteredList = staffList.filter {
        it.name.contains(searchField) || it.id.contains(searchField) || it.role.contains(searchField)
    }

    // Simulated Import Excel Data Row List
    val excelImportMock = listOf(
        StaffMember("9899E", "趙子龍", "值班主管", phone = "0988-111-222"),
        StaffMember("9877F", "關雲長", "現場操作", phone = "0955-444-555"),
        StaffMember("9866H", "張翼德", "現場操作", phone = "0944-888-999")
    )

    if (showImportExcelPreview) {
        AlertDialog(
            onDismissRequest = { showImportExcelPreview = false },
            title = { Text("CSV / Excel 批次導入預覽 (Bilingual Import Table)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "以下讀取自主管提交對接 CSV 文件，確認無誤後點擊執行寫入存檔：",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                    Card(
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("姓名", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), fontSize = 11.sp)
                                Text("員工編號", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f), fontSize = 11.sp)
                                Text("職責角色", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f), fontSize = 11.sp)
                            }
                            HorizontalDivider()
                            excelImportMock.forEach { member ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(member.name, modifier = Modifier.weight(1f), fontSize = 11.sp)
                                    Text(member.id, modifier = Modifier.weight(1.2f), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                    Text(member.role, modifier = Modifier.weight(1.5f), fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.batchImportStaff(excelImportMock)
                        showImportExcelPreview = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1460A5)),
                    modifier = Modifier.testTag("confirm_excel_import_btn")
                ) {
                    Text("確認並導入")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportExcelPreview = false }) {
                    Text("取消")
                }
            }
        )
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("新增值班員工資料") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { inputName = it },
                        label = { Text("員工姓名") },
                        modifier = Modifier.fillMaxWidth().testTag("add_staff_name")
                    )
                    OutlinedTextField(
                        value = inputId,
                        onValueChange = { inputId = it },
                        label = { Text("員工編號 (ID)") },
                        modifier = Modifier.fillMaxWidth().testTag("add_staff_id")
                    )
                    OutlinedTextField(
                        value = inputPhone,
                        onValueChange = { inputPhone = it },
                        label = { Text("連絡電話") },
                        modifier = Modifier.fillMaxWidth().testTag("add_staff_phone")
                    )
                    
                    // Simple Dropdown/Selection row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("職別角色:", fontSize = 13.sp, color = Color(0xFF64748B))
                        Box(
                            modifier = Modifier
                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                                .clickable {
                                    inputRole = if (inputRole == "現場操作") "值班主管" else "現場操作"
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(inputRole, fontWeight = FontWeight.Bold, color = Color(0xFF1460A5))
                        }
                        Text("(點擊切換)", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputName.isNotBlank() && inputId.isNotBlank()) {
                            viewModel.addStaffMember(inputName, inputId, inputRole, inputPhone)
                            inputName = ""
                            inputId = ""
                            inputPhone = ""
                            showAddDialog = false
                        } else {
                            viewModel.showToast("請填寫姓名及員工編號")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1460A5)),
                    modifier = Modifier.testTag("submit_add_staff_btn")
                ) {
                    Text("確認加入")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("關閉")
                }
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "I-LC 值班通訊錄與管理",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = "配署站所主管與現場技術人員名單",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
                
                Button(
                    onClick = { showImportExcelPreview = true },
                    modifier = Modifier.testTag("excel_batch_import_trigger"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0891B2)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.UploadFile, null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("批次匯入", fontSize = 11.sp)
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchField,
                onValueChange = { searchField = it },
                placeholder = { Text("搜尋姓名、工編或職級...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1460A5),
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("roster_search_field")
            )

            // Scrollable directory list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList) { member ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = member.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = member.role,
                                        fontSize = 10.sp,
                                        color = if (member.role == "值班主管") Color(0xFFD97706) else Color(0xFF1460A5),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .background(
                                                color = if (member.role == "值班主管") Color(0xFFFEF3C7) else Color(0xFFEFF6FF),
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text("內部工編 ID: ${member.id}", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = Color(0xFF64748B))
                                if (member.phone.isNotBlank()) {
                                    Text("聯絡電話: ${member.phone}", fontSize = 12.sp, color = Color(0xFF475569))
                                }
                            }

                            // Active status badge indicator
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                LedIndicator(color = Color(0xFF16A34A))
                                Text("在庫在職", color = Color(0xFF16A34A), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Add Floating Action Button FAB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = Color(0xFF1460A5),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 12.dp, end = 4.dp)
                .testTag("add_staff_fab")
        ) {
            Icon(Icons.Default.Add, "add staff", modifier = Modifier.size(24.dp))
        }
    }
}

// --- SHIFT CALENDAR VIEW (Screen 9) ---
@Composable
fun ScheduleScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val shifts by viewModel.shifts.collectAsState()
    var selectedDate by remember { mutableStateOf("2026-06-16") }
    var showAssignDialog by remember { mutableStateOf(false) }
    var selectedShiftTypeForAssign by remember { mutableStateOf("日班") }

    // Dialog inputs
    var assignInputNames by remember { mutableStateOf("") }

    val daysInJune = (1..30).toList()

    if (showAssignDialog) {
        AlertDialog(
            onDismissRequest = { showAssignDialog = false },
            title = { Text("$selectedDate $selectedShiftTypeForAssign 名冊排班") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("請填寫指派值勤人員姓名 (逗號或空格切分):", fontSize = 12.sp, color = Color(0xFF64748B))
                    OutlinedTextField(
                        value = assignInputNames,
                        onValueChange = { assignInputNames = it },
                        placeholder = { Text("例如: 陳建國, 王志明") },
                        modifier = Modifier.fillMaxWidth().testTag("assign_schedule_names")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val names = assignInputNames.split(Regex("[,\\s]+")).filter { it.isNotBlank() }
                        if (names.isNotEmpty()) {
                            viewModel.assignShift(selectedDate, selectedShiftTypeForAssign, names)
                            assignInputNames = ""
                            showAssignDialog = false
                        } else {
                            viewModel.showToast("請輸入至少一位值勤員工姓名")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1460A5)),
                    modifier = Modifier.testTag("submit_assign_schedule_btn")
                ) {
                    Text("更新指派")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAssignDialog = false }) {
                    Text("關閉")
                }
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "TSMC 廠區轉運站值班排程表",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF1E293B)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "2026 年 6 月 (June 2026)",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = Color(0xFF1E293B)
                    )

                    // Week headings
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val weeksLabels = listOf("日", "一", "二", "三", "四", "五", "六")
                        weeksLabels.forEach { label ->
                            Text(
                                text = label,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Months dates grid
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        // June 2026 starts on Monday (index 1 is Monday). June 1 is Monday.
                        // Sunday week offsets Monday 1 = 1 empty box for week 1 Sunday
                        var dayCounter = 1
                        for (weekRow in 0..4) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                for (dayCol in 0..6) {
                                    val isEmptyCell = (weekRow == 0 && dayCol < 1) || (dayCounter > 30)
                                    
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1.2f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                color = if (!isEmptyCell && "2026-06-%02d".format(dayCounter) == selectedDate) {
                                                    Color(0xFFEFF6FF)
                                                } else {
                                                    Color.Transparent
                                                }
                                            )
                                            .border(
                                                width = if (!isEmptyCell && "2026-06-%02d".format(dayCounter) == selectedDate) {
                                                    1.5.dp
                                                } else {
                                                    0.dp
                                                },
                                                color = Color(0xFF1460A5),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable(enabled = !isEmptyCell) {
                                                selectedDate = "2026-06-%02d".format(dayCounter)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (!isEmptyCell) {
                                            val formattedDate = "2026-06-%02d".format(dayCounter)
                                            val shiftCountOnThisDay = shifts.filter { it.date == formattedDate }.size

                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = dayCounter.toString(),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = Color(0xFF1E293B)
                                                )
                                                if (shiftCountOnThisDay > 0) {
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Box(modifier = Modifier.size(4.dp).background(Color(0xFF0891B2), CircleShape))
                                                        Box(modifier = Modifier.size(4.dp).background(Color(0xFF64748B), CircleShape))
                                                    }
                                                }
                                            }
                                            dayCounter++
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Day shift detail list for checking and assigning
            Text(
                text = "與選定日 $selectedDate 班次配備行程",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF1E293B)
            )

            // Day Shift
            val dayShift = shifts.find { it.date == selectedDate && it.shiftType == "日班" }
            val nightShift = shifts.find { it.date == selectedDate && it.shiftType == "夜班" }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ShiftRow(
                    label = "日班 (Day Shift 07:00-19:00)",
                    staff = dayShift?.assignedStaff ?: listOf("待排定"),
                    colorAccent = Color(0xFF0891B2),
                    onAssign = {
                        selectedShiftTypeForAssign = "日班"
                        assignInputNames = dayShift?.assignedStaff?.joinToString(", ") ?: ""
                        showAssignDialog = true
                    }
                )

                ShiftRow(
                    label = "夜班 (Night Shift 19:00-07:00)",
                    staff = nightShift?.assignedStaff ?: listOf("待排定"),
                    colorAccent = Color(0xFF1E293B),
                    onAssign = {
                        selectedShiftTypeForAssign = "夜班"
                        assignInputNames = nightShift?.assignedStaff?.joinToString(", ") ?: ""
                        showAssignDialog = true
                    }
                )
            }
        }

        // --- Watermark overlay ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    // Slight diagonal watermarking text across center
                }
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "功能開發中 — 待與 User 討論確認規格",
                fontSize = 18.sp,
                color = Color.Red.copy(alpha = 0.15f),
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .graphicsLayer { rotationZ = -20f }
                    .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                    .border(2.dp, Color.Red.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .padding(16.dp)
            )
        }
    }
}

@Composable
fun ShiftRow(
    label: String,
    staff: List<String>,
    colorAccent: Color,
    onAssign: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorAccent
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    staff.forEach { name ->
                        Text(
                            text = name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF1E293B),
                            modifier = Modifier
                                .background(Color(0xFFF1F5F9), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            IconButton(onClick = onAssign, modifier = Modifier.testTag("assign_shift_trigger_btn")) {
                Icon(imageVector = Icons.Default.EditCalendar, contentDescription = "Edit shift", tint = Color(0xFF1460A5))
            }
        }
    }
}
