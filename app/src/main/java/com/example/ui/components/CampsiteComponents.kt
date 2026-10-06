package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.data.model.*
import com.example.ui.theme.*

@Composable
fun CampsiteCard(
    campsite: Campsite,
    distanceMiles: Double,
    onClick: () -> Unit,
    onToggleBookmark: () -> Unit,
    onOpenGoogleMaps: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("campsite_card_${campsite.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Header Banner with Terrain Styling
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = when (campsite.terrainType) {
                                "Alpine Forest" -> listOf(Color(0xFF1B4332), Color(0xFF2D6A4F))
                                "Rainforest Riverbank" -> listOf(Color(0xFF0F3B3E), Color(0xFF186F65))
                                "Desert Canyon" -> listOf(Color(0xFF8D4004), Color(0xFFB85D19))
                                "Glacial Valley" -> listOf(Color(0xFF1A365D), Color(0xFF2B6CB0))
                                "Alpine Lake" -> listOf(Color(0xFF0B525B), Color(0xFF14746F))
                                "Coastal Cliffs" -> listOf(Color(0xFF1D3557), Color(0xFF457B9D))
                                else -> listOf(Color(0xFF2D3748), Color(0xFF4A5568))
                            }
                        )
                    )
                    .padding(14.dp)
            ) {
                // Terrain Tag & Elevation
                Row(
                    modifier = Modifier.align(Alignment.TopStart),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.35f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Landscape,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = campsite.terrainType,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.35f)
                    ) {
                        Text(
                            text = "${campsite.sleep.elevationFt} ft",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                        )
                    }
                }

                // Action Buttons (Google Maps & Bookmark)
                Row(
                    modifier = Modifier.align(Alignment.TopEnd),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onOpenGoogleMaps != null) {
                        IconButton(
                            onClick = onOpenGoogleMaps,
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                                .testTag("card_gmaps_btn_${campsite.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = "Open in Google Maps",
                                tint = Color(0xFF81D4FA),
                                modifier = Modifier.size(17.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                            .testTag("bookmark_btn_${campsite.id}")
                    ) {
                        Icon(
                            imageVector = if (campsite.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = if (campsite.isBookmarked) "Bookmarked" else "Bookmark",
                            tint = if (campsite.isBookmarked) Color(0xFFFFD54F) else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Campsite Name and Region
                Column(
                    modifier = Modifier.align(Alignment.BottomStart)
                ) {
                    Text(
                        text = campsite.name,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${campsite.region} • ${campsite.stateOrCountry}",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }

            // Body: Essential Tri-Pillar Badges (Sleep, Water, Energy)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Quick Summary Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${campsite.rating}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = " (${campsite.reviewCount})",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = "$distanceMiles mi away",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = campsite.feePerNight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // The 3 Pillars Badges (Sleep, Water, Energy)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Pillar 1: Sleep
                    PillarSummaryPill(
                        icon = Icons.Default.Bed,
                        title = campsite.sleep.type.label,
                        subtitle = campsite.sleep.groundType.label,
                        accentColor = Color(0xFF673AB7),
                        bgColor = Color(0xFFEDE7F6),
                        modifier = Modifier.weight(1f)
                    )

                    // Pillar 2: Water
                    PillarSummaryPill(
                        icon = Icons.Default.WaterDrop,
                        title = if (campsite.water.sourceType.isPotable) "Potable Water" else "Filter Needed",
                        subtitle = "${campsite.water.distanceToSourceMeters}m to source",
                        accentColor = Color(0xFF0288D1),
                        bgColor = Color(0xFFE1F5FE),
                        modifier = Modifier.weight(1f)
                    )

                    // Pillar 3: Energy
                    PillarSummaryPill(
                        icon = Icons.Default.Bolt,
                        title = if (campsite.energy.sourceType.hasGridPower) "Hookup / Outlet" else "Solar Off-Grid",
                        subtitle = "Solar: ${campsite.energy.solarExposureIndex}/10",
                        accentColor = Color(0xFFF57C00),
                        bgColor = Color(0xFFFFF3E0),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun PillarSummaryPill(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = bgColor.copy(alpha = 0.85f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = Color(0xFF333333),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SleepSectionCard(sleep: SleepDetails, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFEDE7F6),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Bed,
                            contentDescription = "Sleep",
                            tint = Color(0xFF5E35B1),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Sleeping Setup & Pitch Quality",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = sleep.type.label,
                        fontSize = 13.sp,
                        color = Color(0xFF5E35B1),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Grid of Attributes
            Row(modifier = Modifier.fillMaxWidth()) {
                InfoItem(
                    label = "Ground Type",
                    value = sleep.groundType.label,
                    icon = Icons.Default.Terrain,
                    modifier = Modifier.weight(1f)
                )
                InfoItem(
                    label = "Max Capacity",
                    value = "${sleep.maxCapacity} Campers",
                    icon = Icons.Default.Group,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                InfoItem(
                    label = "Hammock Friendly",
                    value = if (sleep.hammockFriendly) "Yes (Sturdy Trees)" else "No / Sparse Trees",
                    icon = Icons.Default.Nature,
                    modifier = Modifier.weight(1f)
                )
                InfoItem(
                    label = "Tree Shade",
                    value = "${sleep.shadeRating} / 5 Stars",
                    icon = Icons.Default.WbSunny,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeMute,
                        contentDescription = "Quiet Hours",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Quiet Hours: ${sleep.quietHours}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun WaterSectionCard(water: WaterDetails, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFE1F5FE),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = "Water",
                            tint = Color(0xFF0288D1),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Water & Hydration Access",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = water.sourceType.label,
                        fontSize = 13.sp,
                        color = if (water.sourceType.isPotable) Color(0xFF0288D1) else Color(0xFFD32F2F),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                InfoItem(
                    label = "Potability Status",
                    value = if (water.sourceType.isPotable) "Safe to Drink" else "Boil / Filter Required",
                    icon = if (water.sourceType.isPotable) Icons.Default.CheckCircle else Icons.Default.Warning,
                    modifier = Modifier.weight(1f)
                )
                InfoItem(
                    label = "Tap / Stream Distance",
                    value = if (water.distanceToSourceMeters == 0) "Pack In (Dry)" else "${water.distanceToSourceMeters} meters away",
                    icon = Icons.Default.DirectionsWalk,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                InfoItem(
                    label = "Hot Showers",
                    value = if (water.hasHotShowers) "Available On-Site" else "None",
                    icon = Icons.Default.Shower,
                    modifier = Modifier.weight(1f)
                )
                InfoItem(
                    label = "Dish Sink",
                    value = if (water.hasDishwashingSink) "Available" else "Wash at camp",
                    icon = Icons.Default.Countertops,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Reliability",
                        tint = Color(0xFF0288D1),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Flow Reliability: ${water.flowReliability}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun EnergySectionCard(energy: EnergyDetails, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFFF3E0),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Energy",
                            tint = Color(0xFFF57C00),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Power & Energy Hookups",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = energy.sourceType.label,
                        fontSize = 13.sp,
                        color = Color(0xFFF57C00),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Solar meter
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = null,
                            tint = Color(0xFFF57C00),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Solar Exposure Viability",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "${energy.solarExposureIndex} / 10",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF57C00)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { energy.solarExposureIndex / 10f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFFF57C00),
                    trackColor = Color(0xFFFFE0B2)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                InfoItem(
                    label = "Campfire Pit",
                    value = if (energy.campfireRing) "Allowed in Ring" else "Fire Ban Active",
                    icon = Icons.Default.LocalFireDepartment,
                    modifier = Modifier.weight(1f)
                )
                InfoItem(
                    label = "Firewood Available",
                    value = if (energy.firewoodPurchasable) "Buy at Ranger Host" else "Bring your own",
                    icon = Icons.Default.Forest,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                InfoItem(
                    label = "EV / Rig Charging",
                    value = if (energy.hasEvCharging) "Supported" else "Standard Rig Only",
                    icon = Icons.Default.EvStation,
                    modifier = Modifier.weight(1f)
                )
                InfoItem(
                    label = "Generator Hours",
                    value = energy.generatorHours,
                    icon = Icons.Default.ElectricalServices,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun InfoItem(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.padding(horizontal = 4.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
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
}
