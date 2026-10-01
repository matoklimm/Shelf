package io.github.matoklimm.application.adapter

import io.github.matoklimm.domain.shared.Event
import kotlin.uuid.Uuid

interface EventStore {
    fun loadEvents(aggregateId: Uuid, startVersion: Int = 1): List<Event>
    fun saveEvents(aggregateId: Uuid, events: List<Event>)
}