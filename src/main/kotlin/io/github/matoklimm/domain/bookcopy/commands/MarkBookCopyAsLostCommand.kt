package io.github.matoklimm.domain.bookcopy.commands

import io.github.matoklimm.domain.bookcopy.BookCopyId

data class MarkBookCopyAsLostCommand(override val bookCopyId: BookCopyId) : BookCopyCommand