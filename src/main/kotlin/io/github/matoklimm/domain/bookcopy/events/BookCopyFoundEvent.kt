package io.github.matoklimm.domain.bookcopy.events

import io.github.matoklimm.domain.bookcopy.BookCopyId
import java.time.Instant

data class BookCopyFoundEvent(val bookCopyId: BookCopyId, val foundAt: Instant): BookCopyEvent