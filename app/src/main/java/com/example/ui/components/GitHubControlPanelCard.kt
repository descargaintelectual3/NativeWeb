package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.GitHubApiAutomation
import kotlinx.coroutines.launch

@Composable
fun GitHubControlPanelCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var token by remember { mutableStateOf(GitHubApiAutomation.getGitHubToken(context)) }
    var owner by remember { mutableStateOf(GitHubApiAutomation.getRepoOwner(context)) }
    var repo by remember { mutableStateOf(GitHubApiAutomation.getRepoName(context)) }
    
    var isTokenVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(LavenderPrimary.copy(alpha = 0.2f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = null,
                        tint = LavenderPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Centro CI/CD Integrado (GitHub Actions)",
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Controla la compilación de APKs directo desde la App",
                        fontSize = 11.sp,
                        color = TextGrayMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Token Input
            OutlinedTextField(
                value = token,
                onValueChange = { 
                    token = it
                    GitHubApiAutomation.saveGitHubToken(context, it)
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Personal Access Token (PAT)", fontSize = 12.sp, color = TextGrayLight) },
                visualTransformation = if (isTokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isTokenVisible = !isTokenVisible }) {
                        Icon(
                            imageVector = if (isTokenVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle Token",
                            tint = LavenderPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LavenderPrimary,
                    unfocusedBorderColor = ElegantCardBorder,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Owner Input
                OutlinedTextField(
                    value = owner,
                    onValueChange = { 
                        owner = it
                        GitHubApiAutomation.saveRepoInfo(context, owner, repo)
                    },
                    modifier = Modifier.weight(1f),
                    label = { Text("Usuario/Org", fontSize = 12.sp, color = TextGrayLight) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LavenderPrimary,
                        unfocusedBorderColor = ElegantCardBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                // Repo Input
                OutlinedTextField(
                    value = repo,
                    onValueChange = { 
                        repo = it
                        GitHubApiAutomation.saveRepoInfo(context, owner, repo)
                    },
                    modifier = Modifier.weight(1f),
                    label = { Text("Repositorio", fontSize = 12.sp, color = TextGrayLight) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LavenderPrimary,
                        unfocusedBorderColor = ElegantCardBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Verify Token
                OutlinedButton(
                    onClick = {
                        isLoading = true
                        statusMessage = "Conectando con GitHub..."
                        coroutineScope.launch {
                            val result = GitHubApiAutomation.verifyToken(token)
                            isSuccess = result.success
                            statusMessage = result.message
                            isLoading = false
                        }
                    },
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LavenderPrimary.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = LavenderPrimary)
                ) {
                    Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Validar Llave", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                // Trigger Build & Auto OTA Polling
                var isPollingForUpdate by remember { mutableStateOf(false) }
                
                Button(
                    onClick = {
                        if (isPollingForUpdate) return@Button
                        
                        isLoading = true
                        statusMessage = "Enviando señal de compilación a GitHub Actions..."
                        com.example.util.AppLogger.log(context, "Iniciando trigger de compilación OTA (Workflow Dispatch)")
                        coroutineScope.launch {
                            val currentVersion = com.example.util.OtaUpdateManager.getCurrentAppVersion(context)
                            val result = GitHubApiAutomation.triggerWorkflowDispatch(context)
                            isSuccess = result.success
                            statusMessage = result.message
                            com.example.util.AppLogger.log(context, "Trigger result: success=${result.success}, msg=${result.message}")
                            
                            if (result.success) {
                                isPollingForUpdate = true
                                var pollAttempt = 0
                                com.example.util.AppLogger.log(context, "Iniciando Polling (esperando finalización de GitHub Actions)...")
                                
                                // Polling loop
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                    while (isPollingForUpdate && pollAttempt < 20) {
                                        kotlinx.coroutines.delay(15000L) // wait 15 sec
                                        pollAttempt++
                                        
                                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                            statusMessage = "Compilando en la nube (Intento $pollAttempt/20)..."
                                        }
                                        
                                        val runStatus = GitHubApiAutomation.getLatestWorkflowRunStatus(context)
                                        com.example.util.AppLogger.log(context, "Polling #$pollAttempt: status=${runStatus.message}, conclusion=${runStatus.details}")
                                        
                                        if (runStatus.message == "completed" && runStatus.details == "success") {
                                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                statusMessage = "¡Compilación exitosa!\nBuscando nueva versión (Release)..."
                                            }
                                            com.example.util.AppLogger.log(context, "GitHub Actions reportó éxito. Buscando Release...")
                                            
                                            // Give GitHub Releases 10 seconds to publish the artifact
                                            kotlinx.coroutines.delay(10000L)
                                            
                                            val updateStatus = com.example.util.OtaUpdateManager.checkForUpdates(context)
                                            if (updateStatus is com.example.util.UpdateStatus.UpdateAvailable) {
                                                if (updateStatus.versionName != currentVersion && updateStatus.versionName != "v$currentVersion") {
                                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                        statusMessage = "Descargando nueva versión OTA: ${updateStatus.versionName}..."
                                                    }
                                                    com.example.util.AppLogger.log(context, "Nueva versión encontrada: ${updateStatus.versionName}. Iniciando descarga...")
                                                    
                                                    val downloadResult = com.example.util.OtaUpdateManager.downloadAndPrepareInstall(context, updateStatus.downloadUrl, updateStatus.versionName)
                                                    
                                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                        if (downloadResult.isSuccess) {
                                                            val file = downloadResult.getOrNull()
                                                            if (file != null) {
                                                                statusMessage = "¡Descarga completada! Iniciando instalación..."
                                                                com.example.util.AppLogger.log(context, "APK descargado exitosamente. Lanzando FileProvider (Instalador Android).")
                                                                com.example.util.OtaUpdateManager.promptInstallApk(context, file)
                                                            }
                                                        } else {
                                                            isSuccess = false
                                                            statusMessage = "Error al descargar la actualización automática."
                                                            com.example.util.AppLogger.log(context, "Error fatal al descargar el APK: ${downloadResult.exceptionOrNull()?.message}")
                                                        }
                                                        isLoading = false
                                                        isPollingForUpdate = false
                                                    }
                                                    break
                                                }
                                            }
                                            
                                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                isLoading = false
                                                isPollingForUpdate = false
                                                statusMessage = "Compilación finalizada, pero no se detectó una nueva versión (ver: $currentVersion)."
                                                com.example.util.AppLogger.log(context, "Compilación finalizada, pero la Release parece ser la misma versión actual ($currentVersion).")
                                            }
                                            break
                                        } else if (runStatus.message == "completed" && runStatus.details == "failure") {
                                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                                isSuccess = false
                                                isLoading = false
                                                isPollingForUpdate = false
                                                statusMessage = "Error: La compilación en GitHub Actions falló."
                                            }
                                            com.example.util.AppLogger.log(context, "Error: El Workflow de GitHub Actions falló internamente.")
                                            break
                                        }
                                    }
                                    
                                    if (isPollingForUpdate) {
                                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                            isLoading = false
                                            isPollingForUpdate = false
                                            statusMessage = "Tiempo de espera agotado. Revisa el estado manualmente más tarde."
                                        }
                                        com.example.util.AppLogger.log(context, "Timeout: Se agotaron los 20 intentos de polling (5 minutos) sin respuesta conclusiva.")
                                    }
                                }
                            } else {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.weight(1.2f).height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = DeepPurpleOnPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Compilar y Publicar", color = DeepPurpleOnPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            AnimatedVisibility(visible = isLoading || statusMessage != null) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    if (isLoading) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
                            color = LavenderPrimary,
                            trackColor = DeepPurpleTrack
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    statusMessage?.let {
                        val bgColor = if (isLoading) Color.Transparent else if (isSuccess) MintSpeed.copy(alpha = 0.15f) else AmberEnergy.copy(alpha = 0.15f)
                        val txtColor = if (isLoading) TextWhite else if (isSuccess) MintSpeed else AmberEnergy
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(bgColor)
                                .border(1.dp, if (!isLoading) txtColor.copy(alpha = 0.3f) else Color.Transparent, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = it,
                                fontSize = 12.sp,
                                color = txtColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
