package com.example.accessbridge.data.remote.model

import com.example.accessbridge.data.local.entity.ResourceEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResourceDto(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String,
    @SerialName("category") val category: String,
    @SerialName("type") val type: String,
    @SerialName("language") val language: String = "en",
    @SerialName("contact_number") val contactNumber: String? = null,
    @SerialName("address") val address: String? = null,
    @SerialName("content_url") val contentUrl: String? = null,
    @SerialName("is_offline_available") val isOfflineAvailable: Boolean = false,
    @SerialName("last_updated") val lastUpdated: Long = System.currentTimeMillis()
)

fun ResourceDto.toEntity(): ResourceEntity = ResourceEntity(
    id = id,
    title = title,
    description = description,
    category = category,
    type = type,
    language = language,
    contactNumber = contactNumber,
    address = address,
    contentUrl = contentUrl,
    isOfflineAvailable = isOfflineAvailable,
    lastUpdated = lastUpdated
)
