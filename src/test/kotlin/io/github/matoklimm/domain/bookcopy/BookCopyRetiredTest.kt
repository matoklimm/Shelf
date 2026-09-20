package io.github.matoklimm.domain.bookcopy

import io.github.matoklimm.domain.bookcopy.commands.RetireBookCopyCommand
import io.github.matoklimm.domain.bookcopy.events.BookCopyAddedEvent
import io.github.matoklimm.domain.bookcopy.events.BookCopyDamagedEvent
import io.github.matoklimm.domain.bookcopy.events.BookCopyRetiredEvent
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import java.time.Instant
import kotlin.uuid.Uuid

class BookCopyRetireTest : StringSpec({

    "an available book copy can be retired" {
        val bookCopyId = BookCopyId(Uuid.random())
        val bookCopy = BookCopy()

        bookCopy.apply(
            BookCopyAddedEvent(
                bookCopyId = bookCopyId,
                isbn = "978-1-4088-5565-2"
            )
        )

        val command = RetireBookCopyCommand(bookCopyId = bookCopyId)

        val events = bookCopy.handle(command)

        events shouldHaveSize 1
        val event = events.single().shouldBeTypeOf<BookCopyRetiredEvent>()
        event.bookCopyId shouldBe bookCopyId
    }

    "a damaged book copy can be retired" {
        val bookCopyId = BookCopyId(Uuid.random())
        val bookCopy = BookCopy()

        bookCopy.apply(
            BookCopyAddedEvent(
                bookCopyId = bookCopyId,
                isbn = "978-1-4088-5565-2"
            )
        )
        bookCopy.apply(
            BookCopyDamagedEvent(
                bookCopyId = bookCopyId,
                description = "Damaged cover"
            )
        )

        val command = RetireBookCopyCommand(bookCopyId = bookCopyId)

        val events = bookCopy.handle(command)

        events shouldHaveSize 1
        val event = events.single().shouldBeTypeOf<BookCopyRetiredEvent>()
        event.bookCopyId shouldBe bookCopyId
    }

    "a retired book copy cannot be retired again" {
        val bookCopyId = BookCopyId(Uuid.random())
        val bookCopy = BookCopy()

        bookCopy.apply(
            BookCopyAddedEvent(
                bookCopyId = bookCopyId,
                isbn = "978-1-4088-5565-2"
            )
        )
        bookCopy.apply(
            BookCopyRetiredEvent(
                bookCopyId = bookCopyId,
                retiredAt = Instant.now()
            )
        )

        shouldThrow<IllegalStateException> {
            bookCopy.handle(
                RetireBookCopyCommand(bookCopyId = bookCopyId)
            )
        }
    }

    "retired event changes book copy status to retired" {
        val bookCopyId = BookCopyId(Uuid.random())
        val bookCopy = BookCopy()

        bookCopy.apply(
            BookCopyAddedEvent(
                bookCopyId = bookCopyId,
                isbn = "978-1-4088-5565-2"
            )
        )

        bookCopy.apply(
            BookCopyRetiredEvent(
                bookCopyId = bookCopyId,
                retiredAt = Instant.now()
            )
        )

        bookCopy.bookCopyStatus shouldBe BookCopyStatus.RETIRED
    }

})