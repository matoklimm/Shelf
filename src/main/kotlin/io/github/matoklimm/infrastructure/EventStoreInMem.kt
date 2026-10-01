package io.github.matoklimm.infrastructure

import io.github.matoklimm.application.adapter.EventStore
import io.github.matoklimm.domain.shared.Event
import java.util.concurrent.ConcurrentHashMap
import kotlin.uuid.Uuid

class EventStoreInMem(private val streams: ConcurrentHashMap<Uuid, MutableList<Event>>) : EventStore {

    override fun loadEvents(aggregateId: Uuid, startVersion: Int): List<Event> {
        val stream = streams[aggregateId] ?: return emptyList()

        synchronized(stream) {
            return stream.drop(startVersion - 1)
        }
    }

    override fun saveEvents(aggregateId: Uuid, events: List<Event>) {
        if (events.isEmpty()) return

        val stream = streams.computeIfAbsent(aggregateId) {
            mutableListOf()
        }

        synchronized(stream) {
            stream.addAll(events)
        }
    }
}