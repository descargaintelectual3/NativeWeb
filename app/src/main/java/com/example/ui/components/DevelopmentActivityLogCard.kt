package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActivityCategory
import com.example.data.model.ActivityLogEntry
import com.example.ui.theme.*
import com.example.util.AppLogger
import com.example.util.DevelopmentActivityLogManager

@Composable
fun DevelopmentActivityLogCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val activities by produceState(
        initialValue = DevelopmentActivityLogManager.getActivities(context),
        context
    ) {
        DevelopmentActivityLogManager.observeActivities(context).collect { list ->
            value = if (list.isNotEmpty()) list else DevelopmentActivityLogManager.getActivities(context)
        }
    }
    var selectedCategory by remember { mutableStateOf(ActivityCategory.ALL) }
    var expandedEntryId by remember { mutableStateOf<String?>(null) }
    var showReportCopiedNotice by remember { mutableStateOf(false) }

    val filterScrollState = rememberScrollState()

    val filteredList = remember(activities, selectedCategory) {
        if (selectedCategory == ActivityCategory.ALL) {
            activities
        } else {
            activities.filter { it.category == selectedCategory }
        }
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
                            .background(AmberEnergy.copy(alpha = 0.2f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.HistoryEdu, contentDescription = null, tint = AmberEnergy, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Historial de Actividad & Reversión", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 15.sp)
                        Text("Auditoría de Acciones y Cambios de Agentes", fontSize = 11.sp, color = TextGrayMuted)
                    }
                }

                IconButton(onClick = {
                    val report = DevelopmentActivityLogManager.generateAuditReport(context)
                    clipboardManager.setText(AnnotatedString(report))
                    AppLogger.log(context, "Informe de auditoría y reversión copiado al portapapeles", tag = "AUDIT")
                    showReportCopiedNotice = true
                }) {
                    Icon(Icons.Default.ContentPasteGo, contentDescription = "Exportar Informe", tint = AmberEnergy)
                }
            }

            AnimatedVisibility(visible = showReportCopiedNotice) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MintSpeed.copy(alpha = 0.15f))
                        .padding(10.dp)
                ) {
                    Text("¡Informe de auditoría copiado al portapapeles!", color = MintSpeed, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Categories
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(filterScrollState),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ActivityCategory.values().forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        onClick = { selectedCategory = cat },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) AmberEnergy.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) AmberEnergy else Color.White.copy(alpha = 0.1f)
                        )
                    ) {
                        Text(
                            text = when (cat) {
                                ActivityCategory.ALL -> "Todos"
                                ActivityCategory.CICD -> "CI/CD & OTA"
                                ActivityCategory.ADB_HARDWARE -> "ADB / HW"
                                ActivityCategory.CORE_ENGINE -> "Núcleo"
                                ActivityCategory.TESTING -> "Pruebas"
                                ActivityCategory.FIXES -> "Correcciones"
                            },
                            color = if (isSelected) AmberEnergy else TextGrayLight,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Activities List
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                filteredList.forEach { entry ->
                    val isExpanded = expandedEntryId == entry.id
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.Black.copy(alpha = 0.25f))
                            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(14.dp))
                            .clickable {
                                expandedEntryId = if (isExpanded) null else entry.id
                            }
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (entry.category) {
                                                    ActivityCategory.CICD -> LavenderPrimary
                                                    ActivityCategory.ADB_HARDWARE -> MintSpeed
                                                    ActivityCategory.CORE_ENGINE -> AmberEnergy
                                                    ActivityCategory.TESTING -> Color(0xFF64B5F6)
                                                    ActivityCategory.FIXES -> RoseAccent
                                                    else -> TextGrayLight
                                                }
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = entry.title,
                                        color = TextWhite,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                }

                                Text(
                                    text = entry.dateString,
                                    color = TextGrayMuted,
                                    fontSize = 10.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = entry.description,
                                color = TextGrayLight,
                                fontSize = 11.sp
                            )

                            AnimatedVisibility(visible = isExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.Black.copy(alpha = 0.3f))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "Agente: @${entry.agentTag}",
                                        color = LavenderPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (entry.affectedFiles.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Archivos modificados:",
                                            color = TextGrayMuted,
                                            fontSize = 10.sp
                                        )
                                        entry.affectedFiles.forEach { f ->
                                            Text(
                                                text = "• $f",
                                                color = TextGrayLight,
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = "Guía de Reversión / Rollback:",
                                        color = AmberEnergy,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = entry.rollbackInstruction,
                                        color = TextWhite,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = {
                                            clipboardManager.setText(
                                                AnnotatedString(
                                                    "Instrucción de Reversión para '${entry.title}':\n${entry.rollbackInstruction}"
                                                )
                                            )
                                            AppLogger.log(context, "Copiada guía de reversión para ${entry.id}", tag = "AUDIT")
                                        },
                                        modifier = Modifier.fillMaxWidth().height(32.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = AmberEnergy.copy(alpha = 0.25f)),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = AmberEnergy, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Copiar Guía de Reversión", color = AmberEnergy, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
