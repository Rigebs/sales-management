package com.rige.extensions

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth

fun SupabaseClient.requireUserId(): String {
    return this.auth.currentUserOrNull()?.id
        ?: throw IllegalStateException("Usuario no autenticado")
}