package com.rige.repositories

import com.rige.extensions.requireUserId
import com.rige.models.Barcode
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class BarcodeRepository(private val client: SupabaseClient) {

    suspend fun findByProductId(productId: String): List<Barcode> {
        val list = client.postgrest
            .from("barcodes")
            .select {
                filter {
                    eq("product_id", productId)
                }
            }
            .decodeList<Barcode>()

        println("🔍 Supabase Fetch -> Barcodes: $list")
        return list
    }

    suspend fun save(barcode: Barcode) {
        val userId = client.requireUserId()
        val barcodeWithUser = barcode.copy(userId = userId)

        client.postgrest.from("barcodes")
            .insert(barcodeWithUser)
    }

    suspend fun saveAll(barcodes: List<Barcode>) {
        val userId = client.requireUserId()
        val barcodesWithUser = barcodes.map { it.copy(userId = userId) }

        client.postgrest
            .from("barcodes")
            .insert(barcodesWithUser)
    }

    suspend fun deleteById(barcodeId: String) {
        try {
            val result = client.postgrest.from("barcodes")
                .delete {
                    filter {
                        eq("id", barcodeId)
                    }
                }
            println("✅ Registro con ID $barcodeId eliminado correctamente.")
            println("Response: $result")
        } catch (e: Exception) {
            println("❌ Error al eliminar el registro con ID $barcodeId: ${e.message}")
        }
    }
}