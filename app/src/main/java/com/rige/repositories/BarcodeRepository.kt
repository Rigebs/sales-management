package com.rige.repositories

import com.rige.models.Barcode
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class BarcodeRepository(private val client: SupabaseClient) {

    suspend fun findByProductId(productId: String): List<Barcode> {
        return try {
            client.postgrest.from("barcodes")
                .select {
                    filter {
                        eq("product_id", productId)
                    }
                }
                .decodeList<Barcode>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveAll(barcodes: List<Barcode>) {
        client.postgrest
            .from("barcodes")
            .insert(barcodes)
    }
}