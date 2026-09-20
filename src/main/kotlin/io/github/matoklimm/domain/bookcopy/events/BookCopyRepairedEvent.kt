package io.github.matoklimm.domain.bookcopy.events

import io.github.matoklimm.domain.bookcopy.BookCopyId

data class BookCopyRepairedEvent(val bookCopyId: BookCopyId, val description: String, val isDamageRepaired: Boolean): BookCopyEvent
