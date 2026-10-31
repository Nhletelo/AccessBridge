package com.example.accessbridge.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Onboarding : Screen

    @Serializable
    data object Auth : Screen

    @Serializable
    data object Home : Screen

    @Serializable
    data object Learn : Screen

    @Serializable
    data object Services : Screen

    @Serializable
    data object Help : Screen

    @Serializable
    data object Translator : Screen

    @Serializable
    data class ResourceDetail(val resourceId: String) : Screen
}
