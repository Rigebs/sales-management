package com.rige.repositories

import com.rige.models.Customer
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class CustomerRepository(private val client: SupabaseClient) {

    suspend fun findAll(): List<Customer> {
        return client.postgrest.from("customers")
            .select()
            .decodeList()
    }

    suspend fun findById(id: String): Customer? {
        return client.postgrest.from("customers")
            .select {
                filter {
                    eq("id", id)
                }
            }
            .decodeSingleOrNull()
    }

    suspend fun save(customer: Customer) {
        client.postgrest.from("customers")
            .insert(customer)
    }

    suspend fun update(customer: Customer) {
        client.postgrest.from("customers")
            .update(customer) {
                filter {
                    eq("id", customer.id)
                }
            }
    }

    suspend fun deleteById(id: String) {
        client.postgrest.from("customers")
            .delete {
                filter {
                    eq("id", id)
                }
            }
    }
}