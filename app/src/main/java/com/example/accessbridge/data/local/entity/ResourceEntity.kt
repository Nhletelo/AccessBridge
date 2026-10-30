package com.example.accessbridge.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Room Entity representing a resource (Learn, Service, Get Help).
 */
@Serializable
@Entity(tableName = "resources")
data class ResourceEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String,
    val type: String, // e.g., "LEARN", "SERVICE", "HELP"
    val language: String = "en",
    val contactNumber: String? = null,
    val address: String? = null,
    val contentUrl: String? = null,
    val isOfflineAvailable: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)
