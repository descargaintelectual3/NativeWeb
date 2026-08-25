package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewCompact
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WebAppEntity
import com.example.ui.dialogs.PRESET_CATEGORIES
import com.example.ui.theme.AmberEnergy
import com.example.ui.theme.CoralError
import com.example.ui.theme.DeepPurpleContainer
import com.example.ui.theme.DeepPurpleOnPrimary
import com.example.ui.theme.DeepPurpleTrack
import com.example.ui.theme.ElegantBackground
import com.example.ui.theme.ElegantCard
import com.example.ui.theme.ElegantCardBorder
import com.example.ui.theme.LavenderOnContainer
import com.example.ui.theme.LavenderPrimary
import com.example.ui.theme.MintSpeed
import com.example.ui.theme.RoseAccent
import com.example.ui.theme.TextGrayLight
import com.example.ui.theme.TextGrayMuted
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.AppLayoutMode
import com.example.ui.viewmodel.WebAppUiState
import com.example.util.WebShortcutHelper

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppLibraryTab(
    state: WebAppUiState,
    onSearchChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onLayoutModeChange: (AppLayoutMode) -> Unit,
    onLaunchApp: (WebAppEntity) -> Unit,
    onPinShortcut: (WebAppEntity) -> Unit,
    onEditApp: (WebAppEntity) -> Unit,
    onDeleteApp: (WebAppEntity) -> Unit,
    onCreateAppClick: () -> Unit,
    onOpenSheetsSync: () -> Unit,
    onRestoreCiverCloud: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allCategories = remember { listOf("Todas", "Cloud & Empresa") + PRESET_CATEGORIES }
    var showModularityMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Performance Nexus & Modularity Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = ElegantCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElegantCardBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(DeepPurpleContainer.copy(alpha = 0.4f), Color.Transparent)
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f, fill = false),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(DeepPurpleContainer)
                                            .border(1.dp, LavenderPrimary.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Bolt,
                                            contentDescription = null,
                                            tint = LavenderPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "MOTOR TURBO WEB-NATIVO",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Black,
                                            color = LavenderPrimary,
                                            letterSpacing = 1.sp,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${state.webApps.size} Aplicaciones Activas",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite,
                                            maxLines = 1
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Google Sheets Sync Button
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MintSpeed.copy(alpha = 0.18f))
                                        .border(1.dp, MintSpeed.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                        .clickable { onOpenSheetsSync() }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.TableChart, contentDescription = null, tint = MintSpeed, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Sheets Sync",
                                            color = MintSpeed,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Three Turbo Metric Badges
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MetricBadge(
                                    label = "Bloqueados",
                                    value = "${state.totalBlockedAds} Ads",
                                    color = MintSpeed,
                                    modifier = Modifier.weight(1f)
                                )
                                MetricBadge(
                                    label = "Aceleración",
                                    value = "GPU Turbo",
                                    color = AmberEnergy,
                                    modifier = Modifier.weight(1f)
                                )
                                MetricBadge(
                                    label = "Inmersión",
                                    value = "OLED 100%",
                                    color = RoseAccent,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // 2. CIVER CLOUD SUITE SHOWCASE BANNER
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ElegantCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LavenderPrimary.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Cloud, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Civer Cloud Ecosystem",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            }
                            Text(
                                text = "Nativo & Cloud",
                                style = MaterialTheme.typography.labelSmall,
                                color = LavenderPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick Cloud App Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CloudAppPill(
                                name = "Bene Cloud",
                                emoji = "🌟",
                                accent = LavenderPrimary,
                                onClick = {
                                    val app = state.webApps.find { it.url.contains("bene.civer.cloud") }
                                        ?: WebAppEntity(name = "Bene Cloud", url = "https://bene.civer.cloud/", iconValue = "🌟", accentColor = 0xFFD0BCFF, category = "Cloud & Empresa")
                                    onLaunchApp(app)
                                },
                                modifier = Modifier.weight(1f)
                            )

                            CloudAppPill(
                                name = "Manager",
                                emoji = "📊",
                                accent = MintSpeed,
                                onClick = {
                                    val app = state.webApps.find { it.url.contains("manager.civer.cloud") }
                                        ?: WebAppEntity(name = "Manager Cloud", url = "https://manager.civer.cloud/", iconValue = "📊", accentColor = 0xFFA6EECA, category = "Cloud & Empresa")
                                    onLaunchApp(app)
                                },
                                modifier = Modifier.weight(1f)
                            )

                            CloudAppPill(
                                name = "ControlDroid",
                                emoji = "🤖",
                                accent = AmberEnergy,
                                onClick = {
                                    val app = state.webApps.find { it.url.contains("controldroid.civer.cloud") }
                                        ?: WebAppEntity(name = "ControlDroid Cloud", url = "https://controldroid.civer.cloud/", iconValue = "🤖", accentColor = 0xFFFFD999, category = "Cloud & Empresa")
                                    onLaunchApp(app)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 3. Search Bar + Modular Layout Switcher Toolbar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = { Text("Buscar en mis aplicaciones...", color = TextGrayMuted, fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = LavenderPrimary, modifier = Modifier.size(20.dp))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("search_web_apps_input"),
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

                    // Modular Layout Selector (Grid 2, Grid 3, List, Hero)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(ElegantCard)
                            .border(1.dp, ElegantCardBorder, RoundedCornerShape(14.dp))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        IconButton(
                            onClick = { onLayoutModeChange(AppLayoutMode.GRID_2) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                Icons.Default.GridView,
                                contentDescription = "Cuadrícula 2",
                                tint = if (state.layoutMode == AppLayoutMode.GRID_2) LavenderPrimary else TextGrayMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = { onLayoutModeChange(AppLayoutMode.GRID_3) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                Icons.Default.ViewCompact,
                                contentDescription = "Cuadrícula 3",
                                tint = if (state.layoutMode == AppLayoutMode.GRID_3) LavenderPrimary else TextGrayMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = { onLayoutModeChange(AppLayoutMode.LIST_DETAILED) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                Icons.Default.ViewList,
                                contentDescription = "Lista Detallada",
                                tint = if (state.layoutMode == AppLayoutMode.LIST_DETAILED) LavenderPrimary else TextGrayMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // 4. Categories Filter Chips
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(allCategories) { category ->
                        val isSelected = state.selectedCategory == category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) LavenderPrimary else ElegantCard)
                                .border(1.dp, if (isSelected) LavenderPrimary else ElegantCardBorder, RoundedCornerShape(20.dp))
                                .clickable { onCategoryChange(category) }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) DeepPurpleOnPrimary else TextGrayLight,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // 5. Empty State
            if (state.filteredWebApps.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No se encontraron aplicaciones", color = TextGrayLight, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(LavenderPrimary)
                                .clickable { onRestoreCiverCloud() }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("Restaurar Aplicaciones por Defecto", color = DeepPurpleOnPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // 6. APPS RENDERING ACCORDING TO MODULAR LAYOUT
            when (state.layoutMode) {
                AppLayoutMode.LIST_DETAILED -> {
                    items(state.filteredWebApps, key = { it.id }) { app ->
                        WebAppListCard(
                            app = app,
                            onLaunch = { onLaunchApp(app) },
                            onPin = { onPinShortcut(app) },
                            onEdit = { onEditApp(app) },
                            onDelete = { onDeleteApp(app) }
                        )
                    }
                }
                AppLayoutMode.GRID_3 -> {
                    item {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            maxItemsInEachRow = 3
                        ) {
                            val chunkWidthModifier = Modifier.fillMaxWidth(0.31f)
                            state.filteredWebApps.forEach { app ->
                                WebAppCompactCard(
                                    app = app,
                                    onLaunch = { onLaunchApp(app) },
                                    modifier = chunkWidthModifier
                                )
                            }
                        }
                    }
                }
                else -> {
                    // Default GRID_2
                    item {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            maxItemsInEachRow = 2
                        ) {
                            val chunkWidthModifier = Modifier.fillMaxWidth(0.48f)
                            state.filteredWebApps.forEach { app ->
                                WebAppCard(
                                    app = app,
                                    onLaunch = { onLaunchApp(app) },
                                    onPin = { onPinShortcut(app) },
                                    onEdit = { onEditApp(app) },
                                    onDelete = { onDeleteApp(app) },
                                    modifier = chunkWidthModifier
                                )
                            }
                        }
                    }
                }
            }
        }

        // FAB: Add Custom Web App
        FloatingActionButton(
            onClick = onCreateAppClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
                .testTag("fab_add_web_app"),
            containerColor = LavenderPrimary,
            contentColor = DeepPurpleOnPrimary,
            shape = RoundedCornerShape(18.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Convertir Sitio en App")
        }
    }
}

@Composable
fun CloudAppPill(
    name: String,
    emoji: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(accent.copy(alpha = 0.12f))
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = emoji, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = name,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun MetricBadge(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(ElegantBackground)
            .border(1.dp, ElegantCardBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextGrayLight,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun WebAppListCard(
    app: WebAppEntity,
    onLaunch: () -> Unit,
    onPin: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = Color(app.accentColor)
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onLaunch() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ElegantCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(accent.copy(alpha = 0.2f))
                        .border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = app.iconValue, fontSize = 22.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = app.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = app.url,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGrayLight,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPin, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.PushPin, contentDescription = "Fijar", tint = LavenderPrimary, modifier = Modifier.size(18.dp))
                }
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Opciones", tint = TextGrayLight, modifier = Modifier.size(18.dp))
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(ElegantCard)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Abrir App", color = TextWhite) },
                            onClick = { showMenu = false; onLaunch() }
                        )
                        DropdownMenuItem(
                            text = { Text("Editar", color = TextWhite) },
                            onClick = { showMenu = false; onEdit() }
                        )
                        DropdownMenuItem(
                            text = { Text("Eliminar", color = CoralError) },
                            onClick = { showMenu = false; onDelete() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WebAppCompactCard(
    app: WebAppEntity,
    onLaunch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = Color(app.accentColor)

    Card(
        modifier = modifier
            .clickable { onLaunch() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ElegantCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = 0.2f))
                    .border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = app.iconValue, fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = app.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun WebAppCard(
    app: WebAppEntity,
    onLaunch: () -> Unit,
    onPin: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = Color(app.accentColor)
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onLaunch() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ElegantCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Icon & Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(accent.copy(alpha = 0.2f))
                        .border(1.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = app.iconValue, fontSize = 24.sp)
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Opciones",
                            tint = TextGrayLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(ElegantCard)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Abrir App", color = TextWhite) },
                            onClick = { showMenu = false; onLaunch() }
                        )
                        DropdownMenuItem(
                            text = { Text("Fijar a Pantalla de Inicio", color = LavenderPrimary) },
                            onClick = { showMenu = false; onPin() }
                        )
                        DropdownMenuItem(
                            text = { Text("Editar Configuración", color = TextWhite) },
                            onClick = { showMenu = false; onEdit() }
                        )
                        DropdownMenuItem(
                            text = { Text("Eliminar", color = CoralError) },
                            onClick = { showMenu = false; onDelete() }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // App Name
            Text(
                text = app.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Category & Domain
            Text(
                text = app.category,
                style = MaterialTheme.typography.labelSmall,
                color = LavenderPrimary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (app.isHardwareBoostEnabled) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DeepPurpleContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("GPU", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = LavenderPrimary)
                    }
                }
                if (app.isAdBlockEnabled) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MintSpeed.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("NO-ADS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MintSpeed)
                    }
                }
            }
        }
    }
}
