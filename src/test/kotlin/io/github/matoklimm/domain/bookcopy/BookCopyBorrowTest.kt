package io.github.matoklimm.domain.bookcopy

import io.github.matoklimm.domain.bookcopy.commands.BorrowBookCopyCommand
import io.github.matoklimm.domain.bookcopy.events.BookCopyAddedEvent
import io.github.matoklimm.domain.bookcopy.events.BookCopyBorrowedEvent
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import kotlin.uuid.Uuid

class BookCopyBorrowTest : StringSpec({

    "an available book copy can be borrowed" {
        val bookCopyId = BookCopyId(Uuid.random())
        val bookCopy = BookCopy()

        bookCopy.apply(
            BookCopyAddedEvent(
                bookCopyId = bookCopyId, isbn = "978-1-4088-5565-2"
            )
        )

        val command = BorrowBookCopyCommand(
            bookCopyId = bookCopyId, userId = "user-123", borrowedUntil = LocalDate.now().plusDays(10)
        )

        val events = bookCopy.handle(command)

        events shouldHaveSize 1

        val event = events.single() as BookCopyBorrowedEvent

        event.bookCopyId shouldBe bookCopyId
        event.userId shouldBe "user-123"
        event.borrowedUntil shouldBe command.borrowedUntil
    }

    "a borrowed book copy cannot be borrowed again" {
        val bookCopyId = BookCopyId(Uuid.random())
        val bookCopy = BookCopy()

        bookCopy.apply(
            BookCopyAddedEvent(
                bookCopyId = bookCopyId, isbn = "978-1-4088-5565-2"
            )
        )

        val firstBorrow = BorrowBookCopyCommand(
            bookCopyId = bookCopyId, userId = "user-123"
        )

        bookCopy.handle(firstBorrow).forEach(bookCopy::apply)

        shouldThrow<IllegalStateException> {
            bookCopy.handle(
                BorrowBookCopyCommand(
                    bookCopyId = bookCopyId, userId = "user-456"
                )
            )
        }
    }

    "borrow defaults to 14 days when no return date is provided" {
        val bookCopyId = BookCopyId(Uuid.random())
        val bookCopy = BookCopy()

        bookCopy.apply(
            BookCopyAddedEvent(
                bookCopyId = bookCopyId, isbn = "978-1-4088-5565-2"
            )
        )

        val command = BorrowBookCopyCommand(
            bookCopyId = bookCopyId, userId = "user-123"
        )

        val events = bookCopy.handle(command)

        val event = events.single() as BookCopyBorrowedEvent

        event.borrowedUntil shouldBe LocalDate.now().plusDays(14)
    }

    "borrowing for exactly 30 days is allowed" {
        val bookCopyId = BookCopyId(Uuid.random())
        val bookCopy = BookCopy()

        bookCopy.apply(
            BookCopyAddedEvent(
                bookCopyId = bookCopyId, isbn = "978-1-4088-5565-2"
            )
        )

        val borrowedUntil = LocalDate.now().plusDays(30)

        val events = bookCopy.handle(
            BorrowBookCopyCommand(
                bookCopyId = bookCopyId, userId = "user-123", borrowedUntil = borrowedUntil
            )
        )

        events shouldHaveSize 1
        val event = events.single() as BookCopyBorrowedEvent

        event.borrowedUntil shouldBe borrowedUntil
    }

    "borrowing for more than 30 days is rejected" {
        val bookCopyId = BookCopyId(Uuid.random())
        val bookCopy = BookCopy()

        bookCopy.apply(
            BookCopyAddedEvent(
                bookCopyId = bookCopyId, isbn = "978-1-4088-5565-2"
            )
        )

        val borrowedUntil = LocalDate.now().plusDays(31)

        shouldThrow<IllegalStateException> {
            bookCopy.handle(
                BorrowBookCopyCommand(
                    bookCopyId = bookCopyId, userId = "user-123", borrowedUntil = borrowedUntil
                )
            )
        }
    }

    "borrowing a book copy applies the borrowed event" {
        val bookCopyId = BookCopyId(Uuid.random())
        val bookCopy = BookCopy()

        bookCopy.apply(
            BookCopyAddedEvent(
                bookCopyId = bookCopyId, isbn = "978-1-4088-5565-2"
            )
        )

        val borrowedUntil = LocalDate.now().plusDays(10)

        val events = bookCopy.handle(
            BorrowBookCopyCommand(
                bookCopyId = bookCopyId, userId = "user-123", borrowedUntil = borrowedUntil
            )
        )

        events.forEach(bookCopy::apply)

        bookCopy.bookCopyStatus shouldBe BookCopyStatus.BORROWED
        bookCopy.bookCopyLoan!!.borrowedBy shouldBe "user-123"
        bookCopy.bookCopyLoan!!.borrowedUntil shouldBe borrowedUntil
    }
})

