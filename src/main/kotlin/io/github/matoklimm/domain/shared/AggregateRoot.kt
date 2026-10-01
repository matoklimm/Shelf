package io.github.matoklimm.domain.shared

abstract class AggregateRoot<E : Event> {

    var version: Int = 0
        private set

    val pendingEvents = mutableListOf<E>()

    fun raise(event: E) {
        apply(event)
        version++
        pendingEvents.add(event)
    }

    fun replay(event: E) {
        apply(event)
        version++
    }

    abstract fun apply(event: E)
}