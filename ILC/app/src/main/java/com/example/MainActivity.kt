package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val currentRole by viewModel.currentRole.collectAsState()
                val activeTab by viewModel.activeTab.collectAsState()
                val name by viewModel.currentUserName.collectAsState()

                // State to trigger temporary workflow step-scanners
                var activeWorkflow by remember { mutableStateOf<String?>(null) } // "DISPATCH", "ENTER", "EXIT"

                // Snackbar Notifications Support
                val toastMsg by viewModel.toastMessage.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }
                LaunchedEffect(toastMsg) {
                    toastMsg?.let {
                        snackbarHostState.showSnackbar(it)
                        viewModel.clearToast()
                    }
                }

                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    if (currentRole == null) {
                        // User needs to scan badge to login
                        LoginScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                    } else if (activeWorkflow != null) {
                        // Active 3-step or 4-step scanner wizard active
                        WorkflowsScreen(
                            workflowType = activeWorkflow!!,
                            viewModel = viewModel,
                            onBackToHome = { activeWorkflow = null }
                        )
                    } else {
                        // Main Logged-In System Dashboard framework
                        Scaffold(
                            topBar = {
                                TopAppBar(
                                    title = {
                                        Column {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(
                                                            brush = Brush.verticalGradient(
                                                                colors = listOf(Color(0xFF1460A5), Color(0xFF0C4072))
                                                            ),
                                                            shape = RoundedCornerShape(6.dp)
                                                        )
                                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "ILC",
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 11.sp,
                                                        letterSpacing = 0.5.sp
                                                    )
                                                }
                                                Text(
                                                    text = "TSMC ILC 轉運站",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 15.sp,
                                                    style = androidx.compose.ui.text.TextStyle(
                                                        brush = Brush.linearGradient(
                                                            colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF64748B))
                                                        )
                                                    )
                                                )
                                            }
                                            Text(
                                                text = "PDA MOBILE SYSTEM",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp,
                                                color = Color(0xFF64748B),
                                                letterSpacing = 1.2.sp,
                                                modifier = Modifier.padding(start = 2.dp)
                                            )
                                        }
                                    },
                                    actions = {
                                        // Running clock + connection indicator + logout button
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.padding(end = 8.dp)
                                        ) {
                                            ConnectionStatusBar()
                                            LiveClock()
                                            IconButton(
                                                onClick = { viewModel.logout() },
                                                modifier = Modifier.testTag("logout_button")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Logout,
                                                    contentDescription = "logout",
                                                    tint = Color(0xFFDC2626)
                                                )
                                            }
                                        }
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                                )
                            },
                            bottomBar = {
                                // Customized M3 bottom tabs per role
                                NavigationBar(
                                    containerColor = Color.White,
                                    tonalElevation = 8.dp,
                                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                                ) {
                                    val role = currentRole!!
                                    
                                    if (role == "STAFF") {
                                        // STAFF TABS: Home, Alarms, Punch, Roster, Schedules
                                        NavigationBarItem(
                                            selected = activeTab == "HOME",
                                            onClick = { viewModel.selectTab("HOME") },
                                            icon = { Icon(Icons.Default.Home, null) },
                                            label = { Text("首頁", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                            modifier = Modifier.testTag("nav_staff_home")
                                        )
                                        NavigationBarItem(
                                            selected = activeTab == "ALARM",
                                            onClick = { viewModel.selectTab("ALARM") },
                                            icon = { Icon(Icons.Default.Warning, null) },
                                            label = { Text("警報", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                            modifier = Modifier.testTag("nav_staff_alarm")
                                        )
                                        NavigationBarItem(
                                            selected = activeTab == "PUNCH",
                                            onClick = { viewModel.selectTab("PUNCH") },
                                            icon = { Icon(Icons.Default.Fingerprint, null) },
                                            label = { Text("打卡", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                            modifier = Modifier.testTag("nav_staff_punch")
                                        )
                                        NavigationBarItem(
                                            selected = activeTab == "ROSTER",
                                            onClick = { viewModel.selectTab("ROSTER") },
                                            icon = { Icon(Icons.Default.Contacts, null) },
                                            label = { Text("員工", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                            modifier = Modifier.testTag("nav_staff_roster")
                                        )
                                        NavigationBarItem(
                                            selected = activeTab == "SCHEDULE",
                                            onClick = { viewModel.selectTab("SCHEDULE") },
                                            icon = { Icon(Icons.Default.CalendarToday, null) },
                                            label = { Text("排班", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                            modifier = Modifier.testTag("nav_staff_schedule")
                                        )
                                    } else if (role == "DRIVER") {
                                        // DRIVER TABS: Home, Start Dispatch, Enter Parking, Records
                                        NavigationBarItem(
                                            selected = activeTab == "HOME",
                                            onClick = { viewModel.selectTab("HOME") },
                                            icon = { Icon(Icons.Default.Home, null) },
                                            label = { Text("首頁", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                            modifier = Modifier.testTag("nav_driver_home")
                                        )
                                        NavigationBarItem(
                                            selected = false,
                                            onClick = { activeWorkflow = "DISPATCH" },
                                            icon = { Icon(Icons.Default.PlayArrow, null, tint = Color(0xFF1460A5)) },
                                            label = { Text("出工掃碼", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1460A5)) },
                                            modifier = Modifier.testTag("nav_driver_dispatch")
                                        )
                                        NavigationBarItem(
                                            selected = false,
                                            onClick = { activeWorkflow = "ENTER" },
                                            icon = { Icon(Icons.Default.LocalParking, null, tint = Color(0xFF16A34A)) },
                                            label = { Text("入場碼記", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A)) },
                                            modifier = Modifier.testTag("nav_driver_enter")
                                        )
                                        NavigationBarItem(
                                            selected = activeTab == "PUNCH",
                                            onClick = { viewModel.selectTab("PUNCH") },
                                            icon = { Icon(Icons.Default.History, null) },
                                            label = { Text("紀錄", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                            modifier = Modifier.testTag("nav_driver_history")
                                        )
                                    } else {
                                        // VENDOR_DRIVER TABS: Home, Exit Parking, Records
                                        NavigationBarItem(
                                            selected = activeTab == "HOME",
                                            onClick = { viewModel.selectTab("HOME") },
                                            icon = { Icon(Icons.Default.Home, null) },
                                            label = { Text("首頁", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                            modifier = Modifier.testTag("nav_vendor_home")
                                        )
                                        NavigationBarItem(
                                            selected = false,
                                            onClick = { activeWorkflow = "EXIT" },
                                            icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = Color(0xFFD97706)) },
                                            label = { Text("離場作業", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706)) },
                                            modifier = Modifier.testTag("nav_vendor_exit")
                                        )
                                        NavigationBarItem(
                                            selected = activeTab == "PUNCH",
                                            onClick = { viewModel.selectTab("PUNCH") },
                                            icon = { Icon(Icons.Default.History, null) },
                                            label = { Text("歷史紀錄", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                            modifier = Modifier.testTag("nav_vendor_history")
                                        )
                                    }
                                }
                            }
                        ) { mainScaffoldPadding ->
                            // Tab Content wrapper with smooth slide-in page transition animations
                            Box(
                                modifier = Modifier
                                    .padding(mainScaffoldPadding)
                                    .fillMaxSize()
                            ) {
                                AnimatedContent(
                                    targetState = activeTab,
                                    transitionSpec = {
                                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                                                slideOutHorizontally { width -> -width } + fadeOut()
                                    },
                                    label = "tab_transitions"
                                ) { tab ->
                                    when (tab) {
                                        "HOME" -> HomeScreenDashboard(
                                            viewModel = viewModel,
                                            onNavigateToScanWork = { activeWorkflow = it }
                                        )
                                        "ALARM" -> AlarmsScreen(viewModel = viewModel)
                                        "PUNCH" -> PunchScreen(viewModel = viewModel)
                                        "ROSTER" -> RosterScreen(viewModel = viewModel)
                                        "SCHEDULE" -> ScheduleScreen(viewModel = viewModel)
                                        else -> HomeScreenDashboard(
                                            viewModel = viewModel,
                                            onNavigateToScanWork = { activeWorkflow = it }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
