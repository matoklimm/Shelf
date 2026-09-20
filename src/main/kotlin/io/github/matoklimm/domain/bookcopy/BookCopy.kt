package io.github.matoklimm.domain.bookcopy

import io.github.matoklimm.domain.bookcopy.commands.AddBookCopyCommand
import io.github.matoklimm.domain.bookcopy.commands.BookCopyCommand
import io.github.matoklimm.domain.bookcopy.commands.BorrowBookCopyCommand
import io.github.matoklimm.domain.bookcopy.events.BookCopyAdded
import io.github.matoklimm.domain.bookcopy.events.BookCopyBorrowedEvent
import io.github.matoklimm.domain.bookcopy.events.BookCopyEvent
import java.time.Instant
import java.time.LocalDate
import kotlin.uuid.Uuid

class BookCopy {

    lateinit var id: BookCopyId
        private set

    lateinit var isbn: String
        private set

    lateinit var bookCopyStatus: BookCopyStatus
        private set

    var bookCopyLoan: BookCopyLoan? = null
        private set

    fun handle(command: BookCopyCommand): List<BookCopyEvent> {
        return when (command) {
            is AddBookCopyCommand -> listOf(BookCopyAdded(bookCopyId = BookCopyId(Uuid.random()), isbn = command.isbn))
            is BorrowBookCopyCommand -> {
                check(bookCopyStatus == BookCopyStatus.AVAILABLE) {
                    "BookCopy is not in state '${BookCopyStatus.AVAILABLE}' but rather in $bookCopyStatus"
                }

                val borrowedUntil = command.borrowedUntil ?: LocalDate.now().plusDays(14)
                check(borrowedUntil < LocalDate.now().plusDays(31)) {
                    "BookCopy must be borrowed for 30 days or less. $borrowedUntil is exceeding that range"
                }

                listOf(
                    BookCopyBorrowedEvent(
                        bookCopyId = command.bookCopyId,
                        userId = command.userId,
                        borrowedAt = Instant.now(),
                        borrowedUntil = borrowedUntil
                    )
                )
            }
        }
    }

    fun apply(event: BookCopyEvent) {
        when (event) {
            is BookCopyAdded -> {
                check(!::id.isInitialized) {
                    "BookCopy has already been added, calling ${event.bookCopyId} on initialized BookCopy(${id.id})"
                }

                id = event.bookCopyId
                isbn = event.isbn
                bookCopyStatus = BookCopyStatus.AVAILABLE
            }

            is BookCopyBorrowedEvent -> {
                check(bookCopyStatus == BookCopyStatus.AVAILABLE) {
                    "BookCopy must be in state '${BookCopyStatus.AVAILABLE}' to be borrowed, but is in $bookCopyStatus"
                }

                bookCopyStatus = BookCopyStatus.BORROWED
                bookCopyLoan = BookCopyLoan(
                    borrowedBy = event.userId,
                    borrowedAt = event.borrowedAt,
                    borrowedUntil = event.borrowedUntil
                )
            }
        }
    }
}