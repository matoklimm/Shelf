package io.github.matoklimm.application.handler

import io.github.matoklimm.application.adapter.EventStore
import io.github.matoklimm.domain.bookcopy.BookCopy
import io.github.matoklimm.domain.bookcopy.commands.BookCopyCommand
import io.github.matoklimm.domain.bookcopy.events.BookCopyEvent

class BookCopyHandler(private val eventStore: EventStore) {

    fun executeCommand(cmd: BookCopyCommand) {
        val aggregate = BookCopy()

        cmd.bookCopyId?.let { aggregateId ->
            eventStore.loadEvents(aggregateId.id).forEach { aggregate.replay(it as BookCopyEvent) }
        }

        aggregate.handle(cmd)
    }

}