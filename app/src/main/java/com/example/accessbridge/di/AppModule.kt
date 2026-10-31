package com.example.accessbridge.di

import android.content.Context
import com.example.accessbridge.data.local.AccessBridgeDatabase
import com.example.accessbridge.data.local.dao.ResourceDao
import com.example.accessbridge.data.remote.NetworkConnectivityObserver
import com.example.accessbridge.data.remote.NetworkConnectivityObserverImpl
import com.example.accessbridge.data.remote.SupabaseClientProvider
import com.example.accessbridge.data.repository.ResourceRepository
import com.example.accessbridge.data.repository.ResourceRepositoryImpl
import io.github.jan.supabase.SupabaseClient

/**
 * Dependency Injection / Service Locator module providing database, DAO, Supabase client, network observer, and repository singletons.
 */
object AppModule {

    @Volatile
    private var resourceRepositoryInstance: ResourceRepository? = null

    @Volatile
    private var networkConnectivityObserverInstance: NetworkConnectivityObserver? = null

    fun provideDatabase(context: Context): AccessBridgeDatabase {
        return AccessBridgeDatabase.getDatabase(context)
    }

    fun provideResourceDao(context: Context): ResourceDao {
        return provideDatabase(context).resourceDao()
    }

    fun provideSupabaseClient(): SupabaseClient {
        return SupabaseClientProvider.client
    }

    fun provideNetworkConnectivityObserver(context: Context): NetworkConnectivityObserver {
        return networkConnectivityObserverInstance ?: synchronized(this) {
            val instance = NetworkConnectivityObserverImpl(context.applicationContext)
            networkConnectivityObserverInstance = instance
            instance
        }
    }

    fun provideResourceRepository(context: Context): ResourceRepository {
        return resourceRepositoryInstance ?: synchronized(this) {
            val instance = ResourceRepositoryImpl(
                resourceDao = provideResourceDao(context),
                supabaseClient = provideSupabaseClient()
            )
            resourceRepositoryInstance = instance
            instance
        }
    }
}
