package io.github.matoklimm.domain.bookcopy.commands

import io.github.matoklimm.domain.bookcopy.BookCopyId

data class ReportBookCopyDamageCommand(val bookCopyId: BookCopyId, val description: String): BookCopyCommand