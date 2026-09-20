package io.github.matoklimm.domain.bookcopy

import io.github.matoklimm.domain.bookcopy.commands.RepairBookCopyCommand
import io.github.matoklimm.domain.bookcopy.events.BookCopyAddedEvent
import io.github.matoklimm.domain.bookcopy.events.BookCopyDamagedEvent
import io.github.matoklimm.domain.bookcopy.events.BookCopyRepairedEvent
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlin.uuid.Uuid

class BookCopyRepairTest : StringSpec({

    "a damaged book copy can be repaired" {
        val bookCopyId = BookCopyId(Uuid.random())
        val aggregate = BookCopy()

        aggregate.apply(
            BookCopyAddedEvent(
                bookCopyId = bookCopyId,
                isbn = "978-1-4088-5565-2"
            )
        )
        aggregate.apply(
            BookCopyDamagedEvent(
                bookCopyId = bookCopyId,
                description = "Cover torn"
            )
        )

        val command = RepairBookCopyCommand(
            bookCopyId = bookCopyId,
            description = "Cover replaced",
            isDamageRepaired = true
        )

        val events = aggregate.handle(command)

        events shouldHaveSize 1

        val event = events.single().shouldBeTypeOf<BookCopyRepairedEvent>()
        event.bookCopyId shouldBe bookCopyId
        event.description shouldBe command.description
        event.isDamageRepaired shouldBe true
    }

    "a repair command can record that the damage was not repaired" {
        val bookCopyId = BookCopyId(Uuid.random())
        val aggregate = BookCopy()

        aggregate.apply(
            BookCopyAddedEvent(
                bookCopyId = bookCopyId,
                isbn = "978-1-4088-5565-2"
            )
        )
        aggregate.apply(
            BookCopyDamagedEvent(
                bookCopyId = bookCopyId,
                description = "Cover torn"
            )
        )

        val command = RepairBookCopyCommand(
            bookCopyId = bookCopyId,
            description = "Wont do",
            isDamageRepaired = false
        )

        val events = aggregate.handle(command)

        events shouldHaveSize 1

        val event = events.single().shouldBeTypeOf<BookCopyRepairedEvent>()
        event.bookCopyId shouldBe bookCopyId
        event.description shouldBe command.description
        event.isDamageRepaired shouldBe false
    }

    "an available book copy cannot be repaired" {
        val bookCopyId = BookCopyId(Uuid.random())
        val aggregate = BookCopy()

        aggregate.apply(
            BookCopyAddedEvent(
                bookCopyId = bookCopyId,
                isbn = "978-1-4088-5565-2"
            )
        )

        shouldThrow<IllegalStateException> {
            aggregate.handle(
                RepairBookCopyCommand(
                    bookCopyId = bookCopyId,
                    description = "Attempted repair",
                    isDamageRepaired = true
                )
            )
        }
    }

    "a successfully repaired book copy becomes available" {
        val bookCopyId = BookCopyId(Uuid.random())
        val aggregate = BookCopy()

        aggregate.apply(
            BookCopyAddedEvent(
                bookCopyId = bookCopyId,
                isbn = "978-1-4088-5565-2"
            )
        )
        aggregate.apply(
            BookCopyDamagedEvent(
                bookCopyId = bookCopyId,
                description = "Cover torn"
            )
        )

        aggregate.apply(
            BookCopyRepairedEvent(
                bookCopyId = bookCopyId,
                description = "Cover replaced",
                isDamageRepaired = true
            )
        )

        aggregate.bookCopyStatus shouldBe BookCopyStatus.AVAILABLE
    }

    "a book copy remains damaged if the damage was not repaired" {
        val bookCopyId = BookCopyId(Uuid.random())
        val aggregate = BookCopy()

        aggregate.apply(
            BookCopyAddedEvent(
                bookCopyId = bookCopyId,
                isbn = "978-1-4088-5565-2"
            )
        )
        aggregate.apply(
            BookCopyDamagedEvent(
                bookCopyId = bookCopyId,
                description = "Cover torn"
            )
        )

        aggregate.apply(
            BookCopyRepairedEvent(
                bookCopyId = bookCopyId,
                description = "Wont do",
                isDamageRepaired = false
            )
        )

        aggregate.bookCopyStatus shouldBe BookCopyStatus.DAMAGED
    }

    "a repaired event cannot be applied to an available book copy" {
        val bookCopyId = BookCopyId(Uuid.random())
        val aggregate = BookCopy()

        aggregate.apply(
            BookCopyAddedEvent(
                bookCopyId = bookCopyId,
                isbn = "978-1-4088-5565-2"
            )
        )

        shouldThrow<IllegalStateException> {
            aggregate.apply(
                BookCopyRepairedEvent(
                    bookCopyId = bookCopyId,
                    description = "Cover replaced",
                    isDamageRepaired = true
                )
            )
        }

        aggregate.bookCopyStatus shouldBe BookCopyStatus.AVAILABLE
    }
})