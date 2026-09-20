package io.github.matoklimm.domain.bookcopy.commands

import io.github.matoklimm.domain.bookcopy.BookCopyId

data class RetireBookCopyCommand(val bookCopyId: BookCopyId): BookCopyCommand
