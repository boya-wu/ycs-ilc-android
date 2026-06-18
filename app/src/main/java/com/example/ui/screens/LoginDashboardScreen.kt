package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.map

@Composable
fun LoginScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    var selectedRoleForScan by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // slate-50
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // App Banner Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ILC brand logo badge
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.verticalGradient(listOf(Color(0xFF1460A5), Color(0xFF0C4072))),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "ILC TSMC",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "化學品槽車轉運站",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    fontSize = 26.sp,
                    style = androidx.compose.ui.text.TextStyle(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF64748B))
                        )
                    )
                )
                Text(
                    text = "PDA 行動管理系統",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    style = androidx.compose.ui.text.TextStyle(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF475569), Color(0xFF64748B), Color(0xFF94A3B8))
                        )
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "MOBILE HUB MANAGEMENT SYSTEM",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 1.5.sp
                )
            }

            // Role selection panel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "請選擇您的登入角色 (Select Your Role)",
                    color = Color(0xFF64748B),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp)
                )

                // Role card 1: 值班人員 (Staff)
                RoleCard(
                    title = "ILC 值班人員",
                    description = "轉運站排班值勤、車格监控與異常通知及出入場協調",
                    englishTitle = "ILC On-Duty Staff Hub",
                    icon = Icons.Default.Security,
                    colorAccent = Color(0xFF1460A5),
                    testTag = "role_staff_card",
                    onClick = { selectedRoleForScan = "STAFF" }
                )

                // Role card 2: ILC 司機 (Driver)
                RoleCard(
                    title = "ILC 司機員",
                    description = "廠內空車槽體開回登記、出入停車場掃碼及行車申報",
                    englishTitle = "ILC Terminal Driver",
                    icon = Icons.Default.LocalShipping,
                    colorAccent = Color(0xFF0891B2),
                    testTag = "role_driver_card",
                    onClick = { selectedRoleForScan = "DRIVER" }
                )

                // Role card 3: 廠商司機 (Vendor)
                RoleCard(
                    title = "廠商司機員",
                    description = "承攬化工廠商配車離場掃碼及櫃位車體自主返還申報",
                    englishTitle = "Vendor Chemical Driver",
                    icon = Icons.Default.Person,
                    colorAccent = Color(0xFFCA8A04),
                    testTag = "role_vendor_card",
                    onClick = { selectedRoleForScan = "VENDOR_DRIVER" }
                )
            }

            // Credits Footer
            Column(
                modifier = Modifier.padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Control System v4.3.0",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
                Text(
                    text = "宇辰系統科技 Yu-Chen System Technology",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B)
                )
            }
        }
    }

    // Capture Scan Modal Overlay
    selectedRoleForScan?.let { role ->
        val defaultCode = when (role) {
            "STAFF" -> "9812A"
            "DRIVER" -> "林大宏"
            else -> "劉家慶"
        }
        val scanTitle = when (role) {
            "STAFF" -> "值班主管識別證掃描"
            "DRIVER" -> "ILC 司機員身份掃描"
            else -> "外部承攬廠商登記掃描"
        }
        SimulatedScanner(
            title = scanTitle,
            instruction = "請對準識別證背面一維/二維條碼辦理登入",
            defaultOutputCode = defaultCode,
            onCodeScanned = { code ->
                viewModel.login(role, code)
                selectedRoleForScan = null
            },
            onCancel = { selectedRoleForScan = null }
        )
    }
}

@Composable
fun RoleCard(
    title: String,
    englishTitle: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    colorAccent: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .testTag(testTag),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)), // slate-200
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(18.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Circle Icon accent
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(colorAccent.copy(alpha = 0.08f), CircleShape)
                    .border(1.5.dp, colorAccent.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colorAccent,
                    modifier = Modifier.size(26.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = englishTitle,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 16.sp
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// --- Home Screen Dashboard ---
@Composable
fun HomeScreenDashboard(
    viewModel: AppViewModel,
    onNavigateToScanWork: (String) -> Unit // "DISPATCH", "ENTER", "EXIT"
) {
    val role by viewModel.currentRole.collectAsState()
    val name by viewModel.currentUserName.collectAsState()
    val spots by viewModel.parkingSpots.collectAsState()

    val occupiedCount = spots.count { it.status == "滿" }
    val overtimeCount = spots.count { it.status == "滿" && it.hours > 12.0 }
    val warningCount = spots.count { it.status == "滿" && it.hours > 8.0 && it.hours <= 12.0 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // slate-50
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Notice Alert Marquee System ---
        if (overtimeCount > 0 || warningCount > 0) {
            val marqueeBuilder = StringBuilder()
            spots.filter { it.status == "滿" && it.hours > 8.0 }.forEach {
                marqueeBuilder.append("[⚠️ ${it.spotId}] 槽車 ${it.carNo} (${it.matName}) 司機: ${it.driver} 已超時停放 ${it.hours} 小時，請承商速往派任。   ")
            }
            NoticeMarqueeBanner(text = marqueeBuilder.toString())
        }

        // --- Role Dashboards ---
        when (role) {
            "STAFF" -> {
                // Welcome and on-duty card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f)),
                    border = BorderStroke(1.dp, Color(0x0D0F172A)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color(0xFFEFF6FF), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = Color(0xFF1460A5),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "當前值班人員 On Duty",
                                    color = Color(0xFF64748B),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$name (值班主管)",
                                    color = Color(0xFF1E293B),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp
                                )
                            }
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .background(Color(0xFFECFDF5), RoundedCornerShape(999.dp))
                                .border(1.dp, Color(0xFFA7F3D0), RoundedCornerShape(999.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            LedIndicator(color = Color(0xFF16A34A))
                            Text(
                                text = "ACTIVE",
                                color = Color(0xFF16A34A),
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                // KPIs horizontal row
                Text(
                    text = "即時監控數據 (SCADA KPI)",
                    color = Color(0xFF1E293B),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        KpiIndicatorCard(
                            label = "廠內空桶數",
                            value = "5 / 50 桶",
                            ratio = 0.1f,
                            color = Color(0xFF0891B2)
                        )
                    }
                    item {
                        KpiIndicatorCard(
                            label = "廠內槽車數",
                            value = "4 / 12 輛",
                            ratio = 0.33f,
                            color = Color(0xFF16A34A)
                        )
                    }
                    item {
                        Box(contentAlignment = Alignment.TopEnd) {
                            KpiIndicatorCard(
                                label = "轉運站車位",
                                value = "$occupiedCount / 12 輛",
                                ratio = occupiedCount / 12.0f,
                                color = if (overtimeCount > 0) Color(0xFFDC2626) else Color(0xFF2563EB)
                            )
                            if (overtimeCount > 0) {
                                Text(
                                    text = "$overtimeCount 車超時 >12H",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .offset(y = (-4).dp, x = (-4).dp)
                                        .background(Color(0xFFDC2626), RoundedCornerShape(999.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Quick Actions 2x2 grid
                Text(
                    text = "快速扫碼登記 (PDA Scan Actions)",
                    color = Color(0xFF1E293B),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickActionCard(
                            title = "掃碼出工作業",
                            subtitle = "ILC司機從TSMC載回",
                            icon = Icons.Default.PlayArrow,
                            color = Color(0xFF1460A5),
                            backgroundColor = Color(0xFFEFF6FF),
                            testTag = "scan_dispatch_button",
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToScanWork("DISPATCH") }
                        )
                        QuickActionCard(
                            title = "掃碼入場作業",
                            subtitle = "槽車抵達進停車場",
                            icon = Icons.Default.LocalParking,
                            color = Color(0xFF16A34A),
                            backgroundColor = Color(0xFFECFDF5),
                            testTag = "scan_enter_button",
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToScanWork("ENTER") }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickActionCard(
                            title = "現場離場作業",
                            subtitle = "承攬承商提調離場",
                            icon = Icons.Default.ExitToApp,
                            color = Color(0xFFCA8A04),
                            backgroundColor = Color(0xFFFEF3C7),
                            testTag = "scan_exit_button",
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateToScanWork("EXIT") }
                        )
                        QuickActionCard(
                            title = "值班人員打卡",
                            subtitle = "員工打卡與接班簽核",
                            icon = Icons.Default.Fingerprint,
                            color = Color(0xFF475569),
                            backgroundColor = Color(0xFFF1F5F9),
                            testTag = "duty_punch_button",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.selectTab("PUNCH") }
                        )
                    }
                }
            }

            "DRIVER" -> {
                // ILC Driver Welcome Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFF2580D4).copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.LocalShipping, null, tint = Color(0xFF1460A5))
                            }
                            Column {
                                Text(
                                    text = "林大宏 司機員 — 自營組",
                                    color = Color(0xFF1E293B),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                                Text(
                                    text = "簽到時間: 07:45:11 (已執勤 6.5 小時)",
                                    color = Color(0xFF64748B),
                                    fontSize = 12.sp
                                )
                            }
                        }
                        
                        HorizontalDivider(color = Color(0xFFE2E8F0))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("配車槽體:", fontSize = 13.sp, color = Color(0xFF64748B))
                            Text(
                                text = "Tank_B — DP1086",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF1E293B)
                            )
                        }
                    }
                }

                Text(
                    text = "行車調配管理 (Task Dispatch)",
                    color = Color(0xFF1E293B),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                // Large interactive task card
                QuickActionCard(
                    title = "① 掃碼開始出工作業 (Start Dispatch)",
                    subtitle = "前往 TSMC 廠區領裝灌裝後槽體",
                    icon = Icons.Default.DirectionsRun,
                    color = Color(0xFF1460A5),
                    backgroundColor = Color(0xFFEFF6FF),
                    testTag = "driver_start_dispatch",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onNavigateToScanWork("DISPATCH") }
                )

                QuickActionCard(
                    title = "② 掃碼確認入場停車 (Enter Lot)",
                    subtitle = "返回轉運站並歸回正確車格",
                    icon = Icons.Default.LocalParking,
                    color = Color(0xFF16A34A),
                    backgroundColor = Color(0xFFECFDF5),
                    testTag = "driver_start_enter",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onNavigateToScanWork("ENTER") }
                )
            }

            else -> {
                // Vendor Driver dashboard
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFFCA8A04).copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, null, tint = Color(0xFFCA8A04))
                            }
                            Column {
                                Text(
                                    text = "$name 司機員",
                                    color = Color(0xFF1E293B),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                                Text(
                                    text = "外部承攬廠商: 長春化工 (常駐)",
                                    color = Color(0xFF64748B),
                                    fontSize = 12.sp
                                )
                            }
                        }
                        
                        HorizontalDivider(color = Color(0xFFE2E8F0))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("派任原車格:", fontSize = 13.sp, color = Color(0xFF64748B))
                            Text(
                                text = "P-06 (Dev-1 車頭)",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFFCA8A04)
                            )
                        }
                    }
                }

                Text(
                    text = "化學品提調與離場登記 (Exit Registry)",
                    color = Color(0xFF1E293B),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                QuickActionCard(
                    title = "掃碼離場申報 (Exit Verification)",
                    subtitle = "掃描值班、廠商及槽體條碼，完成核卡離場",
                    icon = Icons.Default.ExitToApp,
                    color = Color(0xFFD97706),
                    backgroundColor = Color(0xFFFEF3C7),
                    testTag = "vendor_exit_action",
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    onClick = { onNavigateToScanWork("EXIT") }
                )
            }
        }
    }
}

// --- Helper Composable Widgets ---

@Composable
fun KpiIndicatorCard(
    label: String,
    value: String,
    ratio: Float,
    color: Color
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(115.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f)),
        border = BorderStroke(1.dp, Color(0x0D0F172A)),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                color = Color(0xFF64748B),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.2.sp
            )
            Text(
                text = value,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = color
            )
            LinearProgressIndicator(
                progress = { ratio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = color,
                trackColor = Color(0xFFF1F5F9)
            )
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    backgroundColor: Color = Color(0xFFF1F5F9),
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .testTag(testTag),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)), // slate-200
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 18.dp, horizontal = 12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(backgroundColor, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon, 
                    contentDescription = null, 
                    tint = color,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                color = color,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                lineHeight = 13.sp
            )
        }
    }
}
