package com.rige.extensions

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest

fun SupabaseClient.requireUserId(): String {
    return this.auth.currentUserOrNull()?.id
        ?: throw IllegalStateException("Usuario no autenticado")
}

suspend inline fun <reified T> SupabaseClient.selectById(
    table: String,
    id: String,
    idColumn: String = "id"
): T? {
    return this.postgrest.from(table)
        .select {
            filter { eq(idColumn, id) }
        }
        .decodeSingleOrNull()
}