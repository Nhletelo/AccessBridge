package com.example.accessbridge.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.format.DateUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.accessbridge.data.local.entity.ResourceEntity
import com.example.accessbridge.domain.model.SyncStatus

/**
 * South African Language Map (Code to Display Name)
 */
val SALanguages = listOf(
    "All" to "All Languages",
    "en" to "English",
    "zu" to "isiZulu",
    "xh" to "isiXhosa",
    "af" to "Afrikaans",
    "nso" to "Sepedi",
    "st" to "Sesotho",
    "tn" to "Setswana",
    "ts" to "Xitsonga",
    "ss" to "siSwati",
    "ve" to "Tshivenda",
    "nr" to "isiNdebele"
)

@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        ),
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
    )
}

/**
 * Sync Status Banner displaying current connection/sync state, last synced time, and manual sync trigger.
 */
@Composable
fun SyncStatusBanner(
    syncStatus: SyncStatus,
    lastSyncedTimestamp: Long?,
    onSyncNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSyncing = syncStatus is SyncStatus.Syncing
    val rotationAngle by animateFloatAsState(
        targetValue = if (isSyncing) 360f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "syncRotation"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        color = when (syncStatus) {
            is SyncStatus.Error -> MaterialTheme.colorScheme.errorContainer
            is SyncStatus.Syncing -> MaterialTheme.colorScheme.secondaryContainer
            else -> MaterialTheme.colorScheme.surfaceContainerHigh
        },
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = when (syncStatus) {
                        is SyncStatus.Error -> Icons.Default.Warning
                        is SyncStatus.Syncing -> Icons.Default.Refresh
                        is SyncStatus.Success -> Icons.Default.CloudDone
                        else -> Icons.Default.CloudOff
                    },
                    contentDescription = "Sync Status",
                    tint = when (syncStatus) {
                        is SyncStatus.Error -> MaterialTheme.colorScheme.error
                        is SyncStatus.Syncing -> MaterialTheme.colorScheme.primary
                        is SyncStatus.Success -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outline
                    },
                    modifier = Modifier
                        .size(22.dp)
                        .rotate(if (isSyncing) rotationAngle else 0f)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    val statusText = when (syncStatus) {
                        is SyncStatus.Syncing -> "Syncing data..."
                        is SyncStatus.Error -> "Offline / Sync Error"
                        is SyncStatus.Success -> "Online • Local DB Ready"
                        else -> "Offline Mode"
                    }
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    val relativeTime = if (lastSyncedTimestamp != null && lastSyncedTimestamp > 0) {
                        DateUtils.getRelativeTimeSpanString(
                            lastSyncedTimestamp,
                            System.currentTimeMillis(),
                            DateUtils.MINUTE_IN_MILLIS
                        ).toString()
                    } else {
                        "Initial Seed Loaded"
                    }

                    Text(
                        text = "Last synced: $relativeTime",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = onSyncNow,
                enabled = !isSyncing,
                modifier = Modifier.height(36.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (isSyncing) "Syncing" else "Sync Now",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

/**
 * South African Language Selector Dropdown Chip
 */
@Composable
fun LanguageSelector(
    selectedLanguageCode: String,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val currentLabel = SALanguages.find { it.first == selectedLanguageCode }?.second ?: "English"

    Box(modifier = modifier) {
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable { expanded = true },
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "Language",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = currentLabel,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Expand",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            SALanguages.forEach { (code, name) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = name,
                            fontWeight = if (code == selectedLanguageCode) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onLanguageSelected(code)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Filter Chip Row for Categories
 */
@Composable
fun CategoryFilterChips(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val selected = category == selectedCategory
            FilterChip(
                selected = selected,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

/**
 * Badge showing "Offline Ready"
 */
@Composable
fun OfflineBadge(
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Offline Ready",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

/**
 * Resource Item Card used in Lists (Learn, Services, Home)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResourceCard(
    resource: ResourceEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = resource.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (resource.isOfflineAvailable) {
                    OfflineBadge()
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = resource.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = resource.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                resource.contactNumber?.let { contact ->
                    Button(
                        onClick = { dialPhone(context, contact) },
                        modifier = Modifier.height(36.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = contact, style = MaterialTheme.typography.labelMedium)
                    }
                }

                resource.contentUrl?.let { url ->
                    Button(
                        onClick = { openWebPage(context, url) },
                        modifier = Modifier.height(36.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.outlineVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Open Link",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Visit Site", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

/**
 * Critical Emergency Hotline Card with High Contrast & 1-Touch Call UI
 */
@Composable
fun EmergencyContactCard(
    title: String,
    phoneNumber: String,
    description: String,
    category: String = "Emergency",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    color = MaterialTheme.colorScheme.error,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = category.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Number: $phoneNumber",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = { dialPhone(context, phoneNumber) },
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call $title",
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

/**
 * Phone Dial Helper
 */
fun dialPhone(context: Context, phoneNumber: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:${phoneNumber.replace(" ", "")}")
        }
        context.startActivity(intent)
    } catch (_: Exception) {
    }
}

/**
 * Web Page Launcher Helper
 */
fun openWebPage(context: Context, url: String) {
    try {
        val validUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "https://$url"
        } else {
            url
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(validUrl))
        context.startActivity(intent)
    } catch (_: Exception) {
    }
}
