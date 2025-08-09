package com.rige.repositories

import com.rige.models.Profile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId

class ProfileRepository(private val client: SupabaseClient) {

    suspend fun validateCurrentUserAccess(userId: String): Boolean {
        println("Validating access for user: $userId")
        return try {
            val profile = client.postgrest
                .from("profiles")
                .select()
                .decodeSingleOrNull<Profile>()

            println("Profile: $profile")

            if (profile == null) {
                false
            } else {
                val nowInstant = Instant.now()

                val expiresAtInstant = profile.expiresAt
                    ?.atZone(ZoneId.of("UTC"))
                    ?.toInstant()

                println("Expires at: $expiresAtInstant")
                println("Now: $nowInstant")

                profile.isActive == true && expiresAtInstant?.isAfter(nowInstant) == true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}