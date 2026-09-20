package io.github.matoklimm.domain.bookcopy.events

import io.github.matoklimm.domain.bookcopy.BookCopyId

data class BookCopyAdded(val bookCopyId: BookCopyId, val isbn: String): BookCopyEvent