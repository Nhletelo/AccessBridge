package com.example.accessbridge.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.accessbridge.data.local.entity.ResourceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ResourceDao {
    @Query("SELECT * FROM resources ORDER BY lastUpdated DESC")
    fun getAllResources(): Flow<List<ResourceEntity>>

    @Query("SELECT * FROM resources WHERE id = :id LIMIT 1")
    fun getResourceById(id: String): Flow<ResourceEntity?>

    @Query("SELECT * FROM resources WHERE type = :type ORDER BY lastUpdated DESC")
    fun getResourcesByType(type: String): Flow<List<ResourceEntity>>

    @Query("SELECT * FROM resources WHERE category = :category ORDER BY lastUpdated DESC")
    fun getResourcesByCategory(category: String): Flow<List<ResourceEntity>>

    @Query("SELECT * FROM resources WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY lastUpdated DESC")
    fun searchResources(query: String): Flow<List<ResourceEntity>>

    @Query("SELECT * FROM resources WHERE (:language = 'All' OR language = :language OR language = 'en') ORDER BY lastUpdated DESC")
    fun getAllResourcesByLanguage(language: String): Flow<List<ResourceEntity>>

    @Query("SELECT * FROM resources WHERE type = :type AND (:language = 'All' OR language = :language OR language = 'en') ORDER BY lastUpdated DESC")
    fun getResourcesByTypeAndLanguage(type: String, language: String): Flow<List<ResourceEntity>>

    @Query("SELECT * FROM resources WHERE category = :category AND (:language = 'All' OR language = :language OR language = 'en') ORDER BY lastUpdated DESC")
    fun getResourcesByCategoryAndLanguage(category: String, language: String): Flow<List<ResourceEntity>>

    @Query("SELECT * FROM resources WHERE (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%') AND (:language = 'All' OR language = :language OR language = 'en') ORDER BY lastUpdated DESC")
    fun searchResourcesByLanguage(query: String, language: String): Flow<List<ResourceEntity>>

    @Upsert
    suspend fun upsertResources(resources: List<ResourceEntity>)

    @Query("SELECT COUNT(*) FROM resources")
    suspend fun getResourceCount(): Int

    @Query("DELETE FROM resources WHERE lastUpdated < :thresholdTimestamp")
    suspend fun deleteOldResources(thresholdTimestamp: Long = 0L)

    @Query("DELETE FROM resources")
    suspend fun deleteAllResources()
}
