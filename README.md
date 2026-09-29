<p align="center">
  <img src="./docs/shelf-icon.svg" width="120" alt="Shelf logo">
</p>

# Shelf
A small library application built with Kotlin to explore Event Sourcing and Domain-Driven Design.

# About

This is a learning project, not a production system. I started it to explore Event Sourcing more deeply after using similar ideas in my Master's thesis (an event-based home automation system) and, more recently, applying them professionally in an append-only, event-derived state model.

The domain is intentionally simple: a library where members can borrow and return book copies. The goal isn't a complete application, but a small enough problem to focus on the pattern itself — commands, events, and a single aggregate — without the domain complexity getting in the way.

## Current state

🚧🚧 Work in progress. So far this includes:

- One aggregate (BookCopy)
- A set of commands and the events they produce
- A handful of tests around the core behavior

Not yet implemented: persistence/event store, an API layer, and read models / projections.

# Why Event Sourcing
What interests me about the pattern is less the mechanics and more what it makes possible: state as the result of a sequence of things that happened, rather than just the current snapshot. I'm using this project to work through the practical side of that — designing events and commands, and thinking about how state should be derived and evolved over time.
