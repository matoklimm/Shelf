package io.github.matoklimm.domain.bookcopy.commands

import io.github.matoklimm.domain.bookcopy.BookCopyId

data class MarkBookCopyAsLostCommand(val bookCopyId: BookCopyId): BookCopyCommand