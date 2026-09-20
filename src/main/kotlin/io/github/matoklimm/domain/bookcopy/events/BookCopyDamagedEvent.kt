package io.github.matoklimm.domain.bookcopy.events

import io.github.matoklimm.domain.bookcopy.BookCopyId

data class BookCopyDamagedEvent(val bookCopyId: BookCopyId, val description: String): BookCopyEvent
