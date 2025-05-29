package com.rige.di

import com.rige.BuildConfig
import com.rige.repositories.CategoryRepository
import com.rige.repositories.CustomerRepository
import com.rige.repositories.ProductRepository
import com.rige.repositories.SaleRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): io.github.jan.supabase.SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_KEY
        ) {
            install(Postgrest)
        }
    }

    @Provides
    @Singleton
    fun provideProductRepository(client: io.github.jan.supabase.SupabaseClient): ProductRepository {
        return ProductRepository(client)
    }

    @Provides
    @Singleton
    fun provideCategoryRepository(client: io.github.jan.supabase.SupabaseClient): CategoryRepository {
        return CategoryRepository(client)
    }

    @Provides
    @Singleton
    fun provideSaleRepository(client: io.github.jan.supabase.SupabaseClient): SaleRepository {
        return SaleRepository(client)
    }

    @Provides
    @Singleton
    fun provideCustomerRepository(client: io.github.jan.supabase.SupabaseClient): CustomerRepository {
        return CustomerRepository(client)
    }
}