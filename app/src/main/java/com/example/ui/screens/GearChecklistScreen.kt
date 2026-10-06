package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GearCategory
import com.example.data.model.GearItem
import com.example.ui.viewmodel.CampsiteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GearChecklistScreen(
    viewModel: CampsiteViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val gearList by viewModel.gearList.collectAsState()
    var selectedCategory by remember { mutableStateOf<GearCategory?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredList = remember(gearList, selectedCategory) {
        if (selectedCategory == null) gearList else gearList.filter { it.category == selectedCategory }
    }

    val totalCount = gearList.size
    val checkedCount = gearList.count { it.isChecked }
    val progress = if (totalCount > 0) checkedCount.toFloat() / totalCount else 0f

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Trip Gear & Utilities", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Tailored for Sleep, Water & Power", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("gear_back_btn")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.testTag("add_gear_dialog_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Item")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Gear") },
                modifier = Modifier.testTag("add_gear_fab")
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Progress Bar Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Packing Readiness",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "$checkedCount / $totalCount Packed (${(progress * 100).toInt()}%)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surface
                    )
                }
            }

            // Category Filter Chips
            ScrollableTabRow(
                selectedTabIndex = if (selectedCategory == null) 0 else selectedCategory!!.ordinal + 1,
                edgePadding = 16.dp,
                divider = {},
                containerColor = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    text = { Text("All (${gearList.size})") }
                )
                GearCategory.values().forEach { cat ->
                    val catCount = gearList.count { it.category == cat }
                    val catChecked = gearList.count { it.category == cat && it.isChecked }
                    Tab(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        text = {
                            val icon = when (cat) {
                                GearCategory.SLEEP -> "🛏️"
                                GearCategory.WATER -> "💧"
                                GearCategory.ENERGY -> "⚡"
                                GearCategory.CAMP_COOKING -> "🍳"
                            }
                            Text("$icon ${cat.label} ($catChecked/$catCount)")
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Gear items LazyColumn
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items = filteredList, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleGearChecked(item) }
                            .testTag("gear_item_${item.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.isChecked)
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            else
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = item.isChecked,
                                onCheckedChange = { viewModel.toggleGearChecked(item) }
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                                    color = if (item.isChecked)
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = item.subtitle,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Category badge
                            val badgeColor = when (item.category) {
                                GearCategory.SLEEP -> Color(0xFF673AB7)
                                GearCategory.WATER -> Color(0xFF0288D1)
                                GearCategory.ENERGY -> Color(0xFFF57C00)
                                GearCategory.CAMP_COOKING -> Color(0xFF388E3C)
                            }
                            Surface(
                                shape = CircleShape,
                                color = badgeColor.copy(alpha = 0.15f),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    val icon = when (item.category) {
                                        GearCategory.SLEEP -> Icons.Default.Bed
                                        GearCategory.WATER -> Icons.Default.WaterDrop
                                        GearCategory.ENERGY -> Icons.Default.Bolt
                                        GearCategory.CAMP_COOKING -> Icons.Default.LocalFireDepartment
                                    }
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = badgeColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Gear Item Dialog
    if (showAddDialog) {
        var newTitle by remember { mutableStateOf("") }
        var newSubtitle by remember { mutableStateOf("") }
        var newCategory by remember { mutableStateOf(selectedCategory ?: GearCategory.SLEEP) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Essential Gear") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Item Name (e.g. 50W Solar Panel)") },
                        modifier = Modifier.fillMaxWidth().testTag("new_gear_title")
                    )

                    OutlinedTextField(
                        value = newSubtitle,
                        onValueChange = { newSubtitle = it },
                        label = { Text("Usage / Note") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Pillar Category:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        GearCategory.values().forEach { cat ->
                            FilterChip(
                                selected = newCategory == cat,
                                onClick = { newCategory = cat },
                                label = { Text(cat.label.take(6), fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            viewModel.addCustomGear(
                                title = newTitle.trim(),
                                subtitle = if (newSubtitle.isBlank()) "Added by camper" else newSubtitle.trim(),
                                category = newCategory
                            )
                            showAddDialog = false
                        }
                    },
                    modifier = Modifier.testTag("confirm_add_gear")
                ) {
                    Text("Add Item")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
