package io.github.matoklimm.domain.bookcopy

import io.github.matoklimm.domain.bookcopy.commands.MarkBookCopyAsFoundCommand
import io.github.matoklimm.domain.bookcopy.commands.MarkBookCopyAsLostCommand
import io.github.matoklimm.domain.bookcopy.events.BookCopyAddedEvent
import io.github.matoklimm.domain.bookcopy.events.BookCopyBorrowedEvent
import io.github.matoklimm.domain.bookcopy.events.BookCopyDamagedEvent
import io.github.matoklimm.domain.bookcopy.events.BookCopyFoundEvent
import io.github.matoklimm.domain.bookcopy.events.BookCopyLostEvent
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import java.time.Instant
import java.time.LocalDate
import kotlin.uuid.Uuid

class BookCopyLostAndFoundTest : StringSpec({

    "a book copy can be marked as lost from allowed states" {
        val allowedStatuses = listOf(
            BookCopyStatus.AVAILABLE,
            BookCopyStatus.BORROWED,
            BookCopyStatus.DAMAGED
        )

        allowedStatuses.forEach { status ->
            val bookCopyId = BookCopyId(Uuid.random())
            val aggregate = createBookCopyInStatus(bookCopyId, status)

            val events = aggregate.handle(
                MarkBookCopyAsLostCommand(bookCopyId)
            )

            events shouldHaveSize 1

            val event = events.single().shouldBeTypeOf<BookCopyLostEvent>()
            event.bookCopyId shouldBe bookCopyId
        }
    }

    "a lost book copy cannot be marked as lost again" {
        val bookCopyId = BookCopyId(Uuid.random())
        val aggregate = createBookCopyInStatus(bookCopyId, BookCopyStatus.LOST)

        shouldThrow<IllegalStateException> {
            aggregate.handle(MarkBookCopyAsLostCommand(bookCopyId))
        }
    }

    "a lost book copy can be marked as found" {
        val bookCopyId = BookCopyId(Uuid.random())
        val aggregate = createBookCopyInStatus(bookCopyId, BookCopyStatus.LOST)

        val events = aggregate.handle(
            MarkBookCopyAsFoundCommand(bookCopyId)
        )

        events shouldHaveSize 1

        val event = events.single().shouldBeTypeOf<BookCopyFoundEvent>()
        event.bookCopyId shouldBe bookCopyId
    }

    "a book copy that is not lost cannot be marked as found" {
        val bookCopyId = BookCopyId(Uuid.random())
        val aggregate = createBookCopyInStatus(bookCopyId, BookCopyStatus.AVAILABLE)

        shouldThrow<IllegalStateException> {
            aggregate.handle(MarkBookCopyAsFoundCommand(bookCopyId))
        }
    }

    "a lost event changes the book copy status to lost" {
        val bookCopyId = BookCopyId(Uuid.random())
        val aggregate = createBookCopyInStatus(bookCopyId, BookCopyStatus.AVAILABLE)

        aggregate.apply(
            BookCopyLostEvent(
                bookCopyId = bookCopyId,
                lostAt = Instant.now()
            )
        )

        aggregate.bookCopyStatus shouldBe BookCopyStatus.LOST
    }

    "a found event changes the book copy status to available" {
        val bookCopyId = BookCopyId(Uuid.random())
        val aggregate = createBookCopyInStatus(bookCopyId, BookCopyStatus.LOST)

        aggregate.apply(
            BookCopyFoundEvent(
                bookCopyId = bookCopyId,
                foundAt = Instant.now()
            )
        )

        aggregate.bookCopyStatus shouldBe BookCopyStatus.AVAILABLE
    }

    "a lost event cannot be applied to a book copy that is already lost" {
        val bookCopyId = BookCopyId(Uuid.random())
        val aggregate = createBookCopyInStatus(bookCopyId, BookCopyStatus.LOST)

        shouldThrow<IllegalStateException> {
            aggregate.apply(
                BookCopyLostEvent(
                    bookCopyId = bookCopyId,
                    lostAt = Instant.now()
                )
            )
        }

        aggregate.bookCopyStatus shouldBe BookCopyStatus.LOST
    }

    "a found event cannot be applied to a book copy that is not lost" {
        val bookCopyId = BookCopyId(Uuid.random())
        val aggregate = createBookCopyInStatus(bookCopyId, BookCopyStatus.AVAILABLE)

        shouldThrow<IllegalStateException> {
            aggregate.apply(
                BookCopyFoundEvent(
                    bookCopyId = bookCopyId,
                    foundAt = Instant.now()
                )
            )
        }

        aggregate.bookCopyStatus shouldBe BookCopyStatus.AVAILABLE
    }
})


private fun createBookCopyInStatus(
    bookCopyId: BookCopyId,
    status: BookCopyStatus
): BookCopy {
    val aggregate = BookCopy()

    aggregate.apply(
        BookCopyAddedEvent(
            bookCopyId = bookCopyId,
            isbn = "978-1-4088-5565-2"
        )
    )

    when (status) {
        BookCopyStatus.AVAILABLE -> Unit

        BookCopyStatus.BORROWED -> {
            aggregate.apply(
                BookCopyBorrowedEvent(
                    bookCopyId = bookCopyId,
                    userId = "test-user",
                    borrowedAt = Instant.now(),
                    borrowedUntil = LocalDate.now().plusDays(14)
                )
            )
        }

        BookCopyStatus.DAMAGED -> {
            aggregate.apply(
                BookCopyDamagedEvent(
                    bookCopyId = bookCopyId,
                    description = "Test damage"
                )
            )
        }

        BookCopyStatus.LOST -> {
            aggregate.apply(
                BookCopyLostEvent(
                    bookCopyId = bookCopyId,
                    lostAt = Instant.now()
                )
            )
        }

        BookCopyStatus.RETIRED -> {
            error("RETIRED setup is not implemented in this test helper yet")
        }
    }

    return aggregate
}