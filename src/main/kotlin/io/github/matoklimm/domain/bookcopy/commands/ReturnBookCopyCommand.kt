package io.github.matoklimm.domain.bookcopy.commands

import io.github.matoklimm.domain.bookcopy.BookCopyId

data class ReturnBookCopyCommand(val bookCopyId: BookCopyId): BookCopyCommand