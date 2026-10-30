package com.example.accessbridge.ui.home

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.accessbridge.data.local.entity.ResourceEntity
import com.example.accessbridge.ui.components.CategoryFilterChips
import com.example.accessbridge.ui.components.EmergencyContactCard
import com.example.accessbridge.ui.components.LanguageSelector
import com.example.accessbridge.ui.components.ResourceCard
import com.example.accessbridge.ui.components.SectionTitle
import com.example.accessbridge.ui.components.SyncStatusBanner
import com.example.accessbridge.ui.theme.AccessBridgeTheme

val serviceCategories = listOf("All", "Healthcare", "Home Affairs", "Social Grants", "Municipal Services")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAuth: () -> Unit,
    onNavigateToLearn: () -> Unit,
    onNavigateToServices: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToDetail: (String) -> Unit = {},
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "AccessBridge Hub",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            )
                        )
                    },
                    actions = {
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

            Spacer(modifier = Modifier.height(8.dp))

            // Main Content Area
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
                            category = "Emergency"
                        )
                    }

                    item {
                        EmergencyContactCard(
                            title = "SAPS Crime Stop & Police Emergency",
                            phoneNumber = "10111",
                            description = "South African Police Service 24/7 response for immediate emergency response.",
                            category = "Police"
                        )
                    }

                    item {
                        EmergencyContactCard(
                            title = "Gender-Based Violence Command Centre",
                            phoneNumber = "0800 150 150",
                            description = "Support, crisis intervention, and shelter help for GBV survivors.",
                            category = "Helpline"
                        )
                    }

                    item {
                        EmergencyContactCard(
                            title = "SASSA Social Services Helpline",
                            phoneNumber = "0800 60 10 11",
                            description = "Official helpline for grant status, SRD queries, and emergency relief.",
                            category = "Social Grants"
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
