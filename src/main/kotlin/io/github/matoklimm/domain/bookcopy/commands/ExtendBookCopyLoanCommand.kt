package io.github.matoklimm.domain.bookcopy.commands

import io.github.matoklimm.domain.bookcopy.BookCopyId

data class ExtendBookCopyLoanCommand(override val bookCopyId: BookCopyId, val userId: String) : BookCopyCommand
