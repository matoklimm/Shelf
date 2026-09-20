package io.github.matoklimm.domain.bookcopy.commands

import io.github.matoklimm.domain.bookcopy.BookCopyId

// FIXME userId is String on purpose to keep things easy while we explore event sourcing
data class BorrowBookCopyCommand(val bookCopyId: BookCopyId, val userId: String) : BookCopyCommand