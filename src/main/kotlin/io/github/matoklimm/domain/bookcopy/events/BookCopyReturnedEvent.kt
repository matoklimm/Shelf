package io.github.matoklimm.domain.bookcopy.events

import io.github.matoklimm.domain.bookcopy.BookCopyId
import java.time.LocalDate

data class BookCopyReturnedEvent(val bookCopyId: BookCopyId, val returnedAt: LocalDate) : BookCopyEvent