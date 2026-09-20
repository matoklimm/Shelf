package io.github.matoklimm.domain.bookcopy.events

import io.github.matoklimm.domain.bookcopy.BookCopyId
import java.time.Instant

data class BookCopyLostEvent(val bookCopyId: BookCopyId, val lostAt: Instant): BookCopyEvent