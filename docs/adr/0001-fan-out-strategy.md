# ADR-0001 — Hybrid fan-out for the home timeline

- **Status:** Accepted
- **Date:** 2026-06

## Context

The home timeline (recent posts from everyone you follow) is the read hot path of a feed product —
read far more often than written. Two classic ways to build it:

- **Fan-out on read** — at read time, gather the latest posts from everyone you follow and merge.
  Cheap writes, expensive reads (a query per followee, every read).
- **Fan-out on write** — when someone posts, push the post into each follower's pre-computed timeline.
  Reads are a single range scan, but a post by a user with millions of followers causes millions of
  writes — the "celebrity problem."

## Decision

Use a **hybrid**:

- **Normal authors → fan-out on write.** On post, push the post id onto each follower's
  `timeline:{userId}` Redis sorted set (scored by timestamp). Reads are one `ZREVRANGE`.
- **Celebrity authors (followers > threshold) → fan-out on read.** Skip the fan-out and flag the
  author. At read time, pull their recent posts from `posts:{authorId}` and merge into the timeline.

Redis sorted sets are the timelines; PostgreSQL is the source of truth for users, follows, and post
content (hydrated by id after the merge).

## Rationale

- Most authors have few followers, so fan-out-on-write keeps the common read path to a single range
  scan with no per-followee queries.
- Celebrities are rare but catastrophic for fan-out-on-write; pulling their handful of posts at read
  time bounds the write amplification.
- The threshold makes the trade-off explicit and tunable.

## Consequences

- Verified: a normal author's post lands in followers' pre-computed timelines; a celebrity's post is
  not fanned out and instead appears via the read-time pull — both visible in the same merged feed.
- Eventual consistency: fan-out happens just after the DB commit and is best-effort against Redis; a
  Redis failure could drop a push (the author feed in Postgres remains the source of truth and could
  rebuild it). Hardening (e.g. an outbox-style rebuild) is roadmap.
- Timelines are capped (newest N) to bound memory.

## Alternatives considered

- **Pure fan-out on write** — simplest reads, but the celebrity problem makes it unviable at scale.
- **Pure fan-out on read** — simplest writes, but every read pays a per-followee cost; poor for a
  read-heavy feed.
- **Per-post fan-out via a queue** — fan-out asynchronously through Kafka for very large follower
  sets. A natural next step; the synchronous push is the right first cut for this scale.
