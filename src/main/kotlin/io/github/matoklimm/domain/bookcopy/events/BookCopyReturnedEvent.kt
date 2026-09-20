package io.github.matoklimm.domain.bookcopy.events

import io.github.matoklimm.domain.bookcopy.BookCopyId
import java.time.Instant

data class BookCopyReturnedEvent(val bookCopyId: BookCopyId, val returnedAt: Instant) : BookCopyEvent