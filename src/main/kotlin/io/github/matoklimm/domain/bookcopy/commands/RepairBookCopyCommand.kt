package io.github.matoklimm.domain.bookcopy.commands

import io.github.matoklimm.domain.bookcopy.BookCopyId

data class RepairBookCopyCommand(
    val bookCopyId: BookCopyId, val description: String, val isDamageRepaired: Boolean = true
) : BookCopyCommand
