package io.github.matoklimm.domain.bookcopy

import io.github.matoklimm.domain.bookcopy.commands.AddBookCopyCommand
import io.github.matoklimm.domain.bookcopy.commands.BookCopyCommand
import io.github.matoklimm.domain.bookcopy.events.BookCopyAdded
import io.github.matoklimm.domain.bookcopy.events.BookCopyEvent
import kotlin.uuid.Uuid

class BookCopy {

    lateinit var id: BookCopyId
        private set

    lateinit var isbn: String
        private set

    lateinit var bookCopyStatus: BookCopyStatus
        private set

    fun handle(command: BookCopyCommand): List<BookCopyEvent> {
        return when (command) {
            is AddBookCopyCommand -> listOf(BookCopyAdded(id = BookCopyId(Uuid.random()), isbn = command.isbn))
        }
    }

    fun apply(event: BookCopyEvent) {
        when (event) {
            is BookCopyAdded -> {
                check(!::id.isInitialized) {
                    "BookCopy has already been added, calling ${event.id} on initialized BookCopy(${id.id})"
                }

                id = event.id
                isbn = event.isbn
                bookCopyStatus = BookCopyStatus.AVAILABLE
            }
        }
    }
}