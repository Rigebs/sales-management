package com.rige.clients

import com.rige.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.delay

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY
    ) {
        install(Postgrest)
        install(Auth) {
            autoLoadFromStorage = true
            alwaysAutoRefresh = true
        }
    }

    fun waitForSupabaseSession(timeoutMs: Long = 5000L): UserInfo? {
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < timeoutMs) {
            val user = client.auth.currentUserOrNull()
            if (user != null) return user
        }
        return null
    }

}
