package com.example.accessbridge.data.repository

import com.example.accessbridge.data.local.entity.ResourceEntity
import com.example.accessbridge.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Offline-first repository interface for managing AccessBridge resources.
 */
interface ResourceRepository {
    val syncStatus: StateFlow<SyncStatus>
    val lastSyncedTimestamp: StateFlow<Long?>

    fun getAllResources(): Flow<List<ResourceEntity>>
    fun getResourceById(id: String): Flow<ResourceEntity?>
    fun getResourcesByType(type: String): Flow<List<ResourceEntity>>
    fun getResourcesByCategory(category: String): Flow<List<ResourceEntity>>
    fun searchResources(query: String): Flow<List<ResourceEntity>>

    suspend fun syncResources(): Result<Unit>
    suspend fun deleteOldResources(thresholdTimestamp: Long = 0L)
}
