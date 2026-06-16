package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Tool
import com.example.model.ToolRegistry
import com.example.viewmodel.ToolboxViewModel

@Composable
fun AllToolsListView(
    viewModel: ToolboxViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val sortType by viewModel.sortType.collectAsState()
    val favorites by viewModel.favoriteTools.collectAsState()
    val isGridMode by viewModel.settingsLayoutGrid.collectAsState()

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val gridSpan = when {
        screenWidth >= 900 -> 3
        screenWidth >= 600 -> 2
        else -> 1
    }

    // Filter and Search logic
    val filteredTools = remember(searchQuery, selectedCategory, sortType) {
        var list = ToolRegistry.toolsList.filter {
            (selectedCategory == "All" || it.category == selectedCategory) &&
                    (it.name.contains(searchQuery, ignoreCase = true) ||
                            it.description.contains(searchQuery, ignoreCase = true) ||
                            it.category.contains(searchQuery, ignoreCase = true))
        }
        if (sortType == "A-Z") {
            list = list.sortedBy { it.name }
        } else {
            // "Newest" -> let's make it descend based on our internal index or predefined newest tools.
            list = list.reversed()
        }
        list
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Real-time search row
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("tools_search_bar"),
            placeholder = { Text("Cari tools (cth: QR, Base64)...", color = Color.White.copy(alpha = 0.35f)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Pencarian", tint = MaterialTheme.colorScheme.primary) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Bersihkan", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0x80FF2B2B),
                unfocusedBorderColor = Color(0x13FFFFFF),
                focusedContainerColor = Color(0x0CFFFFFF),
                unfocusedContainerColor = Color(0x0CFFFFFF)
            )
        )

        // Category Filter tabs
        val categories = listOf("All", "Text", "Encoder/Decoder", "Developer", "Utility", "File")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary else Color(0x0CFFFFFF),
                            RoundedCornerShape(50)
                        )
                        .border(
                            1.dp,
                            if (isSelected) Color.Transparent else Color(0x0DFFFFFF),
                            RoundedCornerShape(50)
                        )
                        .clickable { viewModel.selectCategory(cat) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = cat,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // Sorting & Layout Mode buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Urutkan:", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
                listOf("A-Z", "Terbaru").forEach { type ->
                    val resolvedType = if (type == "Terbaru") "Newest" else "A-Z"
                    val isSelected = sortType == resolvedType
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelected) Color(0x26FF2B2B) else Color(0x0CFFFFFF),
                                RoundedCornerShape(6.dp)
                            )
                            .border(
                                1.dp,
                                if (isSelected) Color(0x4DFF2B2B) else Color(0x13FFFFFF),
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { viewModel.setSortType(resolvedType) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = type,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color(0xFFFF2B2B) else Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            IconButton(onClick = { viewModel.toggleLayoutMode() }) {
                Icon(
                    imageVector = if (isGridMode) Icons.Default.GridView else Icons.Default.ViewList,
                    contentDescription = "Ubah Layout",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Tools displaying as Grid or List
        if (filteredTools.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Inbox, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                    Text("Tidak ada tools yang cocok.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            if (isGridMode) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(gridSpan),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredTools) { tool ->
                        val isFav = favorites.any { it.toolId == tool.id }
                        ToolGridItem(
                            tool = tool,
                            isFavorite = isFav,
                            onFavoriteToggle = { viewModel.toggleFavorite(tool) },
                            onOpen = { viewModel.navigateTo("detail", tool.id) }
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(1),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTools) { tool ->
                        val isFav = favorites.any { it.toolId == tool.id }
                        ToolListItem(
                            tool = tool,
                            isFavorite = isFav,
                            onFavoriteToggle = { viewModel.toggleFavorite(tool) },
                            onOpen = { viewModel.navigateTo("detail", tool.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ToolGridItem(
    tool: Tool,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onOpen: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .testTag("tool_card_${tool.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x0CFFFFFF)),
        border = BorderStroke(1.dp, Color(0x13FFFFFF))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(tool.icon, contentDescription = tool.name, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onFavoriteToggle, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Favorit",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = tool.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = tool.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(tool.category, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("BUKA", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun ToolListItem(
    tool: Tool,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onOpen: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .testTag("tool_list_item_${tool.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x0CFFFFFF)),
        border = BorderStroke(1.dp, Color(0x13FFFFFF))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(tool.icon, contentDescription = tool.name, tint = MaterialTheme.colorScheme.primary)
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(tool.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(tool.category, style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text(
                    text = tool.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Favorit",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(Icons.Default.ChevronRight, contentDescription = "Buka", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
