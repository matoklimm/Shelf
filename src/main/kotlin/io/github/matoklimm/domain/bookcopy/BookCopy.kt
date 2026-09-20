package io.github.matoklimm.domain.bookcopy

import io.github.matoklimm.domain.bookcopy.commands.AddBookCopyCommand
import io.github.matoklimm.domain.bookcopy.commands.BookCopyCommand
import io.github.matoklimm.domain.bookcopy.events.BookCopyAdded
import io.github.matoklimm.domain.bookcopy.events.BookCopyEvent
import kotlin.uuid.Uuid

class BookCopy {

    private lateinit var id: BookCopyId
    private lateinit var isbn: String

    fun handle(command: BookCopyCommand): List<BookCopyEvent> {
        return when (command) {
            is AddBookCopyCommand -> listOf(BookCopyAdded(id = BookCopyId(Uuid.random()), isbn = command.isbn))
        }
    }

    fun apply(event: BookCopyEvent) {
        when (event) {
            is BookCopyAdded -> {
                id = event.id
                isbn = event.isbn
            }
        }
    }
}