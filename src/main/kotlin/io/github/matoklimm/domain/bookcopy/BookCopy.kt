package io.github.matoklimm.domain.bookcopy

import io.github.matoklimm.domain.bookcopy.commands.AddBookCopyCommand
import io.github.matoklimm.domain.bookcopy.commands.BookCopyCommand
import io.github.matoklimm.domain.bookcopy.events.BookCopyAdded
import io.github.matoklimm.domain.bookcopy.events.BookCopyEvent
import kotlin.uuid.Uuid

class BookCopy {

    private lateinit var idState: BookCopyId
    private lateinit var isbnState: String

    val id: BookCopyId
        get() = idState

    val isbn: String
        get() = isbnState

    fun handle(command: BookCopyCommand): List<BookCopyEvent> {
        return when (command) {
            is AddBookCopyCommand -> listOf(BookCopyAdded(id = BookCopyId(Uuid.random()), isbn = command.isbn))
        }
    }

    fun apply(event: BookCopyEvent) {
        when (event) {
            is BookCopyAdded -> {
                check(!::idState.isInitialized) {
                    "BookCopy has already been added, calling ${event.id} on initialized BookCopy(${idState.id})"
                }

                idState = event.id
                isbnState = event.isbn
            }
        }
    }
}