package io.github.matoklimm.domain.bookcopy

import io.github.matoklimm.domain.bookcopy.commands.AddBookCopyCommand
import io.github.matoklimm.domain.bookcopy.events.BookCopyAddedEvent
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeTypeOf
import kotlin.uuid.Uuid


class BookCopyTest : StringSpec({

    "a book copy can be added" {
        val command = AddBookCopyCommand(isbn = "978-1-4088-5565-2")

        val events = BookCopy().handle(command)

        events shouldHaveSize 1

        val event = events.single().shouldBeTypeOf<BookCopyAddedEvent>()
        event.isbn shouldBe command.isbn
    }

    "book copies for the same isbn can be added" {
        val addCopy1 = AddBookCopyCommand(isbn = "978-1-4088-5565-2")
        val addCopy2 = AddBookCopyCommand(isbn = "978-1-4088-5565-2")

        val copy1Added = BookCopy()
            .handle(addCopy1)
            .single()
            .shouldBeTypeOf<BookCopyAddedEvent>()

        val copy2Added = BookCopy()
            .handle(addCopy2)
            .single()
            .shouldBeTypeOf<BookCopyAddedEvent>()

        copy1Added.isbn shouldBe addCopy1.isbn
        copy2Added.isbn shouldBe addCopy2.isbn
        copy1Added.isbn shouldBe copy2Added.isbn
        copy1Added.bookCopyId shouldNotBe copy2Added.bookCopyId
    }

    "BookCopyAdded event is processed" {
        val bookCopyId = BookCopyId(Uuid.random())
        val bookCopyAddedEvent = BookCopyAddedEvent(bookCopyId = bookCopyId, isbn = "978-1-4088-5565-2")
        val aggregate = BookCopy()

        aggregate.apply(bookCopyAddedEvent)

        aggregate.isbn shouldBe bookCopyAddedEvent.isbn
        aggregate.id shouldBe bookCopyId
        aggregate.bookCopyStatus shouldBe BookCopyStatus.AVAILABLE
    }

    "BookCopyAdded cannot be processed twice" {
        val firstEvent = BookCopyAddedEvent(
            bookCopyId = BookCopyId(Uuid.random()),
            isbn = "978-1-4088-5565-2"
        )

        val secondEvent = BookCopyAddedEvent(
            bookCopyId = BookCopyId(Uuid.random()),
            isbn = "978-1-4088-5566-9"
        )

        val aggregate = BookCopy()
        aggregate.apply(firstEvent)

        shouldThrow<IllegalStateException> {
            aggregate.apply(secondEvent)
        }

        aggregate.id shouldBe firstEvent.bookCopyId
        aggregate.isbn shouldBe firstEvent.isbn
        aggregate.bookCopyStatus shouldBe BookCopyStatus.AVAILABLE
    }
})