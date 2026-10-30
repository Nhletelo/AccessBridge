package com.example.accessbridge.data.repository

import com.example.accessbridge.data.local.LocalDataSource
import com.example.accessbridge.data.remote.SupabaseClientProvider

class AccessBridgeRepositoryImpl(
    private val localDataSource: LocalDataSource,
    private val supabaseClientProvider: SupabaseClientProvider
)
