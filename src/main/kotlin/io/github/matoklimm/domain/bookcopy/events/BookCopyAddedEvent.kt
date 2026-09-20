package io.github.matoklimm.domain.bookcopy.events

import io.github.matoklimm.domain.bookcopy.BookCopyId

data class BookCopyAddedEvent(val bookCopyId: BookCopyId, val isbn: String): BookCopyEvent