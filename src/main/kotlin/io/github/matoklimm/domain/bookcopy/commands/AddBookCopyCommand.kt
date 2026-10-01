package io.github.matoklimm.domain.bookcopy.commands

import io.github.matoklimm.domain.bookcopy.BookCopyId

data class AddBookCopyCommand(val isbn: String, override val bookCopyId: BookCopyId? = null) : BookCopyCommand