package com.example.accessbridge.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

/**
 * Singleton provider for Supabase Client configured with Auth, Postgrest, and Storage plugins.
 */
object SupabaseClientProvider {
    // Configurable placeholders for Supabase URL and anon key
    var supabaseUrl: String = "https://your-supabase-project.supabase.co"
    var supabaseKey: String = "your-anon-key-placeholder"

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseKey
        ) {
            install(Auth)
            install(Postgrest)
            install(Storage)
        }
    }
}
