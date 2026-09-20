package io.github.matoklimm.domain.bookcopy.events

import io.github.matoklimm.domain.bookcopy.BookCopyId
import java.time.Instant
import java.time.LocalDate

// FIXME userId is String on purpose to keep things easy while we explore event sourcing
data class BookCopyBorrowedEvent(
    val bookCopyId: BookCopyId,
    val userId: String,
    val borrowedAt: Instant,
    val borrowedUntil: LocalDate
) : BookCopyEvent
