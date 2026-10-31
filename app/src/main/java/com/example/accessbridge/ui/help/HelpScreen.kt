package com.example.accessbridge.ui.help

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.accessbridge.ui.components.EmergencyContactCard
import com.example.accessbridge.ui.components.ResourceCard
import com.example.accessbridge.ui.components.SectionTitle
import com.example.accessbridge.ui.home.ResourceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToDetail: (String) -> Unit = {},
    viewModel: ResourceViewModel = viewModel(
        factory = ResourceViewModel.provideFactory(LocalContext.current)
    )
) {
    LaunchedEffect(Unit) {
        viewModel.onTabSelected("HELP")
    }

    val resources by viewModel.resources.collectAsState()
    val helpResources = resources.filter { it.type == "HELP" }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Emergency & Get Help",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionTitle(text = "1-Touch Emergency Hotlines")
                Text(
                    text = "Tap any card for details or tap the call icon for immediate 1-touch dial across South Africa.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                EmergencyContactCard(
                    title = "National Emergency Hotline",
                    phoneNumber = "112",
                    description = "Free 24/7 call line for police, medical assistance, and fire rescue.",
                    category = "Emergency",
                    onClick = { onNavigateToDetail("help-1") }
                )
            }

            item {
                EmergencyContactCard(
                    title = "SAPS Crime Stop & Police Emergency",
                    phoneNumber = "10111",
                    description = "South African Police Service 24/7 emergency hotline.",
                    category = "Police",
                    onClick = { onNavigateToDetail("help-2") }
                )
            }

            item {
                EmergencyContactCard(
                    title = "Gender-Based Violence (GBV) Command Centre",
                    phoneNumber = "0800 150 150",
                    description = "24/7 confidential crisis line, counseling & shelter assistance.",
                    category = "Helpline",
                    onClick = { onNavigateToDetail("help-3") }
                )
            }

            item {
                EmergencyContactCard(
                    title = "SASSA Social Services Helpline",
                    phoneNumber = "0800 60 10 11",
                    description = "Official helpline for social grant status & emergency relief.",
                    category = "Social Grants",
                    onClick = { onNavigateToDetail("help-4") }
                )
            }

            item {
                EmergencyContactCard(
                    title = "SADAG Mental Health Crisis Line",
                    phoneNumber = "0800 567 567",
                    description = "24-hour mental health counseling and crisis support.",
                    category = "Mental Health",
                    onClick = { onNavigateToDetail("help-5") }
                )
            }

            item {
                EmergencyContactCard(
                    title = "Childline South Africa",
                    phoneNumber = "116",
                    description = "Free 24/7 hotline for child safety, protection, and family support.",
                    category = "Child Welfare",
                    onClick = { onNavigateToDetail("help-6") }
                )
            }

            if (helpResources.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionTitle(text = "Additional Support Directories")
                }

                items(helpResources, key = { it.id }) { resource ->
                    ResourceCard(
                        resource = resource,
                        onClick = { onNavigateToDetail(resource.id) }
                    )
                }
            }
        }
    }
}
