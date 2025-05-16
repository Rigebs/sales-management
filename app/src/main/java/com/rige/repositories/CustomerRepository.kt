package com.rige.repositories

import com.rige.models.Customer
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class CustomerRepository(private val client: SupabaseClient) {

    suspend fun getCustomers(): List<Customer> {
        return client.postgrest["customers"]
            .select()
            .decodeList<Customer>()
    }
}