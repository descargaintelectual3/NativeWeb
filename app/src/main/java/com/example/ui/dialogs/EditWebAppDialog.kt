package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.WebAppEntity
import com.example.ui.theme.CoralError
import com.example.ui.theme.DeepPurpleContainer
import com.example.ui.theme.DeepPurpleOnPrimary
import com.example.ui.theme.ElegantBackground
import com.example.ui.theme.ElegantCard
import com.example.ui.theme.ElegantCardBorder
import com.example.ui.theme.LavenderPrimary
import com.example.ui.theme.TextGrayLight
import com.example.ui.theme.TextGrayMuted
import com.example.ui.theme.TextWhite

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditWebAppDialog(
    app: WebAppEntity,
    onDismiss: () -> Unit,
    onSave: (WebAppEntity) -> Unit,
    onDelete: (WebAppEntity) -> Unit
) {
    var name by remember { mutableStateOf(app.name) }
    var url by remember { mutableStateOf(app.url) }
    var selectedEmoji by remember { mutableStateOf(app.iconValue) }
    var selectedAccent by remember { mutableStateOf(app.accentColor) }
    var selectedCategory by remember { mutableStateOf(app.category) }
    var isFullscreen by remember { mutableStateOf(app.isFullscreen) }
    var isHardwareBoost by remember { mutableStateOf(app.isHardwareBoostEnabled) }
    var isAdBlock by remember { mutableStateOf(app.isAdBlockEnabled) }
    var isDesktopMode by remember { mutableStateOf(app.isDesktopMode) }
    var isOledBlackMode by remember { mutableStateOf(app.isOledBlackMode) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
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
                                .size(40.dp)
                                .background(DeepPurpleContainer, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = LavenderPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Editar Web App",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextGrayLight)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la App", color = TextGrayLight) },
                    modifier = Modifier.fillMaxWidth(),
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

                Spacer(modifier = Modifier.height(10.dp))

                // URL
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL", color = TextGrayLight) },
                    leadingIcon = { Icon(Icons.Default.Language, contentDescription = null, tint = LavenderPrimary) },
                    modifier = Modifier.fillMaxWidth(),
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

                Spacer(modifier = Modifier.height(14.dp))

                // Icon / Emoji
                Text("Icono", color = LavenderPrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PRESET_EMOJIS.take(16).forEach { emoji ->
                        val isSelected = selectedEmoji == emoji
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) DeepPurpleContainer else ElegantCard)
                                .border(1.dp, if (isSelected) LavenderPrimary else ElegantCardBorder, RoundedCornerShape(10.dp))
                                .clickable { selectedEmoji = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 18.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Accent color
                Text("Color de Acento", color = TextGrayLight)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PRESET_ACCENTS.forEach { colorLong ->
                        val isSelected = selectedAccent == colorLong
                        val color = Color(colorLong)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(2.dp, if (isSelected) TextWhite else Color.Transparent, CircleShape)
                                .clickable { selectedAccent = colorLong },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = DeepPurpleOnPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Switches Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ElegantCard),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElegantCardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Pantalla Completa Inmersiva", color = TextWhite)
                            Switch(
                                checked = isFullscreen,
                                onCheckedChange = { isFullscreen = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = DeepPurpleOnPrimary, checkedTrackColor = LavenderPrimary)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Aceleración GPU Turbo", color = TextWhite)
                            Switch(
                                checked = isHardwareBoost,
                                onCheckedChange = { isHardwareBoost = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = DeepPurpleOnPrimary, checkedTrackColor = LavenderPrimary)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Bloqueador de Anuncios", color = TextWhite)
                            Switch(
                                checked = isAdBlock,
                                onCheckedChange = { isAdBlock = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = DeepPurpleOnPrimary, checkedTrackColor = LavenderPrimary)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Modo Escritorio (Desktop)", color = TextWhite)
                            Switch(
                                checked = isDesktopMode,
                                onCheckedChange = { isDesktopMode = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = DeepPurpleOnPrimary, checkedTrackColor = LavenderPrimary)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { onDelete(app) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = CoralError
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CoralError.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = CoralError)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Eliminar")
                    }

                    Button(
                        onClick = {
                            val updated = app.copy(
                                name = name.trim(),
                                url = url.trim(),
                                iconValue = selectedEmoji,
                                accentColor = selectedAccent,
                                category = selectedCategory,
                                isFullscreen = isFullscreen,
                                isHardwareBoostEnabled = isHardwareBoost,
                                isAdBlockEnabled = isAdBlock,
                                isDesktopMode = isDesktopMode,
                                isOledBlackMode = isOledBlackMode
                            )
                            onSave(updated)
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(50.dp)
                            .testTag("save_edit_app_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LavenderPrimary,
                            contentColor = DeepPurpleOnPrimary
                        )
                    ) {
                        Text("Guardar Cambios", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
