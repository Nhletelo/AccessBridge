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

    override fun getAllResourcesByLanguage(language: String): Flow<List<ResourceEntity>> {
        return resourceDao.getAllResourcesByLanguage(language)
    }

    override fun getResourcesByTypeAndLanguage(type: String, language: String): Flow<List<ResourceEntity>> {
        return resourceDao.getResourcesByTypeAndLanguage(type, language)
    }

    override fun getResourcesByCategoryAndLanguage(category: String, language: String): Flow<List<ResourceEntity>> {
        return resourceDao.getResourcesByCategoryAndLanguage(category, language)
    }

    override fun searchResourcesByLanguage(query: String, language: String): Flow<List<ResourceEntity>> {
        return resourceDao.searchResourcesByLanguage(query, language)
    }

    override suspend fun syncResources(): Result<Unit> {
        _syncStatus.value = SyncStatus.Syncing
        return try {
            val remoteEntities = fetchRemoteResources()
            if (remoteEntities.isNotEmpty()) {
                resourceDao.upsertResources(remoteEntities)
                _lastSyncedTimestamp.value = System.currentTimeMillis()
                _syncStatus.value = SyncStatus.Success
                Result.success(Unit)
            } else {
                // If remote returned empty, ensure local cache has initial seed resources if empty
                if (resourceDao.getResourceCount() == 0) {
                    resourceDao.upsertResources(getInitialSeedResources())
                }
                _lastSyncedTimestamp.value = System.currentTimeMillis()
                _syncStatus.value = SyncStatus.Success
                Result.success(Unit)
            }
        } catch (e: Exception) {
            // Offline or network error occurred. Fall back gracefully to local Room cache.
            _syncStatus.value = SyncStatus.Offline
            try {
                if (resourceDao.getResourceCount() == 0) {
                    resourceDao.upsertResources(getInitialSeedResources())
                }
            } catch (_: Exception) {}
            Result.success(Unit)
        }
    }

    override suspend fun deleteOldResources(thresholdTimestamp: Long) {
        resourceDao.deleteOldResources(thresholdTimestamp)
    }

    override fun updateNetworkConnectivity(isConnected: Boolean) {
        if (!isConnected) {
            _syncStatus.value = SyncStatus.Offline
        } else {
            if (_syncStatus.value is SyncStatus.Offline) {
                _syncStatus.value = SyncStatus.Idle
            }
        }
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
                title = "Digital Literacy 101",
                description = "Master computer and mobile basics: typing, navigation, browsing the internet, sending emails, and using government online portals.",
                category = "Digital Skills",
                type = "LEARN",
                language = "en",
                contentUrl = "https://www.dcdt.gov.za",
                isOfflineAvailable = true,
                lastUpdated = now
            ),
            ResourceEntity(
                id = "learn-2",
                title = "Basic Smartphone Skills",
                description = "Learn how to connect to Wi-Fi, manage mobile data, use WhatsApp safely, install apps from the Play Store, and adjust accessibility settings.",
                category = "Digital Skills",
                type = "LEARN",
                language = "en",
                contentUrl = "https://www.gov.za",
                isOfflineAvailable = true,
                lastUpdated = now
            ),
            ResourceEntity(
                id = "learn-3",
                title = "Introduction to Job Searching Online",
                description = "Step-by-step guidance on how to create a professional CV, register on SA Youth (sayouth.mobi), and apply for job opportunities online.",
                category = "Job Skills",
                type = "LEARN",
                language = "en",
                contentUrl = "https://sayouth.mobi",
                isOfflineAvailable = true,
                lastUpdated = now
            ),
            ResourceEntity(
                id = "learn-4",
                title = "Online Safety & Scam Prevention",
                description = "Protect yourself from phishing, fake SMS scams, fraudulent job offers, and secure your personal banking and login details.",
                category = "Digital Skills",
                type = "LEARN",
                language = "en",
                contentUrl = "https://www.cybercrime.org.za",
                isOfflineAvailable = true,
                lastUpdated = now
            ),
            ResourceEntity(
                id = "learn-5",
                title = "Basic Budgeting & Mobile Banking",
                description = "Learn how to track household expenses, use USSD codes (*120#) or banking apps safely, and manage your money efficiently.",
                category = "Finance",
                type = "LEARN",
                language = "en",
                contentUrl = "https://www.gov.za",
                isOfflineAvailable = true,
                lastUpdated = now
            ),
            ResourceEntity(
                id = "learn-6",
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
                id = "learn-7",
                title = "Understanding SASSA Grants & Online Status Check (IsiXhosa)",
                description = "Isikhokelo esipheleleyo sokufaka isicelo se-SRD 370, iSondlo saBantwana, kunye nokujonga imihla yokuhlawulwa.",
                category = "Social Grants",
                type = "LEARN",
                language = "xh",
                contentUrl = "https://srd.sassa.gov.za",
                isOfflineAvailable = true,
                lastUpdated = now
            ),

            // Services Resources
            ResourceEntity(
                id = "service-1",
                title = "SASSA Social Grants (SRD R350, Child Support, Old Age)",
                description = "Apply for and manage Social Relief of Distress (SRD R350/R370), Child Support grants, Disability grants, and Old Age pensions.",
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
                id = "service-2",
                title = "Department of Home Affairs - Smart ID & Passport",
                description = "Book appointments via eHomeAffairs for Smart ID card applications, newborn birth registrations, and passport renewals.",
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
                id = "service-3",
                title = "SARS eFiling Basics & Tax Services",
                description = "Register for tax numbers, file annual income tax returns, and access support for tax compliance and refunds.",
                category = "Tax & Finance",
                type = "SERVICE",
                language = "en",
                contactNumber = "0800 00 7277",
                address = "SARS Offices Nationwide",
                contentUrl = "https://www.sars.gov.za",
                isOfflineAvailable = true,
                lastUpdated = now
            ),
            ResourceEntity(
                id = "service-4",
                title = "Municipal Services (Water & Electricity Billing/Faults)",
                description = "Report water pipe bursts, power outages, street light faults, and manage municipal utility billing accounts.",
                category = "Municipal Services",
                type = "SERVICE",
                language = "en",
                contactNumber = "0860 562 874",
                address = "Local City Customer Care Centres",
                contentUrl = "https://www.salga.org.za",
                isOfflineAvailable = true,
                lastUpdated = now
            ),
            ResourceEntity(
                id = "service-5",
                title = "Public Healthcare Clinics Directory",
                description = "Find your nearest public primary healthcare clinic for free consultations, immunizations, and chronic medication collection.",
                category = "Healthcare",
                type = "SERVICE",
                language = "en",
                contactNumber = "0800 029 999",
                address = "Local Municipal Primary Healthcare Clinics",
                contentUrl = "https://www.health.gov.za",
                isOfflineAvailable = true,
                lastUpdated = now
            ),

            // Get Help Resources (Emergency Hotlines & Support)
            ResourceEntity(
                id = "help-1",
                title = "National Emergency Number",
                description = "Free 24/7 toll-free emergency call center for police, ambulance, fire department, and rescue from any mobile phone.",
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
                title = "SAPS Police Emergency",
                description = "South African Police Service 24/7 emergency response line for reporting crimes in progress and urgent safety assistance.",
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
                description = "24/7 confidential helpline providing professional counseling, shelter placement, and emergency support for GBV survivors.",
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
                title = "SASSA Toll-Free Helpline",
                description = "Direct toll-free helpline for social grant enquiries, payment status, appeal assistance, and social relief queries.",
                category = "Social Support",
                type = "HELP",
                language = "en",
                contactNumber = "0800 60 10 11",
                address = "SASSA National Office",
                isOfflineAvailable = true,
                lastUpdated = now
            ),
            ResourceEntity(
                id = "help-5",
                title = "SADAG Mental Health Helpline",
                description = "South African Depression and Anxiety Group 24-hour mental health support, suicide crisis line, and psychological counseling.",
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
                description = "Toll-free 24/7 crisis hotline dedicated to protecting children, offering counseling, and reporting child abuse.",
                category = "Helpline",
                type = "HELP",
                language = "en",
                contactNumber = "116",
                address = "Childline South Africa",
                isOfflineAvailable = true,
                lastUpdated = now
            ),

            // Additional Official SA Languages Support
            ResourceEntity(
                id = "lang-af-1",
                title = "Basiese Digitale Vaardighede & Mobiele Veiligheid",
                description = "Stap-vir-stap gids vir e-pos, persoonlike data beskerming, en aanlyn sekuriteit in Suid-Afrika.",
                category = "Digital Skills",
                type = "LEARN",
                language = "af",
                contentUrl = "https://www.dcdt.gov.za",
                isOfflineAvailable = true,
                lastUpdated = now
            ),
            ResourceEntity(
                id = "lang-nso-1",
                title = "Tlhahlobo ya Ditsireletso ya Dijithale le Merero ya SASSA",
                description = "Ditaelo tša go šomiša maruiwa a dijithale le go hwetša ditireletso tša mmušo ka Sepedi.",
                category = "Social Grants",
                type = "LEARN",
                language = "nso",
                contentUrl = "https://www.sassa.gov.za",
                isOfflineAvailable = true,
                lastUpdated = now
            ),
            ResourceEntity(
                id = "lang-st-1",
                title = "Tataiso ya Dijithale le Tshireletso ya Mehala",
                description = "Tataiso e felletseng ya tshireletso ya dijithale le tlhahiso leseding ya litshebeletso tsa setjhaba.",
                category = "Digital Skills",
                type = "LEARN",
                language = "st",
                contentUrl = "https://www.gov.za",
                isOfflineAvailable = true,
                lastUpdated = now
            ),
            ResourceEntity(
                id = "lang-tn-1",
                title = "Kgakololo ya Dijitale le Tshireletsego ya Dinoko",
                description = "Ditsela tsa go sireletsa tshedimosetso ya gago le go dirisa ditireletso tsa mmuuso ka Setswana.",
                category = "Digital Skills",
                type = "LEARN",
                language = "tn",
                contentUrl = "https://www.gov.za",
                isOfflineAvailable = true,
                lastUpdated = now
            ),
            ResourceEntity(
                id = "lang-ts-1",
                title = "Vulekelisi bya Matimba ya Dijithale",
                description = "Nongonoko wa vuhlayiseki bya swivumbiwa swa dijithale na vuxokoxoko bya migingiriko ya mfumo.",
                category = "Digital Skills",
                type = "LEARN",
                language = "ts",
                contentUrl = "https://www.gov.za",
                isOfflineAvailable = true,
                lastUpdated = now
            ),
            ResourceEntity(
                id = "lang-ss-1",
                title = "Tinkomba tekwakha telwati lwetedijithali",
                description = "Umtsetfo wekuvikela imininingwane yakho nekusebentisa tindlela tetindiza telwati tahulumende.",
                category = "Digital Skills",
                type = "LEARN",
                language = "ss",
                contentUrl = "https://www.gov.za",
                isOfflineAvailable = true,
                lastUpdated = now
            ),
            ResourceEntity(
                id = "lang-ve-1",
                title = "Vhulangi ha Digital na Vhaendedzi ha Muvhuso",
                description = "Nditsheletsho ya u tsireledza data ya vhane na u wana mvelelo ya vhuvhalelani ha Muvhuso.",
                category = "Digital Skills",
                type = "LEARN",
                language = "ve",
                contentUrl = "https://www.gov.za",
                isOfflineAvailable = true,
                lastUpdated = now
            ),
            ResourceEntity(
                id = "lang-nr-1",
                title = "Umhlahlandlela wamaDigital Skills",
                description = "Imiyalelo yokuphepha ku-intanethi kanye nokusebenzisa i-e-services kahulumeni.",
                category = "Digital Skills",
                type = "LEARN",
                language = "nr",
                contentUrl = "https://www.gov.za",
                isOfflineAvailable = true,
                lastUpdated = now
            ),

            // Universities & Higher Education, Employment, and Healthcare Resources
            ResourceEntity(
                id = "edu-nsfas-1",
                title = "NSFAS Student Funding Application Guide",
                description = "Complete step-by-step guidance on how to apply for NSFAS bursaries, required documents (ID, parental income proof, academic results), and tracking application status for public universities and TVET colleges.",
                category = "Education",
                type = "LEARN",
                language = "en",
                contactNumber = "0800 067 327",
                address = "NSFAS Head Office, Wynberg, Cape Town & Online Portal",
                contentUrl = "https://www.nsfas.org.za",
                isOfflineAvailable = true,
                lastUpdated = now
            ),
            ResourceEntity(
                id = "edu-nsfas-zu",
                title = "Umhlahlandlela Wokufaka Isicelo Sesibonelelo Sezimali Sase-NSFAS (IsiZulu)",
                description = "Isikhokelo esipheleleyo sendlela yokufaka isicelo sezibonelelo ze-NSFAS, imibhalo edingekayo, nokulandela isimo sesicelo sakho samaNyuvesi nakuma-TVET.",
                category = "Education",
                type = "LEARN",
                language = "zu",
                contactNumber = "0800 067 327",
                address = "NSFAS Head Office, Wynberg, Cape Town & Online Portal",
                contentUrl = "https://www.nsfas.org.za",
                isOfflineAvailable = true,
                lastUpdated = now
            ),
            ResourceEntity(
                id = "serv-unis-1",
                title = "Public Universities Directory in South Africa",
                description = "Directory and application portals for South African public universities including Wits, UCT, UP, UKZN, UJ, NWU, Rhodes, Stellenbosch, UWC, Unisa, and CUT. Access admissions, faculty guides, and prospectus links.",
                category = "Education",
                type = "SERVICE",
                language = "en",
                contactNumber = "012 441 5700",
                address = "Universities South Africa (USAf), Pretoria",
                contentUrl = "https://www.usaf.ac.za",
                isOfflineAvailable = true,
                lastUpdated = now
            ),
            ResourceEntity(
                id = "learn-tvet-1",
                title = "TVET Colleges Application & Career Guide",
                description = "Explore vocational training programs, NCV and Nated courses, admission requirements, and financial aid across 50 public Technical and Vocational Education and Training (TVET) colleges in South Africa.",
                category = "Education",
                type = "LEARN",
                language = "en",
                contactNumber = "0800 22 67 27",
                address = "Department of Higher Education and Training (DHET) Nationwide",
                contentUrl = "https://www.dhet.gov.za",
                isOfflineAvailable = true,
                lastUpdated = now
            ),
            ResourceEntity(
                id = "serv-yes-1",
                title = "Youth Employment Service (YES) Hubs & Work Experience",
                description = "Discover YES Hubs near you offering 12-month quality work experiences, digital skills training, and employment placement opportunities for unemployed South African youth aged 18-29.",
                category = "Employment",
                type = "SERVICE",
                language = "en",
                contactNumber = "011 280 6800",
                address = "YES Hubs Nationwide (Johannesburg, Durban, Cape Town, etc.)",
                contentUrl = "https://www.yes4youth.co.za",
                isOfflineAvailable = true,
                lastUpdated = now
            ),
            ResourceEntity(
                id = "serv-essa-1",
                title = "Department of Labour ESSA Job & Career Services",
                description = "Register on the Employment Services South Africa (ESSA) database to access job seeker matching, career counseling, UIF registration support, and vocational training opportunities at local Labour Centres.",
                category = "Employment",
                type = "SERVICE",
                language = "en",
                contactNumber = "012 337 5000",
                address = "Provincial and Labour Centres Nationwide",
                contentUrl = "https://www.labour.gov.za",
                isOfflineAvailable = true,
                lastUpdated = now
            ),
            ResourceEntity(
                id = "help-health-1",
                title = "National Health Hotline & COVID-19 / Public Health Enquiries",
                description = "24/7 toll-free public health hotline for medical advice, vaccine information, disease outbreak alerts, health facility enquiries, and emergency medical coordination.",
                category = "Healthcare",
                type = "HELP",
                language = "en",
                contactNumber = "0800 029 999",
                address = "National Department of Health, Pretoria",
                contentUrl = "https://www.health.gov.za",
                isOfflineAvailable = true,
                lastUpdated = now
            ),
            ResourceEntity(
                id = "serv-clinic-1",
                title = "Local Primary Healthcare Clinic Locator",
                description = "Locate your nearest public primary healthcare clinic, community health center (CHC), or district hospital for free consultations, immunizations, maternal care, and chronic medication pickup.",
                category = "Healthcare",
                type = "SERVICE",
                language = "en",
                contactNumber = "0800 029 999",
                address = "Local Municipal Primary Healthcare Clinics Nationwide",
                contentUrl = "https://www.health.gov.za",
                isOfflineAvailable = true,
                lastUpdated = now
            ),
            ResourceEntity(
                id = "serv-clinic-xh",
                title = "Isikhokelo seZibhedlele neZiko lezeMpilo (IsiXhosa)",
                description = "Fumana ikliniki okanye isibhedlele saseburhulumente esikufutshane nawe ukuze ufumane uncedo lwezonyango, amayeza, kunye nogonyo.",
                category = "Healthcare",
                type = "SERVICE",
                language = "xh",
                contactNumber = "0800 029 999",
                address = "Local Municipal Primary Healthcare Clinics Nationwide",
                contentUrl = "https://www.health.gov.za",
                isOfflineAvailable = true,
                lastUpdated = now
            )
        )
    }
}
