package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TestCaseResult
import com.example.data.model.TestSuiteSummary
import com.example.ui.theme.*
import com.example.util.AppLogger
import com.example.util.TestRunner
import kotlinx.coroutines.launch

@Composable
fun TestRunnerCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var testSummary by remember { mutableStateOf<TestSuiteSummary?>(null) }
    var isRunningTests by remember { mutableStateOf(false) }
    var expandedTestIndex by remember { mutableStateOf<Int?>(null) }

    fun runTests() {
        if (isRunningTests) return
        isRunningTests = true
        coroutineScope.launch {
            AppLogger.log(context, "Lanzando ejecución interactiva de TestRunner", tag = "TEST_RUNNER")
            val summary = TestRunner.executeHealthCheckSuite(context)
            testSummary = summary
            isRunningTests = false
        }
    }

    LaunchedEffect(Unit) {
        // Run initial health check automatically
        runTests()
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ElegantCard),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantCardBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color(0xFF4CAF50).copy(alpha = 0.2f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.FactCheck, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Test Runner & Health Check", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 15.sp)
                        Text("Prevención de Regresiones Roborazzi/JUnit", fontSize = 11.sp, color = TextGrayMuted)
                    }
                }

                Button(
                    onClick = { runTests() },
                    enabled = !isRunningTests,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MintSpeed.copy(alpha = 0.2f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    if (isRunningTests) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = MintSpeed, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MintSpeed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ejecutar", color = MintSpeed, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Score Summary Banner
            testSummary?.let { summary ->
                val healthPercent = if (summary.totalTests > 0) {
                    (summary.passedTests * 100) / summary.totalTests
                } else 100

                val isHealthy = summary.failedTests == 0
                val bannerColor = if (isHealthy) MintSpeed else RoseAccent

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(bannerColor.copy(alpha = 0.12f))
                        .border(1.dp, bannerColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isHealthy) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = bannerColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isHealthy) "ESTADO: SALUDABLE ($healthPercent%)" else "REGRESIONES DETECTADAS ($healthPercent%)",
                                    color = bannerColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${summary.passedTests}/${summary.totalTests} pruebas aprobadas • ${summary.durationMs}ms",
                                color = TextGrayLight,
                                fontSize = 11.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(bannerColor.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isHealthy) "PASSED" else "FAILED",
                                color = bannerColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Test cases list
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    summary.results.forEachIndexed { index, testCase ->
                        val isExpanded = expandedTestIndex == index
                        val itemColor = if (testCase.passed) MintSpeed else RoseAccent

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.25f))
                                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                .clickable {
                                    expandedTestIndex = if (isExpanded) null else index
                                }
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(itemColor)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = testCase.name,
                                                color = TextWhite,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = testCase.suite,
                                                color = TextGrayMuted,
                                                fontSize = 9.sp
                                            )
                                        }
                                    }

                                    Text(
                                        text = "${testCase.durationMs}ms",
                                        color = TextGrayMuted,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = testCase.message,
                                    color = if (testCase.passed) TextGrayLight else RoseAccent,
                                    fontSize = 10.sp
                                )

                                AnimatedVisibility(visible = isExpanded && testCase.details.isNotBlank()) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 6.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.Black.copy(alpha = 0.3f))
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = testCase.details,
                                            color = TextGrayLight,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } ?: run {
                if (isRunningTests) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MintSpeed, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ejecutando suite de pruebas y validaciones...", color = TextGrayLight, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
