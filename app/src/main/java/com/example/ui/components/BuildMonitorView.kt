package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ActivityCategory
import com.example.data.model.BuildArtifactInfo
import com.example.data.model.JobStepInfo
import com.example.data.model.WorkflowJobInfo
import com.example.data.model.WorkflowRunInfo
import com.example.ui.theme.*
import com.example.util.AppLogger
import com.example.util.DevelopmentActivityLogManager
import com.example.util.GitHubApiAutomation
import com.example.util.OtaUpdateManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BuildMonitorView(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    var runs by remember { mutableStateOf<List<WorkflowRunInfo>>(emptyList()) }
    var artifacts by remember { mutableStateOf<List<BuildArtifactInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var isTriggering by remember { mutableStateOf(false) }
    var isLivePolling by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Runs, 1: Artifacts
    var statusMessage by remember { mutableStateOf<String?>(null) }

    // Job Details & Logs modal state
    var selectedRunForJobs by remember { mutableStateOf<WorkflowRunInfo?>(null) }
    var jobsList by remember { mutableStateOf<List<WorkflowJobInfo>>(emptyList()) }
    var isLoadingJobs by remember { mutableStateOf(false) }
    var activeJobLogs by remember { mutableStateOf<Pair<String, String>?>(null) } // Pair(JobName, LogsText)
    var isLoadingLogs by remember { mutableStateOf(false) }

    fun refreshData(silent: Boolean = false) {
        if (!silent) isLoading = true
        coroutineScope.launch {
            if (!silent) {
                AppLogger.log(context, "Consultando monitor CI/CD de GitHub Actions", tag = "MONITOR")
            }
            val fetchedRuns = GitHubApiAutomation.getWorkflowRunsList(context, limit = 10)
            val fetchedArtifacts = GitHubApiAutomation.getRecentArtifactsList(context)

            runs = fetchedRuns
            artifacts = fetchedArtifacts
            if (!silent) isLoading = false
            statusMessage = if (fetchedRuns.isEmpty() && fetchedArtifacts.isEmpty()) {
                "No se encontraron ejecuciones o falta configurar el Token en Control CI/CD."
            } else {
                null
            }
        }
    }

    // Auto-polling loop when active or running build detected
    LaunchedEffect(isLivePolling) {
        while (isLivePolling) {
            refreshData(silent = true)
            delay(10000)
        }
    }

    LaunchedEffect(Unit) {
        refreshData()
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
                            .background(LavenderPrimary.copy(alpha = 0.2f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Monitor CI/CD & Artefactos", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 15.sp)
                        Text("GitHub Actions Live REST Pipeline", fontSize = 11.sp, color = TextGrayMuted)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { isLivePolling = !isLivePolling },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isLivePolling) Icons.Default.Sensors else Icons.Default.SensorsOff,
                            contentDescription = "Monitoreo en vivo",
                            tint = if (isLivePolling) MintSpeed else TextGrayMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { refreshData() },
                        enabled = !isLoading,
                        modifier = Modifier.size(36.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = LavenderPrimary, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = LavenderPrimary, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action banner: Quick Trigger + Live Status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black.copy(alpha = 0.25f))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isLivePolling) MintSpeed else LavenderPrimary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isLivePolling) "Monitor en vivo activo (10s)" else "Sondeo manual",
                        fontSize = 11.sp,
                        color = TextWhite,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = {
                        isTriggering = true
                        coroutineScope.launch {
                            AppLogger.log(context, "Disparando workflow CI/CD desde Monitor", tag = "MONITOR")
                            val res = GitHubApiAutomation.triggerWorkflowDispatch(context, workflowFileName = "main.yml")
                            isTriggering = false
                            if (res.success) {
                                isLivePolling = true
                                Toast.makeText(context, "Pipeline iniciado con éxito en GitHub", Toast.LENGTH_SHORT).show()
                                DevelopmentActivityLogManager.logActivity(
                                    context = context,
                                    title = "Disparo de Pipeline CI/CD (main.yml)",
                                    description = "Se envió un POST a GitHub REST API para compilar y generar APKs con versionado.",
                                    category = ActivityCategory.CICD,
                                    agentTag = "BuildMonitor"
                                )
                                delay(2000)
                                refreshData(silent = true)
                            } else {
                                Toast.makeText(context, res.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberEnergy.copy(alpha = 0.2f)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    enabled = !isTriggering,
                    modifier = Modifier.height(34.dp)
                ) {
                    if (isTriggering) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = AmberEnergy, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = AmberEnergy, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Compilar", color = AmberEnergy, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sub-tabs: Runs vs Artifacts
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.3f))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedTab == 0) LavenderPrimary else Color.Transparent)
                        .clickable { selectedTab = 0 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Ejecuciones (${runs.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == 0) DeepPurpleOnPrimary else TextGrayLight
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedTab == 1) LavenderPrimary else Color.Transparent)
                        .clickable { selectedTab = 1 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Artefactos (${artifacts.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == 1) DeepPurpleOnPrimary else TextGrayLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            statusMessage?.let { msg ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(AmberEnergy.copy(alpha = 0.15f))
                        .padding(10.dp)
                ) {
                    Text(text = msg, color = AmberEnergy, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Tab Content
            if (selectedTab == 0) {
                // Workflow Runs List
                if (runs.isEmpty() && !isLoading) {
                    Text(
                        text = "No hay registros de ejecuciones disponibles.",
                        fontSize = 12.sp,
                        color = TextGrayMuted,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        runs.forEach { run ->
                            WorkflowRunItem(
                                run = run,
                                onOpenUrl = { url ->
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                },
                                onViewJobs = {
                                    selectedRunForJobs = run
                                    isLoadingJobs = true
                                    coroutineScope.launch {
                                        jobsList = GitHubApiAutomation.getWorkflowRunJobs(context, run.id)
                                        isLoadingJobs = false
                                    }
                                }
                            )
                        }
                    }
                }
            } else {
                // Artifacts & Releases Assets List
                if (artifacts.isEmpty() && !isLoading) {
                    Text(
                        text = "No hay artefactos de compilación disponibles.",
                        fontSize = 12.sp,
                        color = TextGrayMuted,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        artifacts.forEach { artifact ->
                            ArtifactItem(
                                artifact = artifact,
                                onDownload = {
                                    if (artifact.downloadUrl.isNotBlank()) {
                                        coroutineScope.launch {
                                            Toast.makeText(context, "Iniciando descarga: ${artifact.name}", Toast.LENGTH_SHORT).show()
                                            AppLogger.log(context, "Iniciando descarga de artefacto: ${artifact.name}", tag = "MONITOR")
                                            val res = OtaUpdateManager.downloadAndPrepareInstall(context, artifact.downloadUrl, artifact.name)
                                            if (res.isSuccess) {
                                                val file = res.getOrNull()
                                                if (file != null) {
                                                    OtaUpdateManager.promptInstallApk(context, file)
                                                }
                                            } else {
                                                Toast.makeText(context, "Error en descarga: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Dialog for Jobs and Real Logs
    selectedRunForJobs?.let { run ->
        Dialog(onDismissRequest = {
            selectedRunForJobs = null
            activeJobLogs = null
        }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ElegantCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantCardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Detalles de Ejecución #${run.runNumber}", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 15.sp)
                            Text(run.name, color = LavenderPrimary, fontSize = 12.sp)
                        }
                        IconButton(onClick = {
                            selectedRunForJobs = null
                            activeJobLogs = null
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextWhite)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (isLoadingJobs) {
                        Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = LavenderPrimary)
                        }
                    } else if (jobsList.isEmpty()) {
                        Text("No se encontraron jobs o la ejecución está en cola.", color = TextGrayMuted, fontSize = 12.sp)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            jobsList.forEach { job ->
                                JobItemCard(
                                    job = job,
                                    onViewLogs = {
                                        isLoadingLogs = true
                                        coroutineScope.launch {
                                            val logs = GitHubApiAutomation.getJobLogsText(context, job.id)
                                            activeJobLogs = Pair(job.name, logs)
                                            isLoadingLogs = false
                                        }
                                    }
                                )
                            }
                        }
                    }

                    activeJobLogs?.let { (jobName, logs) ->
                        Spacer(modifier = Modifier.height(14.dp))
                        Divider(color = Color.White.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Logs en Vivo: $jobName", fontWeight = FontWeight.Bold, color = MintSpeed, fontSize = 12.sp)
                            IconButton(onClick = {
                                clipboardManager.setText(AnnotatedString(logs))
                                Toast.makeText(context, "Logs copiados al portapapeles", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copiar logs", tint = MintSpeed, modifier = Modifier.size(16.dp))
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Black.copy(alpha = 0.5f))
                                .padding(8.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = logs.takeLast(6000),
                                color = TextGrayLight,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun JobItemCard(
    job: WorkflowJobInfo,
    onViewLogs: () -> Unit
) {
    val isSuccess = job.conclusion == "success"
    val isFailure = job.conclusion == "failure"
    val isRunning = job.status == "in_progress"

    val statusColor = when {
        isSuccess -> MintSpeed
        isFailure -> RoseAccent
        isRunning -> AmberEnergy
        else -> TextGrayLight
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.3f))
            .border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(job.name, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Button(
                    onClick = onViewLogs,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = statusColor.copy(alpha = 0.2f)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("Ver Logs", color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (job.steps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    job.steps.take(6).forEach { step ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val stepColor = when (step.conclusion) {
                                "success" -> MintSpeed
                                "failure" -> RoseAccent
                                else -> if (step.status == "in_progress") AmberEnergy else TextGrayMuted
                            }
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(stepColor))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(step.name, color = TextGrayLight, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkflowRunItem(
    run: WorkflowRunInfo,
    onOpenUrl: (String) -> Unit,
    onViewJobs: () -> Unit
) {
    val isSuccess = run.conclusion == "success"
    val isFailure = run.conclusion == "failure"
    val isRunning = run.status == "in_progress" || run.status == "queued"

    val statusColor = when {
        isSuccess -> MintSpeed
        isFailure -> RoseAccent
        isRunning -> AmberEnergy
        else -> TextGrayLight
    }

    val statusText = when {
        isSuccess -> "EXITOSO"
        isFailure -> "FALLIDO"
        isRunning -> if (run.status == "queued") "EN COLA" else "COMPILANDO"
        else -> run.conclusion.uppercase()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.Black.copy(alpha = 0.25f))
            .border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
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
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "#${run.runNumber} ${run.name}",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = statusText,
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onViewJobs,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Terminal, contentDescription = "Ver Logs", tint = LavenderPrimary, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = run.displayTitle,
                color = TextGrayLight,
                fontSize = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Rama: ${run.branch} (${run.commitSha})",
                    color = TextGrayMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Por: ${run.author}",
                    color = TextGrayMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun ArtifactItem(
    artifact: BuildArtifactInfo,
    onDownload: () -> Unit
) {
    val sizeMb = if (artifact.sizeInBytes > 0) {
        String.format("%.1f MB", artifact.sizeInBytes / (1024.0 * 1024.0))
    } else {
        "Nube"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.Black.copy(alpha = 0.25f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = if (artifact.name.endsWith(".apk", ignoreCase = true)) Icons.Default.Android else Icons.Default.FolderZip,
                    contentDescription = null,
                    tint = LavenderPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = artifact.name,
                        color = TextWhite,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${artifact.source} • $sizeMb",
                        color = TextGrayMuted,
                        fontSize = 10.sp
                    )
                }
            }

            Button(
                onClick = onDownload,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MintSpeed.copy(alpha = 0.2f)),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = "Descargar", tint = MintSpeed, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Obtener", color = MintSpeed, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
    }
}
