package io.github.matoklimm.domain.bookcopy

import java.time.Instant
import java.time.LocalDate

data class BookCopyLoan(
    val borrowedBy: String,
    val borrowedAt: Instant,
    val borrowedUntil: LocalDate,
    val extendCount: Int,
)