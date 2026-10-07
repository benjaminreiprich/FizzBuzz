# 1. Record architecture decisions

Date: 2026-10-07

## Status

Accepted

## Context

This repository will be reviewed by people who were not there when choices were made. Code shows *what* was decided; it rarely shows *why*, or which alternatives were rejected.

## Decision

We record every non-trivial decision as an Architecture Decision Record (ADR) in `docs/adr/NNNN-short-title.md`, following Michael Nygard's format: Context, Decision, Consequences. ADRs stay short (10–20 lines) and are never rewritten; a changed decision gets a new ADR that supersedes the old one. The README lists each ADR in one line.

## Consequences

- Reviewers can follow the reasoning without access to the authors.
- Writing an ADR costs a few minutes per decision, which is also a useful check that the decision is actually justified.
