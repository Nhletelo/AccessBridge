package com.example.accessbridge.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.accessbridge.data.local.entity.ResourceEntity
import com.example.accessbridge.data.remote.NetworkConnectivityObserver
import com.example.accessbridge.data.remote.SupabaseClientProvider
import com.example.accessbridge.data.repository.ResourceRepository
import com.example.accessbridge.di.AppModule
import com.example.accessbridge.domain.model.SyncStatus
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ResourceViewModel(
    private val resourceRepository: ResourceRepository,
    private val networkConnectivityObserver: NetworkConnectivityObserver? = null
) : ViewModel() {

    val syncStatus: StateFlow<SyncStatus> = resourceRepository.syncStatus
    val lastSyncedTimestamp: StateFlow<Long?> = resourceRepository.lastSyncedTimestamp

    private val _selectedTab = MutableStateFlow("ALL") // "ALL", "LEARN", "SERVICE", "HELP"
    val selectedTab: StateFlow<String> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("All")
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    private val _currentUserEmail = MutableStateFlow("Guest User")
    val currentUserEmail: StateFlow<String> = _currentUserEmail.asStateFlow()

    private val _isGuestMode = MutableStateFlow(true)
    val isGuestMode: StateFlow<Boolean> = _isGuestMode.asStateFlow()

    init {
        syncResources()
        checkCurrentUser()
        observeNetworkConnectivity()
    }

    private fun observeNetworkConnectivity() {
        networkConnectivityObserver?.let { observer ->
            viewModelScope.launch {
                observer.isConnected.collect { isConnected ->
                    val wasOffline = syncStatus.value is SyncStatus.Offline
                    resourceRepository.updateNetworkConnectivity(isConnected)
                    if (isConnected && wasOffline) {
                        syncResources()
                    }
                }
            }
        }
    }

    fun checkCurrentUser() {
        viewModelScope.launch {
            try {
                val supabase = SupabaseClientProvider.client
                val user = supabase.auth.currentUserOrNull()
                if (user != null && !user.email.isNullOrBlank()) {
                    _currentUserEmail.value = user.email ?: "Guest User"
                    _isGuestMode.value = false
                } else {
                    _currentUserEmail.value = "Guest User"
                    _isGuestMode.value = true
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _currentUserEmail.value = "Guest User"
                _isGuestMode.value = true
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                val supabase = SupabaseClientProvider.client
                supabase.auth.signOut()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
            _currentUserEmail.value = "Guest User"
            _isGuestMode.value = true
        }
    }

    val resources: StateFlow<List<ResourceEntity>> = combine(
        _selectedTab,
        _searchQuery,
        _selectedCategory,
        _selectedLanguage
    ) { tab, query, category, lang ->
        FilterState(tab, query, category, lang)
    }.flatMapLatest { filter ->
        if (filter.query.isNotBlank()) {
            resourceRepository.searchResourcesByLanguage(filter.query, filter.language)
        } else if (filter.category != "All") {
            resourceRepository.getResourcesByCategoryAndLanguage(filter.category, filter.language)
        } else if (filter.tab != "ALL") {
            resourceRepository.getResourcesByTypeAndLanguage(filter.tab, filter.language)
        } else {
            resourceRepository.getAllResourcesByLanguage(filter.language)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun getResourceById(id: String): Flow<ResourceEntity?> {
        return resourceRepository.getResourceById(id)
    }

    fun onTabSelected(tab: String) {
        _selectedTab.value = tab
        _selectedCategory.value = "All"
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: String) {
        _selectedCategory.value = category
    }

    fun onLanguageSelected(languageCode: String) {
        _selectedLanguage.value = languageCode
    }

    fun syncResources() {
        viewModelScope.launch {
            resourceRepository.syncResources()
        }
    }

    private data class FilterState(
        val tab: String,
        val query: String,
        val category: String,
        val language: String
    )

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val repo = AppModule.provideResourceRepository(context.applicationContext)
                    val observer = AppModule.provideNetworkConnectivityObserver(context.applicationContext)
                    return ResourceViewModel(repo, observer) as T
                }
            }
    }
}
