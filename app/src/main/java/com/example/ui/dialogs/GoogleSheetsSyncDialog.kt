package com.example.ui.dialogs

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.WebAppEntity
import com.example.ui.theme.AmberEnergy
import com.example.ui.theme.CoralError
import com.example.ui.theme.DeepPurpleContainer
import com.example.ui.theme.DeepPurpleOnPrimary
import com.example.ui.theme.ElegantBackground
import com.example.ui.theme.ElegantCard
import com.example.ui.theme.ElegantCardBorder
import com.example.ui.theme.ElegantSurface
import com.example.ui.theme.LavenderPrimary
import com.example.ui.theme.MintSpeed
import com.example.ui.theme.RoseAccent
import com.example.ui.theme.TextGrayLight
import com.example.ui.theme.TextGrayMuted
import com.example.ui.theme.TextWhite
import com.example.util.GoogleSheetsSyncEngine
import com.example.util.WebShortcutHelper
import kotlinx.coroutines.launch

@Composable
fun GoogleSheetsSyncDialog(
    onDismiss: () -> Unit,
    onImportApps: (List<WebAppEntity>) -> Unit,
    onPinAllApps: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var sheetUrl by remember {
        mutableStateOf("https://raw.githubusercontent.com/PabloArboledai/WebNative/main/google_sheets_template.csv")
    }
    var rawCsvInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var syncStatusMessage by remember { mutableStateOf<String?>(null) }
    var parsedAppsCount by remember { mutableStateOf<Int?>(null) }
    var isRawPasteMode by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(26.dp))
                .border(1.dp, ElegantCardBorder, RoundedCornerShape(26.dp)),
            color = ElegantBackground
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    Brush.linearGradient(listOf(MintSpeed, LavenderPrimary)),
                                    RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TableChart,
                                contentDescription = null,
                                tint = DeepPurpleOnPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Google Sheets & DB Sync",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = TextWhite
                            )
                            Text(
                                text = "Base de datos masiva en la nube (15 Columnas)",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextGrayLight,
                                fontSize = 11.sp
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextGrayLight)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Info banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ElegantCard),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElegantCardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AmberEnergy, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sincronización Automática con Google Sheets",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = AmberEnergy
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Conecta una hoja de cálculo con tus sitios (Bene Cloud, Manager Cloud, ControlDroid Cloud y más). Al editar el Google Sheet, actualizas todas tus Web Apps al instante.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGrayLight,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // URL or Paste Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (!isRawPasteMode) LavenderPrimary else ElegantCard)
                            .clickable { isRawPasteMode = false }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "URL / Webhook",
                            fontWeight = FontWeight.Bold,
                            color = if (!isRawPasteMode) DeepPurpleOnPrimary else TextGrayLight,
                            fontSize = 12.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isRawPasteMode) LavenderPrimary else ElegantCard)
                            .clickable { isRawPasteMode = true }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Pegar CSV / Texto",
                            fontWeight = FontWeight.Bold,
                            color = if (isRawPasteMode) DeepPurpleOnPrimary else TextGrayLight,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (!isRawPasteMode) {
                    OutlinedTextField(
                        value = sheetUrl,
                        onValueChange = { sheetUrl = it },
                        label = { Text("Enlace publicado de Google Sheets o CSV URL", color = TextGrayLight) },
                        placeholder = { Text("https://docs.google.com/spreadsheets/d/.../export?format=csv", color = TextGrayMuted) },
                        leadingIcon = { Icon(Icons.Default.Language, contentDescription = null, tint = LavenderPrimary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_google_sheet_url"),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LavenderPrimary,
                            unfocusedBorderColor = ElegantCardBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedContainerColor = ElegantCard,
                            unfocusedContainerColor = ElegantCard
                        ),
                        singleLine = false,
                        maxLines = 3
                    )
                } else {
                    OutlinedTextField(
                        value = rawCsvInput,
                        onValueChange = { rawCsvInput = it },
                        label = { Text("Contenido CSV con encabezados de 15 columnas", color = TextGrayLight) },
                        placeholder = { Text("id,name,url,category,icon_type,icon_value...", color = TextGrayMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LavenderPrimary,
                            unfocusedBorderColor = ElegantCardBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedContainerColor = ElegantCard,
                            unfocusedContainerColor = ElegantCard
                        )
                    )
                }

                // Sync status indicator
                syncStatusMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (msg.startsWith("Éxito")) MintSpeed.copy(alpha = 0.15f) else CoralError.copy(alpha = 0.15f))
                            .border(1.dp, if (msg.startsWith("Éxito")) MintSpeed else CoralError, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = msg,
                            color = if (msg.startsWith("Éxito")) MintSpeed else CoralError,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Main Action: Sincronizar / Descargar Apps
                Button(
                    onClick = {
                        isLoading = true
                        syncStatusMessage = null
                        coroutineScope.launch {
                            if (!isRawPasteMode) {
                                val result = GoogleSheetsSyncEngine.fetchCsvFromGoogleSheetUrl(sheetUrl)
                                isLoading = false
                                result.onSuccess { csvText ->
                                    val apps = GoogleSheetsSyncEngine.parseCsvToWebApps(csvText)
                                    if (apps.isNotEmpty()) {
                                        onImportApps(apps)
                                        parsedAppsCount = apps.size
                                        syncStatusMessage = "Éxito: Se importaron y sincronizaron ${apps.size} Web Apps."
                                        Toast.makeText(context, "¡${apps.size} Web Apps sincronizadas!", Toast.LENGTH_LONG).show()
                                    } else {
                                        syncStatusMessage = "Error: El archivo CSV no contiene registros válidos."
                                    }
                                }.onFailure { error ->
                                    syncStatusMessage = "Error al descargar: ${error.localizedMessage}"
                                }
                            } else {
                                isLoading = false
                                val apps = GoogleSheetsSyncEngine.parseCsvToWebApps(rawCsvInput)
                                if (apps.isNotEmpty()) {
                                    onImportApps(apps)
                                    parsedAppsCount = apps.size
                                    syncStatusMessage = "Éxito: Se importaron ${apps.size} Web Apps desde el texto."
                                    Toast.makeText(context, "¡${apps.size} Web Apps importadas!", Toast.LENGTH_LONG).show()
                                } else {
                                    syncStatusMessage = "Error: Formato CSV inválido o vacío."
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("sync_google_sheets_button"),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LavenderPrimary,
                        contentColor = DeepPurpleOnPrimary
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = DeepPurpleOnPrimary,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sincronizando...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = DeepPurpleOnPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sincronizar Apps desde Google Sheets",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Massive Pin / Create Shortcuts Action
                OutlinedButton(
                    onClick = {
                        onPinAllApps()
                        Toast.makeText(context, "Generando accesos directos en inicio...", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MintSpeed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MintSpeed.copy(alpha = 0.6f))
                ) {
                    Icon(Icons.Default.PushPin, contentDescription = null, tint = MintSpeed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Fijar Todas a Pantalla de Inicio (Shortcuts Masivos)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Export / Share Official Google Sheets Template
                OutlinedButton(
                    onClick = {
                        GoogleSheetsSyncEngine.exportAndShareTemplate(context)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = LavenderPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LavenderPrimary.copy(alpha = 0.6f))
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = LavenderPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Descargar / Compartir Plantilla Google Sheets",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
