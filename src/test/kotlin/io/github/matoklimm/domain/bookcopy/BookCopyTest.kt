package io.github.matoklimm.domain.bookcopy

import io.github.matoklimm.domain.bookcopy.commands.AddBookCopyCommand
import io.github.matoklimm.domain.bookcopy.events.BookCopyAdded
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeTypeOf


class BookCopyTest : StringSpec({

    "a book copy can be added" {
        // Given
        val command = AddBookCopyCommand(isbn = "978-1-4088-5565-2")

        // When
        val events = BookCopy().handle(command)

        // Then
        events shouldBe listOf(events.single())

        val event = events.single().shouldBeTypeOf<BookCopyAdded>()
        event.isbn shouldBe command.isbn
    }

    "book copies for the same isbn can be added" {
        // Given
        val addCopy1 = AddBookCopyCommand(isbn = "978-1-4088-5565-2")
        val addCopy2 = AddBookCopyCommand(isbn = "978-1-4088-5565-2")

        // When
        val copy1Added = BookCopy()
            .handle(addCopy1)
            .single()
            .shouldBeTypeOf<BookCopyAdded>()

        val copy2Added = BookCopy()
            .handle(addCopy2)
            .single()
            .shouldBeTypeOf<BookCopyAdded>()

        // Then
        copy1Added.isbn shouldBe addCopy1.isbn
        copy2Added.isbn shouldBe addCopy2.isbn
        copy1Added.isbn shouldBe copy2Added.isbn
        copy1Added.id shouldNotBe copy2Added.id
    }
})