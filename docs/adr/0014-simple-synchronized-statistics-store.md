# 14. Simple synchronized statistics store

Date: 2026-10-07

## Status

Accepted. Supersedes the concurrency design of [ADR-0009](0009-in-memory-statistics-store.md); its port and documented limitations stay.

## Context

ADR-0009 made the in-memory store lock-free: a `ConcurrentHashMap` of `AtomicLong` counters and a leader replaced by a compare-and-set loop. It was correct and tested, but hard to read and easy to break for developers who are not used to lock-free code. The project must stay easy to maintain, including by junior developers, and nothing here needs lock-free performance.

## Decision

`InMemoryRequestStatistics` keeps a plain `HashMap` of counts and a `leader` field, and both of its methods are `synchronized`. Recording a request increments its count and, if the count strictly exceeds the leader's, makes it the new leader. Reading returns the leader.

## Consequences

- The class reads top to bottom with no concurrency expertise; the counts and the leader can never disagree.
- One thread at a time holds the lock, for well under a microsecond: negligible next to the cost of an HTTP request.
- The concurrency test is unchanged and still proves that no hit is lost; it fails if `synchronized` is removed.
