package com.example.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.ui.theme.AmberEnergy
import com.example.ui.theme.DeepPurpleContainer
import com.example.ui.theme.DeepPurpleOnPrimary
import com.example.ui.theme.ElegantBackground
import com.example.ui.theme.ElegantCard
import com.example.ui.theme.ElegantCardBorder
import com.example.ui.theme.LavenderPrimary
import com.example.ui.theme.MintSpeed
import com.example.ui.theme.RoseAccent
import com.example.ui.theme.TextGrayLight
import com.example.ui.theme.TextGrayMuted
import com.example.ui.theme.TextWhite
import com.example.util.FaviconHelper

val PRESET_EMOJIS = listOf(
    "⚡", "🚀", "🤖", "🌐", "🎮", "🎨", "📝", "💼", "🎥", "💬",
    "🎵", "🛡️", "📊", "🕹️", "🔮", "💻", "🧠", "🍿", "🏆", "📱", "🔥", "✨"
)

val PRESET_ACCENTS = listOf(
    0xFFD0BCFF, // Lavender Luxury
    0xFFFFD8E4, // Rose Dark
    0xFFFFD999, // Amber Energy
    0xFFA6EECA, // Mint Speed
    0xFFFFB4AB, // Coral Rose
    0xFF80D8FF, // Ice Blue
    0xFFEADDFF, // Light Lavender
    0xFF49454F  // Slate Dark
)

val PRESET_CATEGORIES = listOf(
    "Productividad",
    "Entretenimiento",
    "IA & Herramientas",
    "Redes Sociales",
    "Desarrollo",
    "Juegos & Cloud",
    "Utilidades"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateWebAppDialog(
    initialUrl: String = "",
    initialName: String = "",
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        url: String,
        iconType: String,
        iconValue: String,
        accentColor: Long,
        category: String,
        isFullscreen: Boolean,
        isHardwareBoostEnabled: Boolean,
        isAdBlockEnabled: Boolean,
        isBatterySaverBypassEnabled: Boolean,
        isDesktopMode: Boolean,
        customCss: String,
        customJs: String,
        isOledBlackMode: Boolean
    ) -> Unit
) {
    var url by remember { mutableStateOf(initialUrl) }
    var name by remember { mutableStateOf(initialName) }
    var selectedEmoji by remember { mutableStateOf("⚡") }
    var selectedAccent by remember { mutableStateOf(0xFFD0BCFF) }
    var selectedCategory by remember { mutableStateOf("Productividad") }
    var isFullscreen by remember { mutableStateOf(true) }
    var isHardwareBoost by remember { mutableStateOf(true) }
    var isAdBlock by remember { mutableStateOf(true) }
    var isBatteryBypass by remember { mutableStateOf(true) }
    var isDesktopMode by remember { mutableStateOf(false) }
    var isOledBlackMode by remember { mutableStateOf(false) }
    var showAdvanced by remember { mutableStateOf(false) }
    var customCss by remember { mutableStateOf("") }
    var customJs by remember { mutableStateOf("") }

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
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = LavenderPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Convertir Sitio en App",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Text(
                                text = "Nativo, pantalla completa y ultra rápido",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextGrayLight
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_create_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = TextGrayLight
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // URL Input
                OutlinedTextField(
                    value = url,
                    onValueChange = {
                        url = it
                        if (name.isBlank() && it.contains(".")) {
                            name = FaviconHelper.extractDomainName(it).replaceFirstChar { char -> char.uppercase() }
                        }
                    },
                    label = { Text("URL del Sitio Web o Búsqueda", color = TextGrayLight) },
                    placeholder = { Text("ej. https://canva.com o figma.com", color = TextGrayMuted) },
                    leadingIcon = {
                        Icon(Icons.Default.Language, contentDescription = null, tint = LavenderPrimary)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_web_url"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LavenderPrimary,
                        unfocusedBorderColor = ElegantCardBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedContainerColor = ElegantCard,
                        unfocusedContainerColor = ElegantCard
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la App", color = TextGrayLight) },
                    placeholder = { Text("ej. Canva Studio", color = TextGrayMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_app_name"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LavenderPrimary,
                        unfocusedBorderColor = ElegantCardBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedContainerColor = ElegantCard,
                        unfocusedContainerColor = ElegantCard
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Icon / Logo Picker Studio
                Text(
                    text = "Logo e Icono Personalizado",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = LavenderPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Emoji list
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PRESET_EMOJIS.take(16).forEach { emoji ->
                        val isSelected = selectedEmoji == emoji
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) DeepPurpleContainer else ElegantCard)
                                .border(
                                    1.dp,
                                    if (isSelected) LavenderPrimary else ElegantCardBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedEmoji = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 20.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Color Picker
                Text(
                    text = "Color de Acento & Brillo",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextGrayLight
                )
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
                                .border(
                                    2.dp,
                                    if (isSelected) TextWhite else Color.Transparent,
                                    CircleShape
                                )
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

                // Category selector
                Text(
                    text = "Categoría",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextGrayLight
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PRESET_CATEGORIES.forEach { cat ->
                        val isSel = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSel) LavenderPrimary else ElegantCard)
                                .border(1.dp, if (isSel) LavenderPrimary else ElegantCardBorder, RoundedCornerShape(20.dp))
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSel) DeepPurpleOnPrimary else TextGrayLight,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Turbo Switches Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ElegantCard),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElegantCardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Fullscreen Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Fullscreen, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Pantalla Completa Inmersiva", color = TextWhite, style = MaterialTheme.typography.bodyMedium)
                            }
                            Switch(
                                checked = isFullscreen,
                                onCheckedChange = { isFullscreen = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = DeepPurpleOnPrimary, checkedTrackColor = LavenderPrimary)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Hardware Acceleration Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = AmberEnergy, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Aceleración GPU Turbo", color = TextWhite, style = MaterialTheme.typography.bodyMedium)
                            }
                            Switch(
                                checked = isHardwareBoost,
                                onCheckedChange = { isHardwareBoost = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = DeepPurpleOnPrimary, checkedTrackColor = LavenderPrimary)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // AdBlock Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = MintSpeed, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Bloquear Anuncios & Trackers", color = TextWhite, style = MaterialTheme.typography.bodyMedium)
                            }
                            Switch(
                                checked = isAdBlock,
                                onCheckedChange = { isAdBlock = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = DeepPurpleOnPrimary, checkedTrackColor = LavenderPrimary)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Advanced Options Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showAdvanced = !showAdvanced }
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = TextGrayLight, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (showAdvanced) "Ocultar Opciones Avanzadas" else "Ver Opciones Avanzadas (Desktop, OLED, CSS)",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextGrayLight
                    )
                }

                AnimatedVisibility(visible = showAdvanced) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        // Desktop mode
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Computer, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Modo Escritorio (Desktop UA)", color = TextWhite, style = MaterialTheme.typography.bodySmall)
                            }
                            Switch(
                                checked = isDesktopMode,
                                onCheckedChange = { isDesktopMode = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = DeepPurpleOnPrimary, checkedTrackColor = LavenderPrimary)
                            )
                        }

                        // OLED Black Mode
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DarkMode, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Forzar Modo Oscuro Puro OLED", color = TextWhite, style = MaterialTheme.typography.bodySmall)
                            }
                            Switch(
                                checked = isOledBlackMode,
                                onCheckedChange = { isOledBlackMode = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = DeepPurpleOnPrimary, checkedTrackColor = LavenderPrimary)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Custom CSS
                        OutlinedTextField(
                            value = customCss,
                            onValueChange = { customCss = it },
                            label = { Text("CSS Personalizado", color = TextGrayLight) },
                            placeholder = { Text("ej. body { font-family: sans-serif; }", color = TextGrayMuted) },
                            leadingIcon = { Icon(Icons.Default.Code, contentDescription = null, tint = TextGrayLight) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
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
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Create Action Button
                Button(
                    onClick = {
                        if (url.isNotBlank()) {
                            onConfirm(
                                name,
                                url,
                                "EMOJI",
                                selectedEmoji,
                                selectedAccent,
                                selectedCategory,
                                isFullscreen,
                                isHardwareBoost,
                                isAdBlock,
                                isBatteryBypass,
                                isDesktopMode,
                                customCss,
                                customJs,
                                isOledBlackMode
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("confirm_create_app_button"),
                    enabled = url.isNotBlank(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LavenderPrimary,
                        contentColor = DeepPurpleOnPrimary
                    )
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = DeepPurpleOnPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Crear y Guardar App Nativa",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
