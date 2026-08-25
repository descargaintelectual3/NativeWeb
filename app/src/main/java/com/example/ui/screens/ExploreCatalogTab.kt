package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DeepPurpleContainer
import com.example.ui.theme.DeepPurpleOnPrimary
import com.example.ui.theme.ElegantCard
import com.example.ui.theme.ElegantCardBorder
import com.example.ui.theme.LavenderOnContainer
import com.example.ui.theme.LavenderPrimary
import com.example.ui.theme.MintSpeed
import com.example.ui.theme.TextGrayLight
import com.example.ui.theme.TextGrayMuted
import com.example.ui.theme.TextWhite
import com.example.util.FaviconHelper

data class CuratedAppItem(
    val name: String,
    val url: String,
    val icon: String,
    val category: String,
    val description: String,
    val accentColor: Long,
    val isHardwareBoost: Boolean = true,
    val isAdBlock: Boolean = true
)

val CURATED_CATALOG = listOf(
    // Cloud & Empresa Civer Suite
    CuratedAppItem("Bene Cloud", "https://bene.civer.cloud/", "🌟", "Cloud & Empresa", "Plataforma centralizada de gestión y servicios en la nube", 0xFFD0BCFF, true, true),
    CuratedAppItem("Manager Cloud", "https://manager.civer.cloud/", "📊", "Cloud & Empresa", "Panel de control administrativo y métricas empresariales", 0xFFA6EECA, true, true),
    CuratedAppItem("ControlDroid Cloud", "https://controldroid.civer.cloud/", "🤖", "Cloud & Empresa", "Gestión inteligente de dispositivos y automatización remota", 0xFFFFD999, true, true),

    // IA & Herramientas
    CuratedAppItem("ChatGPT AI", "https://chatgpt.com", "🤖", "IA & Herramientas", "Asistente de inteligencia artificial y generación de ideas", 0xFF10A37F, true, false),
    CuratedAppItem("Claude AI", "https://claude.ai", "🧠", "IA & Herramientas", "Modelo de razonamiento avanzado y programación", 0xFFD97706, true, false),
    CuratedAppItem("DeepSeek", "https://chat.deepseek.com", "🔮", "IA & Herramientas", "Inteligencia artificial de alto rendimiento", 0xFF0284C7, true, false),
    CuratedAppItem("Perplexity", "https://www.perplexity.ai", "🌐", "IA & Herramientas", "Buscador conversacional con citas en tiempo real", 0xFFD0BCFF, true, false),

    // Entretenimiento & Streaming
    CuratedAppItem("YouTube TV", "https://m.youtube.com", "▶️", "Entretenimiento", "Streaming de video sin anuncios molestos y en pantalla completa", 0xFFFF3366, true, true),
    CuratedAppItem("Twitch Live", "https://m.twitch.tv", "🎮", "Entretenimiento", "Transmisiones en vivo de gaming y esports", 0xFF9146FF, true, true),
    CuratedAppItem("Spotify Web", "https://open.spotify.com", "🎵", "Entretenimiento", "Música y podcasts sin límites", 0xFF1DB954, true, false),
    CuratedAppItem("Netflix Web", "https://www.netflix.com", "🍿", "Entretenimiento", "Películas y series en streaming", 0xFFE50914, true, false),

    // Productividad & Diseño
    CuratedAppItem("Figma Pro", "https://www.figma.com", "📐", "Productividad", "Diseño de interfaces colaborativo acelerado por GPU", 0xFF00C2FF, true, false),
    CuratedAppItem("Notion Workspace", "https://www.notion.so", "📝", "Productividad", "Notas, bases de datos y gestión de proyectos", 0xFFD0BCFF, true, false),
    CuratedAppItem("Canva Studio", "https://www.canva.com", "🎨", "Productividad", "Diseño gráfico, presentaciones y videos", 0xFF7D2AE8, true, true),
    CuratedAppItem("Google Drive", "https://drive.google.com", "📁", "Productividad", "Almacenamiento y documentos en la nube", 0xFF4285F4, true, false),

    // Redes & Comunicación
    CuratedAppItem("X / Twitter", "https://x.com", "✖️", "Redes Sociales", "Noticias, tendencias y debates en tiempo real", 0xFF49454F, true, true),
    CuratedAppItem("Instagram Web", "https://www.instagram.com", "📸", "Redes Sociales", "Fotos, Reels e historias sin distracciones", 0xFFE1306C, true, true),
    CuratedAppItem("Telegram Web", "https://web.telegram.org", "💬", "Redes Sociales", "Mensajería rápida, segura y ligera", 0xFF2AABEE, true, false),
    CuratedAppItem("Reddit Pro", "https://www.reddit.com", "👾", "Redes Sociales", "Comunidades, debates y tendencias globales", 0xFFFF4500, true, true),

    // Desarrollo & Gaming
    CuratedAppItem("GitHub Hub", "https://github.com", "🐙", "Desarrollo", "Repositorios de código, pull requests y proyectos", 0xFF49454F, true, false),
    CuratedAppItem("Stack Overflow", "https://stackoverflow.com", "💻", "Desarrollo", "Preguntas y respuestas para programadores", 0xFFF48024, true, true),
    CuratedAppItem("GeForce NOW Web", "https://play.geforcenow.com", "🕹️", "Juegos & Cloud", "Cloud Gaming de alta gama en tu teléfono", 0xFF76B900, true, false)
)

@Composable
fun ExploreCatalogTab(
    onInstallApp: (name: String, url: String, icon: String, accent: Long, category: String, isHw: Boolean, isAd: Boolean) -> Unit,
    onLaunchDirect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var quickUrlInput by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("Todas") }

    val categories = listOf("Todas", "Cloud & Empresa", "IA & Herramientas", "Entretenimiento", "Productividad", "Redes Sociales", "Desarrollo", "Juegos & Cloud")

    val filteredApps = remember(selectedCategoryFilter) {
        if (selectedCategoryFilter == "Todas") CURATED_CATALOG
        else CURATED_CATALOG.filter { it.category == selectedCategoryFilter }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Quick Converter Hero Card in Elegant Dark Deep Purple
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = DeepPurpleContainer),
                border = androidx.compose.foundation.BorderStroke(1.dp, LavenderPrimary.copy(alpha = 0.25f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    DeepPurpleContainer,
                                    Color(0xFF2C1659),
                                    Color(0xFF1E103D)
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(LavenderPrimary, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Bolt, contentDescription = null, tint = DeepPurpleOnPrimary)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Convertidor Instantáneo",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = LavenderOnContainer
                                )
                                Text(
                                    text = "Pega cualquier enlace para abrirlo a pantalla completa",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LavenderPrimary.copy(alpha = 0.85f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // URL Input Box in #2B2930
                        OutlinedTextField(
                            value = quickUrlInput,
                            onValueChange = { quickUrlInput = it },
                            placeholder = { Text("Escribe o pega una URL (ej. reddit.com)...", color = TextGrayMuted, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Language, contentDescription = null, tint = TextGrayMuted) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("instant_url_converter_input"),
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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (quickUrlInput.isNotBlank()) {
                                        val clean = FaviconHelper.cleanUrl(quickUrlInput)
                                        val domain = FaviconHelper.extractDomainName(clean)
                                        onInstallApp(
                                            domain.replaceFirstChar { it.uppercase() },
                                            clean,
                                            "⚡",
                                            0xFFD0BCFF,
                                            "Productividad",
                                            true,
                                            true
                                        )
                                        quickUrlInput = ""
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("convert_and_save_button"),
                                enabled = quickUrlInput.isNotBlank(),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LavenderPrimary, contentColor = DeepPurpleOnPrimary)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Guardar en Mis Apps", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    if (quickUrlInput.isNotBlank()) {
                                        onLaunchDirect(quickUrlInput)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("instant_fullscreen_launch_button"),
                                enabled = quickUrlInput.isNotBlank(),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.12f), contentColor = TextWhite)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Abrir Inmersivo", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Category Filter Chips
        item {
            Text(
                text = "Directorio de Web Apps Curadas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = LavenderPrimary,
                modifier = Modifier.padding(top = 8.dp, bottom = 10.dp)
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSel = selectedCategoryFilter == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSel) LavenderPrimary else ElegantCard)
                            .border(1.dp, if (isSel) LavenderPrimary else ElegantCardBorder, RoundedCornerShape(20.dp))
                            .clickable { selectedCategoryFilter = cat }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSel) DeepPurpleOnPrimary else TextGrayLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Curated List Items
        items(filteredApps) { item ->
            CuratedAppRowItem(
                item = item,
                onInstall = {
                    onInstallApp(
                        item.name,
                        item.url,
                        item.icon,
                        item.accentColor,
                        item.category,
                        item.isHardwareBoost,
                        item.isAdBlock
                    )
                },
                onLaunch = {
                    onLaunchDirect(item.url)
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun CuratedAppRowItem(
    item: CuratedAppItem,
    onInstall: () -> Unit,
    onLaunch: () -> Unit
) {
    var isAdded by remember { mutableStateOf(false) }
    val accentColor = Color(item.accentColor)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, ElegantCardBorder, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = ElegantCard),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.4f),
                                accentColor.copy(alpha = 0.15f)
                            )
                        )
                    )
                    .border(1.5.dp, accentColor.copy(alpha = 0.7f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = item.icon, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(LavenderPrimary.copy(alpha = 0.15f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text("PWA", fontSize = 9.sp, color = LavenderPrimary, fontWeight = FontWeight.Black)
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGrayMuted,
                    maxLines = 2,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Install / Added Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isAdded) MintSpeed.copy(alpha = 0.2f) else LavenderPrimary)
                    .border(1.dp, if (isAdded) MintSpeed else LavenderPrimary, RoundedCornerShape(12.dp))
                    .clickable {
                        isAdded = true
                        onInstall()
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isAdded) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = MintSpeed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Añadida", color = MintSpeed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    } else {
                        Icon(Icons.Default.Add, contentDescription = null, tint = DeepPurpleOnPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Instalar", color = DeepPurpleOnPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
