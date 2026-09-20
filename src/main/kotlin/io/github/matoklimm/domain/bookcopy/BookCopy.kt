package io.github.matoklimm.domain.bookcopy

import io.github.matoklimm.domain.bookcopy.commands.*
import io.github.matoklimm.domain.bookcopy.events.BookCopyAddedEvent
import io.github.matoklimm.domain.bookcopy.events.BookCopyBorrowedEvent
import io.github.matoklimm.domain.bookcopy.events.BookCopyDamagedEvent
import io.github.matoklimm.domain.bookcopy.events.BookCopyEvent
import io.github.matoklimm.domain.bookcopy.events.BookCopyReturnedEvent
import java.time.Instant
import java.time.LocalDate
import kotlin.uuid.Uuid

data class BookCopyId(val id: Uuid)

class BookCopy {

    lateinit var id: BookCopyId
        private set

    lateinit var isbn: String
        private set

    lateinit var bookCopyStatus: BookCopyStatus
        private set

    var bookCopyLoan: BookCopyLoan? = null
        private set

    var damageDescription: String? = null
        private set

    fun handle(command: BookCopyCommand): List<BookCopyEvent> {
        return when (command) {
            is AddBookCopyCommand -> listOf(BookCopyAddedEvent(bookCopyId = BookCopyId(Uuid.random()), isbn = command.isbn))

            is BorrowBookCopyCommand -> {
                checkStatus(BookCopyStatus.AVAILABLE)

                val borrowedUntil = command.borrowedUntil ?: LocalDate.now().plusDays(14)
                check(borrowedUntil < LocalDate.now().plusDays(31)) {
                    "BookCopy(${id.id}) must be borrowed for 30 days or less. $borrowedUntil is exceeding that range"
                }

                listOf(
                    BookCopyBorrowedEvent(
                        bookCopyId = command.bookCopyId, userId = command.userId, borrowedAt = Instant.now(), borrowedUntil = borrowedUntil
                    )
                )
            }

            is ReturnBookCopyCommand -> {
                checkStatus( BookCopyStatus.BORROWED)

                listOf(BookCopyReturnedEvent(bookCopyId = command.bookCopyId, returnedAt = Instant.now()))
            }

            is ReportDamageBookCopyCommand -> {
                checkStatus(BookCopyStatus.AVAILABLE, BookCopyStatus.BORROWED)

                listOf(BookCopyDamagedEvent(bookCopyId = command.bookCopyId, description = command.description))
            }
        }
    }

    fun apply(event: BookCopyEvent) {
        when (event) {
            is BookCopyAddedEvent -> {
                check(!::id.isInitialized) {
                    "BookCopy(${id.id}) has already been added, calling ${event.bookCopyId} on initialized BookCopy(${id.id})"
                }

                id = event.bookCopyId
                isbn = event.isbn
                bookCopyStatus = BookCopyStatus.AVAILABLE
            }

            is BookCopyBorrowedEvent -> {
                checkStatus(BookCopyStatus.AVAILABLE)

                bookCopyStatus = BookCopyStatus.BORROWED
                bookCopyLoan = BookCopyLoan(
                    borrowedBy = event.userId, borrowedAt = event.borrowedAt, borrowedUntil = event.borrowedUntil
                )
            }

            is BookCopyReturnedEvent -> {
                checkStatus(BookCopyStatus.BORROWED)

                bookCopyStatus = BookCopyStatus.AVAILABLE
                bookCopyLoan = null
            }

            is BookCopyDamagedEvent -> {
                checkStatus(BookCopyStatus.AVAILABLE, BookCopyStatus.BORROWED)

                damageDescription = event.description
                bookCopyStatus = BookCopyStatus.DAMAGED
            }
        }
    }

    private fun checkStatus(vararg allowed: BookCopyStatus) {
        check(bookCopyStatus in allowed) {
            "BookCopy(${id.id}) must be in one of states ${allowed.toList()} but is in '$bookCopyStatus'"
        }
    }

}