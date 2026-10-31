package com.example.accessbridge.ui.home

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.accessbridge.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.accessbridge.data.local.entity.ResourceEntity
import com.example.accessbridge.ui.components.CategoryFilterChips
import com.example.accessbridge.ui.components.EmergencyContactCard
import com.example.accessbridge.ui.components.LanguageSelector
import com.example.accessbridge.ui.components.ResourceCard
import com.example.accessbridge.ui.components.SALanguages
import com.example.accessbridge.ui.components.SectionTitle
import com.example.accessbridge.ui.components.SyncStatusBanner
import com.example.accessbridge.ui.theme.AccessBridgeTheme

val serviceCategories = listOf("All", "Healthcare", "Home Affairs", "Social Grants", "Municipal Services", "Education", "Employment")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAuth: () -> Unit,
    onNavigateToLearn: () -> Unit,
    onNavigateToServices: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToTranslator: () -> Unit,
    onNavigateToDetail: (String) -> Unit = {},
    onSignOut: () -> Unit = onNavigateToAuth,
    viewModel: ResourceViewModel = viewModel(
        factory = ResourceViewModel.provideFactory(LocalContext.current)
    ),
    modifier: Modifier = Modifier
) {
    val resources by viewModel.resources.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val lastSyncedTimestamp by viewModel.lastSyncedTimestamp.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val currentUserEmail by viewModel.currentUserEmail.collectAsState()
    val isGuestMode by viewModel.isGuestMode.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_access_bridge_logo),
                                contentDescription = "AccessBridge Logo",
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "AccessBridge",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                )
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateToTranslator) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = "Translator & Phrasebook",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        LanguageSelector(
                            selectedLanguageCode = selectedLanguage,
                            onLanguageSelected = { viewModel.onLanguageSelected(it) }
                        )
                        IconButton(onClick = onNavigateToAuth) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Account / Auth",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // Sync Status Indicator Banner
                SyncStatusBanner(
                    syncStatus = syncStatus,
                    lastSyncedTimestamp = lastSyncedTimestamp,
                    onSyncNow = { viewModel.syncResources() }
                )
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == "ALL",
                    onClick = { viewModel.onTabSelected("ALL") },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Hub") }
                )
                NavigationBarItem(
                    selected = selectedTab == "LEARN",
                    onClick = { viewModel.onTabSelected("LEARN") },
                    icon = { Icon(Icons.Default.School, contentDescription = "Learn") },
                    label = { Text("Learn") }
                )
                NavigationBarItem(
                    selected = selectedTab == "SERVICE",
                    onClick = { viewModel.onTabSelected("SERVICE") },
                    icon = { Icon(Icons.Default.Work, contentDescription = "Services") },
                    label = { Text("Services") }
                )
                NavigationBarItem(
                    selected = selectedTab == "HELP",
                    onClick = { viewModel.onTabSelected("HELP") },
                    icon = { Icon(Icons.Default.Help, contentDescription = "Get Help") },
                    label = { Text("Get Help") }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // User Profile Banner / Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isGuestMode || currentUserEmail.startsWith("Guest")) "Guest Mode" else "Logged in as:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (isGuestMode || currentUserEmail.isBlank()) "Guest User" else currentUserEmail,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    TextButton(
                        onClick = {
                            viewModel.signOut()
                            onSignOut()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Sign Out",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isGuestMode) "Sign In" else "Sign Out",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Search Bar
            PaddingValues(horizontal = 16.dp, vertical = 8.dp).let { searchPadding ->
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = { Text("Search services, guides, hotlines...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(searchPadding),
                    shape = RoundedCornerShape(14.dp)
                )
            }

            // Category Filter Chips for Services/Hub
            if (selectedTab == "ALL" || selectedTab == "SERVICE") {
                CategoryFilterChips(
                    categories = serviceCategories,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { viewModel.onCategorySelected(it) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // Active Language Indicator
            if (selectedLanguage != "All") {
                val activeLangName = SALanguages.find { it.first == selectedLanguage }?.second ?: selectedLanguage
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Active Language: $activeLangName (showing localized & English content)",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Content Area
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Translator Banner Card on Hub / All tab
                if (selectedTab == "ALL" && searchQuery.isBlank() && selectedCategory == "All") {
                    item {
                        Card(
                            onClick = onNavigateToTranslator,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "🗣️ Offline Translator & Phrasebook",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Translate public service, emergency, healthcare & university terms across all 11 official SA languages offline.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Translate,
                                    contentDescription = "Open Translator",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // If Get Help tab is active or in Hub view, show Emergency Cards section
                if (selectedTab == "HELP" || (selectedTab == "ALL" && searchQuery.isBlank() && selectedCategory == "All")) {
                    item {
                        SectionTitle(text = "24/7 Emergency Hotlines")
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    item {
                        EmergencyContactCard(
                            title = "National Emergency Hotline",
                            phoneNumber = "112",
                            description = "Free 24/7 toll-free emergency call line for police, medical, and fire rescue.",
                            category = "Emergency",
                            onClick = { onNavigateToDetail("help-1") }
                        )
                    }

                    item {
                        EmergencyContactCard(
                            title = "SAPS Crime Stop & Police Emergency",
                            phoneNumber = "10111",
                            description = "South African Police Service 24/7 response for immediate emergency response.",
                            category = "Police",
                            onClick = { onNavigateToDetail("help-2") }
                        )
                    }

                    item {
                        EmergencyContactCard(
                            title = "Gender-Based Violence Command Centre",
                            phoneNumber = "0800 150 150",
                            description = "Support, crisis intervention, and shelter help for GBV survivors.",
                            category = "Helpline",
                            onClick = { onNavigateToDetail("help-3") }
                        )
                    }

                    item {
                        EmergencyContactCard(
                            title = "SASSA Social Services Helpline",
                            phoneNumber = "0800 60 10 11",
                            description = "Official helpline for grant status, SRD queries, and emergency relief.",
                            category = "Social Grants",
                            onClick = { onNavigateToDetail("help-4") }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        SectionTitle(text = if (selectedTab == "HELP") "All Support Resources" else "Public Services & Learning Guides")
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                if (resources.isEmpty()) {
                    item {
                        Text(
                            text = "No matching resources found. Try adjusting your search query or language filter.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    }
                } else {
                    items(resources, key = { it.id }) { resource ->
                        ResourceCard(
                            resource = resource,
                            onClick = { onNavigateToDetail(resource.id) }
                        )
                    }
                }
            }
        }
    }
}
