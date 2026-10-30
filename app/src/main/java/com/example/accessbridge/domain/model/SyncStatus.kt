package com.example.accessbridge.domain.model

/**
 * State representing the synchronization status.
 */
sealed interface SyncStatus {
    object Idle : SyncStatus
    object Syncing : SyncStatus
    object Success : SyncStatus
    data class Error(val message: String) : SyncStatus
}
