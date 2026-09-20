package io.github.matoklimm.domain.bookcopy.events

import io.github.matoklimm.domain.bookcopy.BookCopyId
import java.time.LocalDate

data class BookCopyLoanExtendedEvent(val bookCopyId: BookCopyId, val extendedUntil: LocalDate): BookCopyEvent
