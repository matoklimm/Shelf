package io.github.matoklimm.domain.bookcopy.commands

import io.github.matoklimm.domain.bookcopy.BookCopyId

data class ReportBookCopyDamageCommand(override val bookCopyId: BookCopyId, val description: String) : BookCopyCommand