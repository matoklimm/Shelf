package io.github.matoklimm.domain.bookcopy.commands

import io.github.matoklimm.domain.bookcopy.BookCopyId

data class ReportDamageBookCopyCommand(val bookCopyId: BookCopyId, val description: String): BookCopyCommand