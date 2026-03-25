# Agent Relay

Cross-device agent handover folder. The Linux development PC and the MacBook (running
REAPER/DAW) both have VS Code + GitHub Copilot open on this repository. Files written
here are committed and pushed so the agent on the other device can pick up where the
first left off.

## Protocol

1. The initiating device writes a handoff file in `.agent-relay/handoff/`.
2. Commits and pushes.
3. The receiving device pulls; the agent reads the handoff file before continuing.
4. The receiving agent appends a receipt line when it picks up the task.

## Folder Structure

```
.agent-relay/
├── README.md           This file
├── context/            Long-lived shared context (tech stack, env notes)
│   └── *.md
└── handoff/            In-progress handoff notes (one file per task/session)
    └── YYYY-MM-DD-<topic>.md
```

## Handoff File Format

Each file in `handoff/` uses:

```
# Handoff: <topic>
**Date**: YYYY-MM-DD
**From / To**: <device descriptions>
**Status**: in-progress | awaiting-pickup | complete

## Current State
## Pending Work  (checked list)
## Decisions Made
## Environment Notes  (paths only — no secrets)
---
*Picked up by: <device> on YYYY-MM-DD*
```

## Rules

- Never store secrets here — reference their location only.
- Use ISO 8601 dates: `2026-03-25-topic.md`
- Mark `Status: complete` when both sides are done.
- `.agent-relay/` is committed to VCS, not in `.gitignore`.
