package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Tool
import com.example.model.ToolRegistry
import com.example.viewmodel.ToolboxViewModel
import com.example.data.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MainDashboardView(
    viewModel: ToolboxViewModel,
    modifier: Modifier = Modifier
) {
    val favorites by viewModel.favoriteTools.collectAsState()
    val activities by viewModel.recentActivities.collectAsState()
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
    ) {
        // Hero Glow card
        item {
            DashboardHeroCard(totalFav = favorites.size, latestActivity = activities.firstOrNull()?.toolName ?: "-")
        }

        // Kategori Populer
        item {
            Text(
                text = "KATEGORI POPULER",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.5.sp
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            CategoryGridSection(
                onCategorySelect = { cat ->
                    viewModel.selectCategory(cat)
                    viewModel.navigateTo("all_tools")
                }
            )
        }

        // Stats section
        item {
            StatsCompactRow(favoritesCount = favorites.size, runCount = activities.size)
        }

        // Trending/Favorite Tools
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FAVORIT & REKOMENDASI",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.5.sp
                    )
                )
                if (favorites.isNotEmpty()) {
                    TextButton(onClick = { viewModel.navigateTo("all_tools") }) {
                        Text("Semua Tools", color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
            if (favorites.isEmpty()) {
                RecommendationFallbackCard { toolId ->
                    viewModel.navigateTo("detail", toolId)
                }
            } else {
                FavoritesRowSection(favorites = favorites) { toolId ->
                    viewModel.navigateTo("detail", toolId)
                }
            }
        }

        // Recently Added/Activity Feed
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AKTIVITAS TERBARU",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.5.sp
                    )
                )
                if (activities.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.clearAllHistory() },
                        modifier = Modifier.testTag("clear_history_btn")
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Dihapus", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            RecentActivitySection(activities) { toolId ->
                viewModel.navigateTo("detail", toolId)
            }
        }

        // Watermark & Version
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "wm : Dapp.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp
                        )
                    )
                    Text(
                        text = "v1.0.0 (D-Toolbox)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardHeroCard(totalFav: Int, latestActivity: String) {
    val currentTime = remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime.value = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            kotlinx.coroutines.delay(1000)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hero_dashboard_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x0CFFFFFF)),
        border = BorderStroke(1.dp, Brush.linearGradient(listOf(Color(0xFFFF2B2B).copy(alpha = 0.4f), Color(0x1AFFFFFF))))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawCircle(
                        color = Color(0x19FF2B2B),
                        radius = size.width / 3f,
                        center = Offset(size.width, 0f)
                    )
                }
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "D-TOOLBOX",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                letterSpacing = 2.sp,
                                fontFamily = FontFamily.SansSerif
                            )
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "One Place, Unlimited Tools.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFFFF2B2B),
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            val infiniteTransition = rememberInfiniteTransition(label = "PulseGlow")
                            val pulseAlpha by infiniteTransition.animateFloat(
                                initialValue = 0.3f,
                                targetValue = 1.0f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "PulseAlpha"
                            )
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(Color(0xFFFF2B2B).copy(alpha = pulseAlpha), RoundedCornerShape(50))
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0x33FF2B2B), RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0x4DFF2B2B), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "OFFLINE OK",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF2B2B)
                            )
                        )
                    }
                }

                Divider(color = Color(0x1AFFFFFF), modifier = Modifier.padding(vertical = 4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("SISTEM STATUS", style = MaterialTheme.typography.labelSmall, color = Color(0x99FFFFFF))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(8.dp).background(Color(0xFF00E676), RoundedCornerShape(50)))
                            Text("Operational", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("D-CLOCK", style = MaterialTheme.typography.labelSmall, color = Color(0x99FFFFFF))
                        Text(
                            text = currentTime.value.ifEmpty { "00:00:00" },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryGridSection(onCategorySelect: (String) -> Unit) {
    val categories = listOf(
        Triple("Text", "Text Suite", Icons.Default.TextFields),
        Triple("Encoder/Decoder", "Codec Engine", Icons.Default.Code),
        Triple("Developer", "Dev Resource", Icons.Default.Terminal),
        Triple("Utility", "Utilities", Icons.Default.AutoAwesome),
        Triple("File", "File Handler", Icons.Default.FolderOpen)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        categories.forEach { (cat, label, icon) ->
            Card(
                modifier = Modifier
                    .width(130.dp)
                    .clickable { onCategorySelect(cat) }
                    .testTag("category_card_${cat.replace("/", "_")}"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x0CFFFFFF)),
                border = BorderStroke(1.dp, Color(0x13FFFFFF))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatsCompactRow(favoritesCount: Int, runCount: Int) {
    val totalTools = remember { ToolRegistry.toolsList.size }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            modifier = Modifier.weight(1.1f),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0x0CFFFFFF)),
            border = BorderStroke(1.dp, Color(0x13FFFFFF))
        ) {
            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0x19FF9100), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Build, contentDescription = "Total Tools", tint = Color(0xFFFF9100), modifier = Modifier.size(16.dp))
                }
                Column {
                    Text(text = "Total Tools", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                    Text(text = "$totalTools Tools", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }

        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0x0CFFFFFF)),
            border = BorderStroke(1.dp, Color(0x13FFFFFF))
        ) {
            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0x19FF2B2B), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Favorite, contentDescription = "Suka", tint = Color(0xFFFF2B2B), modifier = Modifier.size(16.dp))
                }
                Column {
                    Text(text = "Favorit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                    Text(text = "$favoritesCount", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }

        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0x0CFFFFFF)),
            border = BorderStroke(1.dp, Color(0x13FFFFFF))
        ) {
            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0x1900E5FF), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.History, contentDescription = "Riwayat", tint = Color(0xFF00E5FF), modifier = Modifier.size(16.dp))
                }
                Column {
                    Text(text = "Aktivitas", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                    Text(text = "$runCount Kali", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun RecommendationFallbackCard(onSelect: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x06FFFFFF)),
        border = BorderStroke(1.dp, Color(0x13FFFFFF))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Belum memiliki favorit? Coba rekomendasi ini:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val recs = listOf(
                Pair("qr_gen", "QR Gen"),
                Pair("color_picker", "Palette Picker"),
                Pair("pass_gen", "Pass Generator")
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                recs.forEach { (id, label) ->
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable { onSelect(id) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                            Text(label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FavoritesRowSection(favorites: List<FavoriteToolEntity>, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (fav in favorites) {
            Row(
                modifier = Modifier
                    .background(Color(0x14FF2B2B), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0x33FF2B2B), RoundedCornerShape(8.dp))
                    .clickable { onSelect(fav.toolId) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Star, contentDescription = "Favorit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Column {
                    Text(fav.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Text(fav.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                }
            }
        }
    }
}

@Composable
fun RecentActivitySection(activities: List<RecentActivityEntity>, onSelect: (String) -> Unit) {
    if (activities.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0x0CFFFFFF)),
            border = BorderStroke(1.dp, Color(0x13FFFFFF))
        ) {
            Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    "Belum ada riwayat aktivitas.",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0x0CFFFFFF)),
            border = BorderStroke(1.dp, Color(0x13FFFFFF))
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                val listToDraw = activities.take(5)
                for (act in listToDraw) {
                    val dateStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(act.timestamp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(act.toolId) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(0x13FFFFFF), RoundedCornerShape(6.dp))
                                    .border(1.dp, Color(0x13FFFFFF), RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                val icon = ToolRegistry.getToolById(act.toolId)?.icon ?: Icons.Default.Build
                                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                            }
                            Column {
                                Text(act.toolName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = "${act.category} • ${act.actionDetail}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = dateStr,
                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
