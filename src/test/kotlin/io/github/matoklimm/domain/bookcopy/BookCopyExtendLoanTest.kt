package io.github.matoklimm.domain.bookcopy

import io.github.matoklimm.domain.bookcopy.commands.ExtendBookCopyLoanCommand
import io.github.matoklimm.domain.bookcopy.events.BookCopyAddedEvent
import io.github.matoklimm.domain.bookcopy.events.BookCopyBorrowedEvent
import io.github.matoklimm.domain.bookcopy.events.BookCopyLoanExtendedEvent
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import java.time.Instant
import java.time.LocalDate
import kotlin.uuid.Uuid

class BookCopyExtendLoanTest : StringSpec({
    "a book copy loan can be extended by the current borrower" {
        val bookCopyId = BookCopyId(Uuid.random())
        val userId = "user-123"
        val bookCopy = BookCopy()

        bookCopy.replay(BookCopyAddedEvent(bookCopyId = bookCopyId, isbn = "978-1-4088-5565-2"))
        bookCopy.replay(
            BookCopyBorrowedEvent(
                bookCopyId = bookCopyId, userId = userId, borrowedAt = Instant.now(), borrowedUntil = LocalDate.now().plusDays(14)
            )
        )

        bookCopy.handle(ExtendBookCopyLoanCommand(bookCopyId = bookCopyId, userId = userId))

        bookCopy.pendingEvents shouldHaveSize 1
        val event = bookCopy.pendingEvents.single().shouldBeTypeOf<BookCopyLoanExtendedEvent>()

        event.bookCopyId shouldBe bookCopyId
        event.extendedUntil shouldBe LocalDate.now().plusDays(28)
    }

    "a loan cannot be extended by another user" {
        val bookCopyId = BookCopyId(Uuid.random())
        val borrowerId = "user-123"
        val bookCopy = BookCopy()

        bookCopy.replay(BookCopyAddedEvent(bookCopyId = bookCopyId, isbn = "978-1-4088-5565-2"))
        bookCopy.replay(
            BookCopyBorrowedEvent(
                bookCopyId = bookCopyId, userId = borrowerId, borrowedAt = Instant.now(), borrowedUntil = LocalDate.now().plusDays(14)
            )
        )

        shouldThrow<IllegalStateException> {
            bookCopy.handle(ExtendBookCopyLoanCommand(bookCopyId = bookCopyId, userId = "user-456"))
        }
    }

    "an available book copy cannot have its loan extended" {
        val bookCopyId = BookCopyId(Uuid.random())
        val bookCopy = BookCopy()

        bookCopy.replay(BookCopyAddedEvent(bookCopyId = bookCopyId, isbn = "978-1-4088-5565-2"))

        shouldThrow<IllegalStateException> {
            bookCopy.handle(ExtendBookCopyLoanCommand(bookCopyId = bookCopyId, userId = "user-123"))
        }
    }

    "a loan cannot be extended more than twice" {
        val bookCopyId = BookCopyId(Uuid.random())
        val userId = "user-123"
        val bookCopy = BookCopy()

        bookCopy.replay(BookCopyAddedEvent(bookCopyId = bookCopyId, isbn = "978-1-4088-5565-2"))
        bookCopy.replay(
            BookCopyBorrowedEvent(
                bookCopyId = bookCopyId, userId = userId, borrowedAt = Instant.now(), borrowedUntil = LocalDate.now().plusDays(14)
            )
        )
        bookCopy.replay(BookCopyLoanExtendedEvent(bookCopyId = bookCopyId, extendedUntil = LocalDate.now().plusDays(28)))
        bookCopy.replay(BookCopyLoanExtendedEvent(bookCopyId = bookCopyId, extendedUntil = LocalDate.now().plusDays(42)))

        shouldThrow<IllegalStateException> {
            bookCopy.handle(ExtendBookCopyLoanCommand(bookCopyId = bookCopyId, userId = userId))
        }
    }

    "a loan extended event increments the extend count" {
        val bookCopyId = BookCopyId(Uuid.random())
        val userId = "user-123"
        val bookCopy = BookCopy()

        bookCopy.replay(BookCopyAddedEvent(bookCopyId = bookCopyId, isbn = "978-1-4088-5565-2"))
        bookCopy.replay(
            BookCopyBorrowedEvent(
                bookCopyId = bookCopyId, userId = userId, borrowedAt = Instant.now(), borrowedUntil = LocalDate.now().plusDays(14)
            )
        )
        bookCopy.replay(BookCopyLoanExtendedEvent(bookCopyId = bookCopyId, extendedUntil = LocalDate.now().plusDays(28)))

        bookCopy.bookCopyLoan!!.extendCount shouldBe 1
    }

    "a loan extended event updates the borrowed until date" {
        val bookCopyId = BookCopyId(Uuid.random())
        val userId = "user-123"
        val bookCopy = BookCopy()

        bookCopy.replay(BookCopyAddedEvent(bookCopyId = bookCopyId, isbn = "978-1-4088-5565-2"))
        bookCopy.replay(
            BookCopyBorrowedEvent(
                bookCopyId = bookCopyId, userId = userId, borrowedAt = Instant.now(), borrowedUntil = LocalDate.now().plusDays(14)
            )
        )

        val extendedUntil = LocalDate.now().plusDays(28)
        bookCopy.replay(BookCopyLoanExtendedEvent(bookCopyId = bookCopyId, extendedUntil = extendedUntil))

        bookCopy.bookCopyLoan!!.borrowedUntil shouldBe extendedUntil
    }

    "a loan can be extended twice" {
        val bookCopyId = BookCopyId(Uuid.random())
        val userId = "user-123"
        val bookCopy = BookCopy()

        bookCopy.replay(BookCopyAddedEvent(bookCopyId = bookCopyId, isbn = "978-1-4088-5565-2"))
        bookCopy.replay(
            BookCopyBorrowedEvent(
                bookCopyId = bookCopyId, userId = userId, borrowedAt = Instant.now(), borrowedUntil = LocalDate.now().plusDays(14)
            )
        )
        bookCopy.replay(BookCopyLoanExtendedEvent(bookCopyId = bookCopyId, extendedUntil = LocalDate.now().plusDays(28)))
        bookCopy.replay(BookCopyLoanExtendedEvent(bookCopyId = bookCopyId, extendedUntil = LocalDate.now().plusDays(42)))

        bookCopy.bookCopyLoan!!.extendCount shouldBe 2
        bookCopy.bookCopyLoan!!.borrowedUntil shouldBe LocalDate.now().plusDays(42)
    }
})