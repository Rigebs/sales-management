package com.rige.extensions

import com.rige.utils.calculateRange
import io.github.jan.supabase.postgrest.query.PostgrestRequestBuilder

fun PostgrestRequestBuilder.paginate(page: Int, pageSize: Int) {
    val (from, to) = calculateRange(page, pageSize)
    range(from, to)
}
