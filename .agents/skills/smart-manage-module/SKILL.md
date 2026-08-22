---
name: smart-manage-module
description: "Plan, implement, or significantly extend a Smart Manage business module using the repository's architecture, reference modules, page conventions, migrations, and risk-matched verification. Use for end-to-end modules, business aggregates, master data, configuration, records, or consoles; do not use for small local fixes that do not change module boundaries."
---

# Smart Manage Module

Create a module that fits the repository rather than merely matching its technology stack. Treat repository documents and current code as the source of truth; this skill only orchestrates the workflow.

## Required sources

Before implementation, read completely:

1. The root `AGENTS.md` and the `AGENTS.md` of every changed subproject.
2. `docs/development/module-development-guide.md`.
3. `docs/development/module-pattern-catalog.md`.
4. The architecture, security, database, verification, and domain documents routed by the root `AGENTS.md` for the actual task.
5. For a business aggregate, `docs/development/business-aggregate-checklist.md`.

Do not load unrelated domain documents. If a required domain document does not exist, establish the minimum module design in the current task and add that document as part of the implementation.

## Workflow

### 1. Establish the implementation baseline

Inspect current code, configuration, migrations, tests, and documents before asking questions. State or record:

- scope and measurable completion criteria;
- module type and `{domain}/{application}/{module}` ownership;
- stable `featureKey`, pages, permissions, menu entry, and other stable identities;
- state commands, transaction boundary, data ownership, references, sensitive fields, and external side effects;
- the primary reference module plus any focused auxiliary references;
- required verification.

Ask the user only when an unresolved point changes architecture, data safety, external state, or materially different product behavior. Do not invent future features or abstractions without a real consumer.

### 2. Compare the reference before coding

Inspect the selected reference end to end: domain document, migration, backend entry and transaction services, frontend types/API/query keys/permissions/registration/pages, and risk tests. Record intentional differences from the reference. Never copy feature keys, permission codes, business fields, CSS names, or states blindly.

### 3. Implement the smallest complete vertical slice

Keep Feature, permissions, menu, migration, backend API, frontend registration, pages, and tests explicitly connected. Preserve existing user changes and avoid unrelated refactors.

For standard master data and configuration, default to LIST + EDIT. For business aggregates, follow the aggregate checklist. Use CUSTOM only when common page models cannot express the actual interaction.

### 4. Run deterministic and risk-matched verification

Run:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\verify-module-conventions.ps1
```

Then run every command required by `docs/development/verification.md` for the files and risks changed. Page registration changes require regeneration and a clean generated diff; migration changes require an empty-database Flyway verification.

Do not weaken lint, tests, generators, migrations, or architecture checks to make the implementation pass.

### 5. Hand off with evidence

Report the implemented boundary, important design decisions, files or areas changed, commands run and their outcomes, and any unverified or deferred work. A passing convention script does not replace business, security, concurrency, or browser verification.
