package com.rige.utils

fun calculateRange(page: Int, pageSize: Int): Pair<Long, Long> {
    val offset = page * pageSize
    val to = offset + pageSize - 1
    return offset.toLong() to to.toLong()
}