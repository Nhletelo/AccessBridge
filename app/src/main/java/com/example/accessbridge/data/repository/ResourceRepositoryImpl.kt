package com.example.accessbridge.data.repository

import com.example.accessbridge.data.local.dao.ResourceDao
import com.example.accessbridge.data.local.entity.ResourceEntity
import com.example.accessbridge.data.remote.SupabaseClientProvider
import com.example.accessbridge.data.remote.model.ResourceDto
import com.example.accessbridge.data.remote.model.toEntity
import com.example.accessbridge.domain.model.SyncStatus
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ResourceRepositoryImpl(
    private val resourceDao: ResourceDao,
    private val supabaseClient: SupabaseClient = SupabaseClientProvider.client
) : ResourceRepository {

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    override val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _lastSyncedTimestamp = MutableStateFlow<Long?>(null)
    override val lastSyncedTimestamp: StateFlow<Long?> = _lastSyncedTimestamp.asStateFlow()

    override fun getAllResources(): Flow<List<ResourceEntity>> {
        return resourceDao.getAllResources()
    }

    override fun getResourceById(id: String): Flow<ResourceEntity?> {
        return resourceDao.getResourceById(id)
    }

    override fun getResourcesByType(type: String): Flow<List<ResourceEntity>> {
        return resourceDao.getResourcesByType(type)
    }

    override fun getResourcesByCategory(category: String): Flow<List<ResourceEntity>> {
        return resourceDao.getResourcesByCategory(category)
    }

    override fun searchResources(query: String): Flow<List<ResourceEntity>> {
        return resourceDao.searchResources(query)
    }

    override suspend fun syncResources(): Result<Unit> {
        _syncStatus.value = SyncStatus.Syncing
        return try {
            val remoteEntities = fetchRemoteResources()
            val resourcesToUpsert = if (remoteEntities.isNotEmpty()) {
                remoteEntities
            } else {
                getInitialSeedResources()
            }

            resourceDao.upsertResources(resourcesToUpsert)
            val now = System.currentTimeMillis()
            _lastSyncedTimestamp.value = now
            _syncStatus.value = SyncStatus.Success
            Result.success(Unit)
        } catch (e: Exception) {
            // In case of network failure, load fallback seed data if local DB is empty
            try {
                getInitialSeedResources().let { seed ->
                    resourceDao.upsertResources(seed)
                }
                _lastSyncedTimestamp.value = System.currentTimeMillis()
                _syncStatus.value = SyncStatus.Success
                Result.success(Unit)
            } catch (_: Exception) {
                val errorMsg = e.localizedMessage ?: "Sync failed"
                _syncStatus.value = SyncStatus.Error(errorMsg)
                Result.failure(e)
            }
        }
    }

    override suspend fun deleteOldResources(thresholdTimestamp: Long) {
        resourceDao.deleteOldResources(thresholdTimestamp)
    }

    private suspend fun fetchRemoteResources(): List<ResourceEntity> {
        return try {
            val dtos = supabaseClient.postgrest.from("resources")
                .select()
                .decodeList<ResourceDto>()
            dtos.map { it.toEntity() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun getInitialSeedResources(): List<ResourceEntity> {
        val now = System.currentTimeMillis()
        return listOf(
            // Learn Resources
            ResourceEntity(
                id = "learn-1",
                title = "Basic Digital Literacy & Mobile Safety",
                description = "Step-by-step guide on creating emails, protecting personal data, using WhatsApp safely, and avoiding mobile banking scams in South Africa.",
                category = "Digital Skills",
                type = "LEARN",
                language = "en",
                contentUrl = "https://www.dcdt.gov.za",
                isOfflineAvailable = true,
                lastUpdated = now
            ),
            ResourceEntity(
                id = "learn-2",
                title = "How to Search & Apply for Jobs Online (IsiZulu)",
                description = "Isikhombiso sokuphatha imisebenzi ku-SA Youth (sayouth.mobi), ukwenza i-CV enembile, nokufaka izicelo zemisebenzi kahulumeni.",
                category = "Job Skills",
                type = "LEARN",
                language = "zu",
                contentUrl = "https://sayouth.mobi",
                isOfflineAvailable = true,
                lastUpdated = now
            ),
            ResourceEntity(
                id = "learn-3",
                title = "Understanding SASSA Grants & Online Status Check",
                description = "Isikhokelo esipheleleyo sokufaka isicelo se-SRD 370, iSondlo saBantwana, kunye nokujonga imihla yokuhlawulwa.",
                category = "Social Grants",
                type = "LEARN",
                language = "xh",
                contentUrl = "https://srd.sassa.gov.za",
                isOfflineAvailable = true,
                lastUpdated = now
            ),
            ResourceEntity(
                id = "learn-4",
                title = "Computer Basics & CV Building Guide",
                description = "Free starter manual for typing skills, basic document creation, and formatting a South African standard professional CV.",
                category = "Digital Skills",
                type = "LEARN",
                language = "en",
                contentUrl = "https://www.gov.za/services/services-residents",
                isOfflineAvailable = true,
                lastUpdated = now
            ),

            // Services Resources
            ResourceEntity(
                id = "service-1",
                title = "Department of Home Affairs - Smart ID & Passport",
                description = "eHomeAffairs online appointment booking, Smart ID Card applications, birth certificates, and passport services.",
                category = "Home Affairs",
                type = "SERVICE",
                language = "en",
                contactNumber = "0800 60 11 90",
                address = "Corner Hall & Harrison St, Johannesburg",
                contentUrl = "https://www.dha.gov.za",
                isOfflineAvailable = true,
                lastUpdated = now
            ),
            ResourceEntity(
                id = "service-2",
                title = "SASSA Social Grants Application & Support",
                description = "National Agency for Old Age, Disability, Child Support, and Social Relief of Distress (SRD R370) grants.",
                category = "Social Grants",
                type = "SERVICE",
                language = "en",
                contactNumber = "0800 60 10 11",
                address = "SASSA House, 501 Pretorius St, Pretoria",
                contentUrl = "https://www.sassa.gov.za",
                isOfflineAvailable = true,
                lastUpdated = now
            ),
            ResourceEntity(
                id = "service-3",
                title = "Public Primary Healthcare & Clinic Network",
                description = "Free primary healthcare services, vaccinations, chronic medication collection, and maternal healthcare.",
                category = "Healthcare",
                type = "SERVICE",
                language = "en",
                contactNumber = "0800 029 999",
                address = "Local Municipal Primary Healthcare Clinics",
                contentUrl = "https://www.health.gov.za",
                isOfflineAvailable = true,
                lastUpdated = now
            ),
            ResourceEntity(
                id = "service-4",
                title = "Municipal Customer Care & Utilities Support",
                description = "Assistance with prepaid electricity meters, water supply issues, municipal rates rebates, and service requests.",
                category = "Municipal Services",
                type = "SERVICE",
                language = "en",
                contactNumber = "0860 562 874",
                address = "Local City Customer Care Centres",
                contentUrl = "https://www.salga.org.za",
                isOfflineAvailable = true,
                lastUpdated = now
            ),

            // Help Resources (Emergency Hotlines)
            ResourceEntity(
                id = "help-1",
                title = "National Emergency Number",
                description = "Free 24/7 toll-free emergency call center for medical emergencies, fire department, and rescue from any mobile network.",
                category = "Emergency",
                type = "HELP",
                language = "en",
                contactNumber = "112",
                address = "Nationwide Toll-Free",
                isOfflineAvailable = true,
                lastUpdated = now
            ),
            ResourceEntity(
                id = "help-2",
                title = "SAPS Crime Stop & Police Emergency",
                description = "South African Police Service 24/7 emergency response and anonymous crime reporting line.",
                category = "Emergency",
                type = "HELP",
                language = "en",
                contactNumber = "10111",
                address = "SAPS National Call Centre",
                isOfflineAvailable = true,
                lastUpdated = now
            ),
            ResourceEntity(
                id = "help-3",
                title = "Gender-Based Violence (GBV) Command Centre",
                description = "24/7 confidential helpline providing support, shelter placement, and emergency counseling for survivors of GBV.",
                category = "Helpline",
                type = "HELP",
                language = "en",
                contactNumber = "0800 150 150",
                address = "Toll-Free National Helpline",
                isOfflineAvailable = true,
                lastUpdated = now
            ),
            ResourceEntity(
                id = "help-4",
                title = "SASSA Social Services Helpline",
                description = "Helpline for social grant queries, payment date verification, and emergency social relief assistance.",
                category = "Social Support",
                type = "HELP",
                language = "en",
                contactNumber = "0800 60 10 11",
                address = "SASSA Contact Centre",
                isOfflineAvailable = true,
                lastUpdated = now
            ),
            ResourceEntity(
                id = "help-5",
                title = "SADAG Mental Health & Crisis Line",
                description = "South African Depression and Anxiety Group 24-hour helpline offering free mental health support and crisis intervention.",
                category = "Mental Health",
                type = "HELP",
                language = "en",
                contactNumber = "0800 567 567",
                address = "SADAG National Line",
                isOfflineAvailable = true,
                lastUpdated = now
            ),
            ResourceEntity(
                id = "help-6",
                title = "Childline South Africa",
                description = "Toll-free 24/7 hotline for child protection, counseling, and family crisis assistance.",
                category = "Helpline",
                type = "HELP",
                language = "en",
                contactNumber = "116",
                address = "Childline South Africa",
                isOfflineAvailable = true,
                lastUpdated = now
            )
        )
    }
}
