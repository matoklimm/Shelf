package io.github.matoklimm.infrastructure

import io.github.matoklimm.application.adapter.EventStore
import io.github.matoklimm.domain.shared.Event
import kotlin.uuid.Uuid

/**
 * On the database level, we must set a unique constraint on (aggregateId, version) to enforce optimistic concurrency.
 * That prevents two events from being persisted at the same version within the same aggregate stream.
 * */
class EventStoreMongo: EventStore {

    override fun loadEvents(aggregateId: Uuid, startVersion: Int): List<Event> {
        TODO("Not yet implemented")
    }

    override fun saveEvents(aggregateId: Uuid, events: List<Event>) {
        TODO("Not yet implemented")
    }
}