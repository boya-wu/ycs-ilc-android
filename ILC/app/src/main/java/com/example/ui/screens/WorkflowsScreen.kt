package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun WorkflowsScreen(
    workflowType: String, // "DISPATCH", "ENTER", "EXIT"
    viewModel: AppViewModel,
    onBackToHome: () -> Unit
) {
    var showScannerForStep by remember { mutableStateOf<Int?>(null) }
    var operationSubmitted by remember { mutableStateOf(false) }

    // Steppers state flows
    val d1 by viewModel.dispatchStep1.collectAsState()
    val d2 by viewModel.dispatchStep2.collectAsState()
    val d3 by viewModel.dispatchStep3.collectAsState()
    val d4 by viewModel.dispatchStep4.collectAsState()

    val en1 by viewModel.enterStep1.collectAsState()
    val en2 by viewModel.enterStep2.collectAsState()
    val en3 by viewModel.enterStep3.collectAsState()
    val en4 by viewModel.enterStep4.collectAsState()

    val ex1 by viewModel.exitStep1.collectAsState()
    val ex2 by viewModel.exitStep2.collectAsState()
    val ex3 by viewModel.exitStep3.collectAsState()

    // Title / Configuration
    val workflowTitle = when (workflowType) {
        "DISPATCH" -> "出工開始作業 (Start Dispatch from Fab)"
        "ENTER" -> "進停車場作業 (Enter Terminal Parking)"
        else -> "廠商離場作業 (Vendor Vehicle Exit)"
    }

    if (operationSubmitted) {
        // Success complete animation page
        SuccessBlastSplash(
            title = "登記完成 (Registry Updated)",
            message = when (workflowType) {
                "DISPATCH" -> "出工申報完成！槽車已安全登記出發回防。"
                "ENTER" -> "進場登記已完成！車位狀態已同步，通知廠商主管派員提調。"
                else -> "離場登記已完成！車格已釋放歸零。"
            },
            onFinished = {
                viewModel.resetScanWorkflows()
                onBackToHome()
            }
        )
    } else {
        Scaffold(
            topBar = {
                OptAppBar(title = workflowTitle, onBack = {
                    viewModel.resetScanWorkflows()
                    onBackToHome()
                })
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8FAFC))
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (workflowType) {
                        "DISPATCH" -> {
                            // Steps layout: 4 steps
                            val currentStep = when {
                                d1 == null -> 1
                                d2 == null -> 2
                                d3 == null -> 3
                                d4 == null -> 4
                                else -> 5
                            }
                            
                            StepperProgressBar(
                                steps = listOf("① ILC值班", "② ILC司機", "③ 槽體", "④ 廠區"),
                                currentStep = currentStep
                            )

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ScanStepRow(
                                    stepNum = 1,
                                    label = "掃描 ILC 值班人員識別條碼",
                                    value = d1,
                                    testTag = "scan_d1_trigger",
                                    onScanTrigger = { showScannerForStep = 1 }
                                )
                                ScanStepRow(
                                    stepNum = 2,
                                    label = "掃描 ILC 司機人員識別條碼",
                                    value = d2,
                                    enabled = d1 != null,
                                    testTag = "scan_d2_trigger",
                                    onScanTrigger = { showScannerForStep = 2 }
                                )
                                ScanStepRow(
                                    stepNum = 3,
                                    label = "掃描化學品槽體標籤條碼",
                                    value = d3,
                                    enabled = d2 != null,
                                    testTag = "scan_d3_trigger",
                                    onScanTrigger = { showScannerForStep = 3 }
                                )
                                ScanStepRow(
                                    stepNum = 4,
                                    label = "掃描裝灌廠區位置條碼",
                                    value = d4,
                                    enabled = d3 != null,
                                    testTag = "scan_d4_trigger",
                                    onScanTrigger = { showScannerForStep = 4 }
                                )
                            }

                            if (d1 != null && d2 != null && d3 != null && d4 != null) {
                                // Confirmation summary list card
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.5.dp, Color(0xFF16A34A)),
                                    shape = RoundedCornerShape(24.dp),
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(18.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF16A34A))
                                            Text("出工申報資料確認 (Bilingual Details)", fontWeight = FontWeight.Bold, color = Color(0xFF16A34A), fontSize = 16.sp)
                                        }
                                        HorizontalDivider(color = Color(0xFFE2E8F0))
                                        SummaryFieldRow("值班主管", d1 ?: "")
                                        SummaryFieldRow("ILC 司機", d2 ?: "")
                                        SummaryFieldRow("槽體代碼", d3 ?: "")
                                        SummaryFieldRow("載回車號", "DP1086")
                                        SummaryFieldRow("出發廠區", d4 ?: "")
                                        SummaryFieldRow("登記時間", "2026-06-16 15:30:12")

                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = { viewModel.resetScanWorkflows() },
                                                modifier = Modifier.weight(1f).testTag("reset_dispatch_btn"),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text("重新掃描")
                                            }
                                            Button(
                                                onClick = { 
                                                    viewModel.submitDispatchAssignment()
                                                    operationSubmitted = true
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1460A5)),
                                                modifier = Modifier.weight(1.5f).testTag("confirm_dispatch_btn"),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text("確認送出", color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        "ENTER" -> {
                            val currentStep = when {
                                en1 == null -> 1
                                en2 == null -> 2
                                en3 == null -> 3
                                en4 == null -> 4
                                else -> 5
                            }
                            StepperProgressBar(
                                steps = listOf("① ILC值班", "② ILC司機", "③ 槽體", "④ 停車格"),
                                currentStep = currentStep
                            )

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ScanStepRow(
                                    stepNum = 1,
                                    label = "掃描 ILC 值班人員識別條碼",
                                    value = en1,
                                    testTag = "scan_en1_trigger",
                                    onScanTrigger = { showScannerForStep = 11 }
                                )
                                ScanStepRow(
                                    stepNum = 2,
                                    label = "掃描 ILC 司機人員識別條碼",
                                    value = en2,
                                    enabled = en1 != null,
                                    testTag = "scan_en2_trigger",
                                    onScanTrigger = { showScannerForStep = 12 }
                                )
                                ScanStepRow(
                                    stepNum = 3,
                                    label = "掃描化工槽體本體條碼",
                                    value = en3,
                                    enabled = en2 != null,
                                    testTag = "scan_en3_trigger",
                                    onScanTrigger = { showScannerForStep = 13 }
                                )
                                ScanStepRow(
                                    stepNum = 4,
                                    label = "掃描地面專屬停車格條碼",
                                    value = en4,
                                    enabled = en3 != null,
                                    testTag = "scan_en4_trigger",
                                    onScanTrigger = { showScannerForStep = 14 }
                                )
                            }

                            if (en1 != null && en2 != null && en3 != null && en4 != null) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.5.dp, Color(0xFF16A34A)),
                                    shape = RoundedCornerShape(24.dp),
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(18.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF16A34A))
                                            Text("進場登記與任務解編完成", fontWeight = FontWeight.Bold, color = Color(0xFF16A34A), fontSize = 16.sp)
                                        }
                                        HorizontalDivider(color = Color(0xFFE2E8F0))
                                        SummaryFieldRow("認證值班", en1 ?: "")
                                        SummaryFieldRow("返站司機", en2 ?: "")
                                        SummaryFieldRow("槽體識別", en3 ?: "")
                                        SummaryFieldRow("定位車格", en4 ?: "")
                                        SummaryFieldRow("任務狀態", "ILC司機解編/工作結束 ✅")

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "⚠️ 系統已自動於 Line 送出提醒通知承攬廠商 (長春化工) 指派司機員至車格 $en4 辦理出車拉櫃作業。",
                                            color = Color(0xFFB45309),
                                            fontSize = 11.sp,
                                            lineHeight = 16.sp,
                                            modifier = Modifier
                                                .background(Color(0xFFFEF3C7), RoundedCornerShape(8.dp))
                                                .padding(10.dp)
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = { viewModel.resetScanWorkflows() },
                                                modifier = Modifier.weight(1f).testTag("reset_enter_btn"),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text("重新掃描")
                                            }
                                            Button(
                                                onClick = {
                                                    viewModel.submitEnterAssignment()
                                                    operationSubmitted = true
                                                },
                                                modifier = Modifier.weight(1.5f).testTag("confirm_enter_btn"),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1460A5)),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text("確認進站", color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        else -> {
                            // "EXIT" - Vendor Driver exit (3 steps)
                            val currentStep = when {
                                ex1 == null -> 1
                                ex2 == null -> 2
                                ex3 == null -> 3
                                else -> 4
                            }
                            StepperProgressBar(
                                steps = listOf("① ILC值班", "② 廠商司機", "③ 槽體"),
                                currentStep = currentStep
                            )

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ScanStepRow(
                                    stepNum = 1,
                                    label = "掃描 ILC 站值班人員識別證",
                                    value = ex1,
                                    testTag = "scan_ex1_trigger",
                                    onScanTrigger = { showScannerForStep = 21 }
                                )
                                ScanStepRow(
                                    stepNum = 2,
                                    label = "掃描廠商司機員身分條碼",
                                    value = ex2,
                                    enabled = ex1 != null,
                                    testTag = "scan_ex2_trigger",
                                    onScanTrigger = { showScannerForStep = 22 }
                                )
                                ScanStepRow(
                                    stepNum = 3,
                                    label = "載離化學品槽體本體識別條碼",
                                    value = ex3,
                                    enabled = ex2 != null,
                                    testTag = "scan_ex3_trigger",
                                    onScanTrigger = { showScannerForStep = 23 }
                                )
                            }

                            if (ex1 != null && ex2 != null && ex3 != null) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.5.dp, Color(0xFF16A34A)),
                                    shape = RoundedCornerShape(24.dp),
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(18.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF16A34A))
                                            Text("離場與車格釋放確認", fontWeight = FontWeight.Bold, color = Color(0xFF16A34A), fontSize = 16.sp)
                                        }
                                        HorizontalDivider(color = Color(0xFFE2E8F0))
                                        SummaryFieldRow("認證值班", ex1 ?: "")
                                        SummaryFieldRow("承攬承商", ex2 ?: "")
                                        SummaryFieldRow("提取槽體", ex3 ?: "")
                                        SummaryFieldRow("釋放原格", "P-06 (釋放並同步歸空) ✅")
                                        SummaryFieldRow("對位時間", "2026-06-16 18:10:05")
                                        SummaryFieldRow("承載待置時間", "2.4 小時 (無異常)")

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = { viewModel.resetScanWorkflows() },
                                                modifier = Modifier.weight(1f).testTag("reset_exit_btn"),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text("重新掃描")
                                            }
                                            Button(
                                                onClick = {
                                                    viewModel.submitExitAssignment()
                                                    operationSubmitted = true
                                                },
                                                modifier = Modifier.weight(1.5f).testTag("confirm_exit_btn"),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1460A5)),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text("簽出離場", color = Color.White)
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
    }

    // Interactive camera dialogue scanners
    showScannerForStep?.let { step ->
        val title = when (step) {
            1 -> "Step 1: ILC 值班主管識別証條碼"
            2 -> "Step 2: ILC 自營司機身分條碼"
            3 -> "Step 3: 槽體一維代碼標籤"
            4 -> "Step 4: TSMC F18A 閘室位置代碼"
            11 -> "Step 1: ILC 值班核驗識別証二維碼"
            12 -> "Step 2: ILC 返廠司機條碼驗證"
            13 -> "Step 3: 槽體編號條碼比對"
            14 -> "Step 4: 停車格 P-06 地錨二維條碼"
            21 -> "Step 1: ILC 值班驗證條碼"
            22 -> "Step 2: 廠商提車主管身分條碼"
            else -> "Step 3: 出場槽體二維碼"
        }
        val defaultVal = when (step) {
            1, 11, 21 -> "陳建國 (9812A主管)"
            2, 12 -> "林大宏 (自營司機)"
            3, 13 -> "Tank_B (裝載: Dev-1)"
            23 -> "Tank_A (裝載: Dev-1)"
            4 -> "F18A Phase P1 灌充站"
            14 -> "P-06"
            22 -> "劉家慶 (長春化工)"
            else -> "Tank_A"
        }

        SimulatedScanner(
            title = title,
            instruction = "請在對準框內刷讀 PDA 條碼識別標籤 (1.5s 內自動對焦)",
            defaultOutputCode = defaultVal,
            onCodeScanned = { code ->
                when (step) {
                    1 -> viewModel.dispatchStep1.value = code
                    2 -> viewModel.dispatchStep2.value = code
                    3 -> viewModel.dispatchStep3.value = code
                    4 -> viewModel.dispatchStep4.value = code
                    11 -> viewModel.enterStep1.value = code
                    12 -> viewModel.enterStep2.value = code
                    13 -> viewModel.enterStep3.value = code
                    14 -> viewModel.enterStep4.value = code
                    21 -> viewModel.exitStep1.value = code
                    22 -> viewModel.exitStep2.value = code
                    23 -> viewModel.exitStep3.value = code
                }
                showScannerForStep = null
            },
            onCancel = { showScannerForStep = null }
        )
    }
}

// --- Stepper Sub Views ---

@Composable
fun StepperProgressBar(
    steps: List<String>,
    currentStep: Int
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "掃描進度 (Sequential Scanning)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
                Text(
                    text = "$currentStep / ${steps.size + 1} 階段",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = Color(0xFF1460A5),
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.forEachIndexed { index, label ->
                    val isDone = index + 1 < currentStep
                    val isActive = index + 1 == currentStep
                    
                    val color = when {
                        isDone -> Color(0xFF16A34A)
                        isActive -> Color(0xFF1460A5)
                        else -> Color(0xFFCBD5E1)
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium,
                            color = color,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScanStepRow(
    stepNum: Int,
    label: String,
    value: String?,
    enabled: Boolean = true,
    testTag: String,
    onScanTrigger: () -> Unit
) {
    val completed = value != null

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (completed) Color(0xFFF0FDF4) else if (enabled) Color.White else Color(0xFFF1F5F9)
        ),
        border = BorderStroke(
            width = if (completed) 1.5.dp else 1.dp,
            color = if (completed) Color(0xFF86EFAC) else if (enabled) Color(0xFFE2E8F0) else Color(0xFFE2E8F0)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            color = if (completed) Color(0xFF16A34A) else if (enabled) Color(0xFF1460A5) else Color(0xFF94A3B8),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (completed) {
                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        Text(
                            text = stepNum.toString(),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column {
                    Text(
                        text = label,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = if (enabled) Color(0xFF1E293B) else Color(0xFF94A3B8)
                    )
                    value?.let {
                        Text(
                            text = "✅ 讀取值: $it",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF16A34A)
                        )
                    }
                }
            }

            if (!completed) {
                Button(
                    onClick = onScanTrigger,
                    enabled = enabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1460A5)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .testTag(testTag)
                        .padding(start = 8.dp)
                ) {
                    Icon(Icons.Default.QrCodeScanner, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("掃指", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun SummaryFieldRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
        Text(text = value, fontSize = 13.sp, color = Color(0xFF1E293B), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}

// --- Success Animated Particle Checkmark Splash ---
@Composable
fun SuccessBlastSplash(
    title: String,
    message: String,
    onFinished: () -> Unit
) {
    var animTriggered by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animTriggered = true
        delay(2500)
        onFinished()
    }

    val scale by animateFloatAsState(
        targetValue = if (animTriggered) 1.2f else 0.4f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "bounce"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Visual green check circle
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
                    .background(Color(0xFFDCFCE7), CircleShape)
                    .border(2.dp, Color(0xFF16A34A), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF16A34A),
                    modifier = Modifier.size(68.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = title,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                color = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = message,
                fontSize = 14.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            CircularProgressIndicator(
                color = Color(0xFF16A34A),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("正在返回控制台...", fontSize = 11.sp, color = Color(0xFF94A3B8))
        }
    }
}

// Custom Extension to make scale float readable
private fun Modifier.scale(scale: Float): Modifier = graphicsLayer {
    this.scaleX = scale
    this.scaleY = scale
}

// --- Custom Local Toolbar ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptAppBar(
    title: String,
    onBack: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF1E293B)
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack, modifier = Modifier.testTag("back_toolbar")) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "back")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
    )
}
