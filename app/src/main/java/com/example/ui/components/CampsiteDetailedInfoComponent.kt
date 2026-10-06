package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampsiteDetailedInfoComponent(
    detailedInfo: CampsiteDetailedInfo,
    onNavigateGoogleMaps: () -> Unit,
    onOpenGoogleMaps: () -> Unit,
    modifier: Modifier = Modifier
) {
    val campsite = detailedInfo.campsite
    var selectedPhotoTag by remember { mutableStateOf(PhotoTag.ALL) }
    var expandedPhoto by remember { mutableStateOf<CampsitePhoto?>(null) }

    var selectedAmenityCategory by remember { mutableStateOf<AmenityCategory?>(null) }
    var amenitySearchQuery by remember { mutableStateOf("") }

    val filteredPhotos = remember(detailedInfo.photos, selectedPhotoTag) {
        if (selectedPhotoTag == PhotoTag.ALL) detailedInfo.photos
        else detailedInfo.photos.filter { it.tag == selectedPhotoTag }
    }

    val filteredAmenities = remember(detailedInfo.amenities, selectedAmenityCategory, amenitySearchQuery) {
        var list = detailedInfo.amenities
        if (selectedAmenityCategory != null) {
            list = list.filter { it.category == selectedAmenityCategory }
        }
        if (amenitySearchQuery.isNotBlank()) {
            val q = amenitySearchQuery.trim().lowercase()
            list = list.filter { it.name.lowercase().contains(q) || it.detailNote.lowercase().contains(q) || it.badgeLabel.lowercase().contains(q) }
        }
        list
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("campsite_detailed_info_component"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // SECTION 1: PHOTO GALLERY CAROUSEL
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Campsite Photos & Field Views",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${detailedInfo.photos.size} Verified Camper Photos",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Photo Category Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PhotoTag.values().forEach { tag ->
                        FilterChip(
                            selected = selectedPhotoTag == tag,
                            onClick = { selectedPhotoTag = tag },
                            label = { Text(tag.label, fontSize = 11.sp) },
                            modifier = Modifier.height(30.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Photos Horizontal Scroll Row
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(filteredPhotos, key = { it.id }) { photo ->
                        PhotoCardItem(
                            photo = photo,
                            onClick = { expandedPhoto = photo }
                        )
                    }
                }
            }
        }

        // SECTION 2: LOGISTICS & SEASONALITY TELEMETRY STRIP
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Camp Seasonality & Access Rules",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(modifier = Modifier.fillMaxWidth()) {
                    TelemetryItem(
                        icon = Icons.Default.CalendarMonth,
                        label = "Seasonality",
                        value = detailedInfo.seasonality,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryItem(
                        icon = Icons.Default.Timer,
                        label = "Max Stay",
                        value = "${detailedInfo.maxStayNights} Consecutive Nights",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    TelemetryItem(
                        icon = Icons.Default.Pets,
                        label = "Pet Policy",
                        value = detailedInfo.petPolicy,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryItem(
                        icon = Icons.Default.Wc,
                        label = "Restrooms",
                        value = detailedInfo.restroomType,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    TelemetryItem(
                        icon = Icons.Default.DeleteOutline,
                        label = "Waste & Trash",
                        value = detailedInfo.wasteDisposal,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryItem(
                        icon = Icons.Default.ConfirmationNumber,
                        label = "Permits",
                        value = if (detailedInfo.permitRequired) "Permit Required" else "No Permit Needed",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // SECTION 3: AMENITIES DIRECTORY & EXPLORER
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFE8F5E9),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Checklist,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Camp Amenities & Facilities",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${detailedInfo.amenities.count { it.isAvailable }} of ${detailedInfo.amenities.size} Amenities Verified",
                                fontSize = 11.sp,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Amenity search text field
                OutlinedTextField(
                    value = amenitySearchQuery,
                    onValueChange = { amenitySearchQuery = it },
                    placeholder = { Text("Filter amenities (water, shower, power...)", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    trailingIcon = {
                        if (amenitySearchQuery.isNotEmpty()) {
                            IconButton(onClick = { amenitySearchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("amenity_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Amenity Category Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedAmenityCategory == null,
                        onClick = { selectedAmenityCategory = null },
                        label = { Text("All (${detailedInfo.amenities.size})", fontSize = 11.sp) },
                        modifier = Modifier.height(30.dp)
                    )

                    AmenityCategory.values().forEach { cat ->
                        val count = detailedInfo.amenities.count { it.category == cat }
                        FilterChip(
                            selected = selectedAmenityCategory == cat,
                            onClick = { selectedAmenityCategory = cat },
                            label = { Text("${cat.title.substringBefore("&").trim()} ($count)", fontSize = 11.sp) },
                            modifier = Modifier.height(30.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Amenities List
                if (filteredAmenities.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No amenities match \"$amenitySearchQuery\"",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        filteredAmenities.forEach { amenity ->
                            AmenityRowItem(amenity = amenity)
                        }
                    }
                }
            }
        }
    }

    // EXPANDED PHOTO DIALOG
    expandedPhoto?.let { photo ->
        Dialog(onDismissRequest = { expandedPhoto = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("expanded_photo_dialog"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Full aspect visual art
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = photo.gradientColors.map { Color(it) }
                                )
                            )
                            .padding(14.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.5f),
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Text(
                                text = photo.timeOfDay,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        IconButton(
                            onClick = { expandedPhoto = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(34.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }

                        // Center Icon Emblem
                        Icon(
                            imageVector = when (photo.dominantIcon) {
                                "tent" -> Icons.Default.Bed
                                "water" -> Icons.Default.WaterDrop
                                "bolt" -> Icons.Default.Bolt
                                "stars" -> Icons.Default.Star
                                else -> Icons.Default.Landscape
                            },
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.35f),
                            modifier = Modifier
                                .size(88.dp)
                                .align(Alignment.Center)
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.5f),
                            modifier = Modifier.align(Alignment.BottomStart)
                        ) {
                            Text(
                                text = photo.tag.label,
                                color = Color.White,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = photo.caption,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Photo by ${photo.photographer}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { expandedPhoto = null },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Done")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoCardItem(
    photo: CampsitePhoto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .height(160.dp)
            .clickable(onClick = onClick)
            .testTag("photo_card_${photo.id}"),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = photo.gradientColors.map { Color(it) }
                    )
                )
                .padding(10.dp)
        ) {
            // Tag & Time of day
            Row(
                modifier = Modifier.align(Alignment.TopStart),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.45f)
                ) {
                    Text(
                        text = photo.timeOfDay.take(15),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            // Center Symbol
            Icon(
                imageVector = when (photo.dominantIcon) {
                    "tent" -> Icons.Default.Bed
                    "water" -> Icons.Default.WaterDrop
                    "bolt" -> Icons.Default.Bolt
                    "stars" -> Icons.Default.Star
                    else -> Icons.Default.Landscape
                },
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.25f),
                modifier = Modifier
                    .size(54.dp)
                    .align(Alignment.Center)
            )

            // Caption overlay
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.Black.copy(alpha = 0.6f),
                modifier = Modifier.align(Alignment.BottomStart)
            ) {
                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)) {
                    Text(
                        text = photo.caption,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun AmenityRowItem(amenity: CampsiteAmenity) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Surface(
                shape = CircleShape,
                color = if (amenity.isAvailable) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = getAmenityIcon(amenity.icon),
                        contentDescription = amenity.name,
                        tint = if (amenity.isAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = amenity.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = amenity.detailNote,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Badge Pill
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (amenity.isAvailable) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                border = BorderStroke(1.dp, if (amenity.isAvailable) Color(0xFFA5D6A7) else Color(0xFFFFCDD2))
            ) {
                Text(
                    text = amenity.badgeLabel,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (amenity.isAvailable) Color(0xFF2E7D32) else Color(0xFFC62828),
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun TelemetryItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp).padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun getAmenityIcon(icon: AmenityIcon): ImageVector {
    return when (icon) {
        AmenityIcon.POTABLE_WATER -> Icons.Default.WaterDrop
        AmenityIcon.SHOWERS -> Icons.Default.Shower
        AmenityIcon.TOILETS -> Icons.Default.Wc
        AmenityIcon.TRASH_LOCKER -> Icons.Default.Lock
        AmenityIcon.FIRE_PIT -> Icons.Default.LocalFireDepartment
        AmenityIcon.PICNIC_TABLE -> Icons.Default.Deck
        AmenityIcon.HAMMOCK_TREES -> Icons.Default.Nature
        AmenityIcon.TENT_PAD -> Icons.Default.Bed
        AmenityIcon.SHORE_POWER -> Icons.Default.Bolt
        AmenityIcon.SOLAR_ZONE -> Icons.Default.WbSunny
        AmenityIcon.EV_CHARGER -> Icons.Default.EvStation
        AmenityIcon.PET_FRIENDLY -> Icons.Default.Pets
        AmenityIcon.SWIMMING -> Icons.Default.Pool
        AmenityIcon.TRAILHEAD -> Icons.Default.DirectionsWalk
        AmenityIcon.CELL_SIGNAL -> Icons.Default.SignalCellularAlt
        AmenityIcon.RIVER_ACCESS -> Icons.Default.Waves
    }
}
