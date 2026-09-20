package io.github.matoklimm.domain.bookcopy.events

import io.github.matoklimm.domain.bookcopy.BookCopyId
import java.time.Instant

data class BookCopyRetiredEvent(val bookCopyId: BookCopyId, val retiredAt: Instant): BookCopyEvent
