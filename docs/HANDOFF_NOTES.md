# Handoff notes

This archive is intended to be supplied to a coding agent for repository alignment and implementation updates.

## Added
- Standalone Mermaid source diagrams under `docs/uml/*.mmd`.
- Java class responsibility and package design under `docs/classes/CLASS_DESIGN.md`.
- Agent instructions in `AGENT_PROMPT.md`.
- Explicit project status and no-test scope.

## Updated
- Root README and service/phase/backlog documentation to defer tests for the current pass.
- CI workflow is manual-only and does not run tests automatically.
- UML index links to the standalone Mermaid diagrams.

## Preserved
- Existing Inventory source files.
- Existing test files unchanged; no tests were added or run.
- Existing SQL baselines, subject to agent review against the diagrams and class design.

## Not represented as completed
- Full Spring Boot implementations for Catalog, Booking, and Payment.
- End-to-end integration.
- Runtime database/Flyway verification.
- Compilation or automated QA.
