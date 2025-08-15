package com.rige.repositories

import com.rige.extensions.requireUserId
import com.rige.models.Category
import com.rige.models.Supplier
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class SupplierRepository(private val client: SupabaseClient) {

    suspend fun findAll(): List<Supplier> {
        val suppliers = client.postgrest.from("suppliers")
            .select()
            .decodeList<Supplier>()

        if (suppliers.isNotEmpty()) {
            println("✅ Se encontraron ${suppliers.size} proveedores.")
        } else {
            println("⚠️ No se encontraron proveedores.")
        }

        return suppliers
    }

    suspend fun save(supplier: Supplier) {
        val userId = client.requireUserId()
        val supplierWithUser = supplier.copy(userId = userId)

        client.postgrest.from("suppliers")
            .insert(supplierWithUser)
    }

    suspend fun findById(id: String): Supplier? {
        return client.postgrest.from("suppliers")
            .select {
                filter {
                    eq("id", id)
                }
            }
            .decodeSingleOrNull()
    }

    suspend fun update(supplier: Supplier) {
        client.postgrest.from("suppliers")
            .update(supplier) {
                filter {
                    eq("id", supplier.id)
                }
            }
    }
}