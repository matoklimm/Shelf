package io.github.matoklimm.domain.bookcopy


import io.github.matoklimm.domain.bookcopy.commands.BorrowBookCopyCommand
import io.github.matoklimm.domain.bookcopy.commands.ReturnBookCopyCommand
import io.github.matoklimm.domain.bookcopy.events.BookCopyAddedEvent
import io.github.matoklimm.domain.bookcopy.events.BookCopyReturnedEvent
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlin.uuid.Uuid


class BookCopyReturnTest : StringSpec({

    "a borrowed book copy can be returned" {
        val bookCopyId = BookCopyId(Uuid.random())
        val bookCopy = BookCopy()

        bookCopy.apply(
            BookCopyAddedEvent(
                bookCopyId = bookCopyId, isbn = "978-1-4088-5565-2"
            )
        )

        bookCopy.handle(
            BorrowBookCopyCommand(
                bookCopyId = bookCopyId, userId = "user-123"
            )
        ).forEach(bookCopy::apply)

        val events = bookCopy.handle(
            ReturnBookCopyCommand(
                bookCopyId = bookCopyId
            )
        )

        events shouldHaveSize 1

        val event = events.single() as BookCopyReturnedEvent

        event.bookCopyId shouldBe bookCopyId
    }

    "a book copy must be borrowed in order to be returned" {
        val bookCopyId = BookCopyId(Uuid.random())
        val bookCopy = BookCopy()

        bookCopy.apply(
            BookCopyAddedEvent(
                bookCopyId = bookCopyId, isbn = "978-1-4088-5565-2"
            )
        )

        shouldThrow<IllegalStateException> {
            bookCopy.handle(
                ReturnBookCopyCommand(
                    bookCopyId = bookCopyId
                )
            )
        }
    }

    "returning a book copy makes it available and removes the active loan" {
        val bookCopyId = BookCopyId(Uuid.random())
        val bookCopy = BookCopy()

        bookCopy.apply(
            BookCopyAddedEvent(
                bookCopyId = bookCopyId, isbn = "978-1-4088-5565-2"
            )
        )

        bookCopy.handle(
            BorrowBookCopyCommand(
                bookCopyId = bookCopyId, userId = "user-123"
            )
        ).forEach(bookCopy::apply)

        val events = bookCopy.handle(
            ReturnBookCopyCommand(
                bookCopyId = bookCopyId
            )
        )

        events.forEach(bookCopy::apply)
        bookCopy.bookCopyStatus shouldBe BookCopyStatus.AVAILABLE
        bookCopy.bookCopyLoan shouldBe null
    }
})
