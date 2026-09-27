package io.github.matoklimm.domain.bookcopy


import io.github.matoklimm.domain.bookcopy.commands.BorrowBookCopyCommand
import io.github.matoklimm.domain.bookcopy.commands.ReturnBookCopyCommand
import io.github.matoklimm.domain.bookcopy.events.BookCopyAddedEvent
import io.github.matoklimm.domain.bookcopy.events.BookCopyReturnedEvent
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import java.time.Instant
import kotlin.uuid.Uuid


class BookCopyReturnTest : StringSpec({
    "a borrowed book copy can be returned" {
        val bookCopyId = BookCopyId(Uuid.random())
        val bookCopy = BookCopy()

        bookCopy.replay(BookCopyAddedEvent(bookCopyId = bookCopyId, isbn = "978-1-4088-5565-2"))

        bookCopy.handle(BorrowBookCopyCommand(bookCopyId = bookCopyId, userId = "user-123"))

        bookCopy.handle(ReturnBookCopyCommand(bookCopyId = bookCopyId))

        bookCopy.pendingEvents shouldHaveSize 2   // borrow + return both still queued
        val event = bookCopy.pendingEvents.last() as BookCopyReturnedEvent

        event.bookCopyId shouldBe bookCopyId
    }

    "a book copy must be borrowed in order to be succeed the return command" {
        val bookCopyId = BookCopyId(Uuid.random())
        val bookCopy = BookCopy()

        bookCopy.replay(BookCopyAddedEvent(bookCopyId = bookCopyId, isbn = "978-1-4088-5565-2"))

        shouldThrow<IllegalStateException> {
            bookCopy.handle(ReturnBookCopyCommand(bookCopyId = bookCopyId))
        }
    }

    "returning a book copy makes it available and removes the active loan" {
        val bookCopyId = BookCopyId(Uuid.random())
        val bookCopy = BookCopy()

        bookCopy.replay(BookCopyAddedEvent(bookCopyId = bookCopyId, isbn = "978-1-4088-5565-2"))

        bookCopy.handle(BorrowBookCopyCommand(bookCopyId = bookCopyId, userId = "user-123"))
        bookCopy.handle(ReturnBookCopyCommand(bookCopyId = bookCopyId))

        bookCopy.bookCopyStatus shouldBe BookCopyStatus.AVAILABLE
        bookCopy.bookCopyLoan shouldBe null
    }
})
