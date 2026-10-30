package com.example.accessbridge.data.remote

import io.github.jan.supabase.SupabaseClient

/**
 * Helper accessor for the initialized SupabaseClient singleton instance.
 */
object SupabaseClient {
    fun getInstance(): SupabaseClient = SupabaseClientProvider.client
}
