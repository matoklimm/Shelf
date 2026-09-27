package io.github.matoklimm.domain.bookcopy

import io.github.matoklimm.domain.bookcopy.commands.BorrowBookCopyCommand
import io.github.matoklimm.domain.bookcopy.commands.ReportBookCopyDamageCommand
import io.github.matoklimm.domain.bookcopy.events.BookCopyAddedEvent
import io.github.matoklimm.domain.bookcopy.events.BookCopyDamagedEvent
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlin.uuid.Uuid

class BookCopyDamageTest : StringSpec({
    "damage can be reported for an available book copy" {
        val bookCopyId = BookCopyId(Uuid.random())
        val bookCopy = BookCopy()

        bookCopy.replay(BookCopyAddedEvent(bookCopyId = bookCopyId, isbn = "978-1-4088-5565-2"))

        val command = ReportBookCopyDamageCommand(bookCopyId = bookCopyId, description = "Cover is scratched")

        bookCopy.handle(command)

        bookCopy.pendingEvents shouldHaveSize 1
        val event = bookCopy.pendingEvents.single() as BookCopyDamagedEvent

        event.bookCopyId shouldBe bookCopyId
        event.description shouldBe "Cover is scratched"
    }

    "damage can be reported for a borrowed book copy" {
        val bookCopyId = BookCopyId(Uuid.random())
        val bookCopy = BookCopy()

        bookCopy.replay(BookCopyAddedEvent(bookCopyId = bookCopyId, isbn = "978-1-4088-5565-2"))

        bookCopy.handle(BorrowBookCopyCommand(bookCopyId = bookCopyId, userId = "user-123"))

        bookCopy.handle(ReportBookCopyDamageCommand(bookCopyId = bookCopyId, description = "Several pages are torn"))

        bookCopy.pendingEvents shouldHaveSize 2   // ← see note below
        val event = bookCopy.pendingEvents.last() as BookCopyDamagedEvent

        event.bookCopyId shouldBe bookCopyId
        event.description shouldBe "Several pages are torn"
    }

    "reporting damage changes the book copy to damaged" {
        val bookCopyId = BookCopyId(Uuid.random())
        val bookCopy = BookCopy()

        bookCopy.replay(BookCopyAddedEvent(bookCopyId = bookCopyId, isbn = "978-1-4088-5565-2"))

        bookCopy.handle(ReportBookCopyDamageCommand(bookCopyId = bookCopyId, description = "Cover is damaged"))

        bookCopy.bookCopyStatus shouldBe BookCopyStatus.DAMAGED
        bookCopy.pendingEvents.filterIsInstance<BookCopyDamagedEvent>().single().description shouldBe "Cover is damaged"
    }

    "damage cannot be reported for an already damaged book copy" {
        val bookCopyId = BookCopyId(Uuid.random())
        val bookCopy = BookCopy()

        bookCopy.replay(BookCopyAddedEvent(bookCopyId = bookCopyId, isbn = "978-1-4088-5565-2"))

        bookCopy.handle(ReportBookCopyDamageCommand(bookCopyId = bookCopyId, description = "First damage"))

        shouldThrow<IllegalStateException> {
            bookCopy.handle(ReportBookCopyDamageCommand(bookCopyId = bookCopyId, description = "Second damage"))
        }
    }
})
