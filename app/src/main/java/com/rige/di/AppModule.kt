package com.rige.di

import com.rige.clients.SupabaseClient
import com.rige.repositories.BarcodeRepository
import com.rige.repositories.CategoryRepository
import com.rige.repositories.CustomerRepository
import com.rige.repositories.OrderRepository
import com.rige.repositories.ProductRepository
import com.rige.repositories.ProfileRepository
import com.rige.repositories.SaleDetailRepository
import com.rige.repositories.SaleRepository
import com.rige.repositories.SupplierRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): io.github.jan.supabase.SupabaseClient {
        return SupabaseClient.client
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

    @Provides
    @Singleton
    fun provideSaleDetailRepository(client: io.github.jan.supabase.SupabaseClient): SaleDetailRepository {
        return SaleDetailRepository(client)
    }

    @Provides
    @Singleton
    fun provideBarcodeRepository(client: io.github.jan.supabase.SupabaseClient): BarcodeRepository {
        return BarcodeRepository(client)
    }

    @Provides
    @Singleton
    fun provideSupplierRepository(client: io.github.jan.supabase.SupabaseClient): SupplierRepository {
        return SupplierRepository(client)
    }

    @Provides
    @Singleton
    fun provideOrderRepository(client: io.github.jan.supabase.SupabaseClient): OrderRepository {
        return OrderRepository(client)
    }

    @Provides
    @Singleton
    fun provideProfileRepository(client: io.github.jan.supabase.SupabaseClient): ProfileRepository {
        return ProfileRepository(client)
    }
}