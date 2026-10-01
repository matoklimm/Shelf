package io.github.matoklimm.domain.bookcopy.commands

import io.github.matoklimm.domain.bookcopy.BookCopyId

sealed interface BookCopyCommand {
    val bookCopyId: BookCopyId?
}