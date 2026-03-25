# Handoff: OSC Markers — First Live Communication Test

**Date**: 2026-03-25
**From / To**: Linux PC (Android dev) → dawice (MacBook with REAPER)
**Status**: awaiting-pickup

---

## What Was Done on the Linux PC

The Linux PC agent has prepared everything needed to test OSC communication and read
REAPER project markers. The following files were created and committed:

| File | Purpose |
|------|---------|
| `docs/osc-reaper-schema.md` | Full OSC command list + REAPER feedback message schema |
| `scripts/read-markers.js` | Node.js script — reads markers from `.RPP` file; `--osc` flag shows live feedback |
| `scripts/build_and_ship.sh` | Firebase App Distribution build script (separate task) |
| `docs/firebase-app-distribution.md` | Firebase distribution workflow |
| `.agent-relay/context/firebase-distribution.md` | Shared context for Firebase setup |

---

## Current State

- `scripts/read-markers.js` is **ready to run** on the dawice.
- It parses the `.RPP` project file directly (no REAPER plugins needed for the basic read).
- The `--osc` flag requires `npm install osc` and REAPER OSC configured to send feedback
  to port `9000` on the same machine.
- **We do NOT yet know** the exact state of REAPER OSC configuration on the dawice.
- **We do NOT yet have a tested end-to-end flow** between the Android app and REAPER.

---

## Pending Work (for the dawice agent)

- [ ] **Pull this repository** on the MacBook.
- [ ] **Enable OSC in REAPER** — follow steps in `docs/reaper-osc-setup.md`; the
      detailed settings needed for localhost same-device testing are spelled out below.
- [ ] **Run `read-markers.js` against the current REAPER project** — verify markers
      are read correctly.
- [ ] **Run `read-markers.js --osc`** — verify live OSC feedback appears from REAPER.
- [ ] **Validate the OSC command schema** — confirm addresses in `docs/osc-reaper-schema.md`
      work with the installed REAPER version.
- [ ] **Set up Node.js** on the dawice if not already installed:
      `brew install node` or via nvm.
- [ ] **(Later)** Set up cross-device connection from Android phone to dawice over
      WiFi, then hand off back to Linux PC.

---

## Steps to Perform in REAPER (dawice agent: follow this)

### Step 1 — Enable OSC in REAPER (same-device test mode)

1. Open REAPER.
2. Go to **Options → Preferences** (`Cmd+,`).
3. In the left panel, select **Control/OSC/Web**.
4. Click **Add** → select **OSC (Open Sound Control)**.
5. In the dialog that opens, set:
   - **Mode**: `Configure device IP+local port`
   - **Local listen port**: `8000`
   - **Device IP**: `127.0.0.1` ← localhost for same-device testing
   - **Device port**: `9000` ← `read-markers.js --osc` listens here
   - ✅ Check: **Allow binding messages to REAPER actions and FX parameters**
6. Click **OK** and make sure the device row is **enabled** (checkbox on the left).
7. Restart REAPER for changes to take effect.

> ⚠️ When you later test cross-device (Android phone → dawice), replace
> `127.0.0.1` with the Android phone's IP on the shared network.
> The local listen port (`8000`) stays the same.

### Step 2 — Enable Web Remote (optional but useful for debugging)

1. Still in **Options → Preferences → Control/OSC/Web**.
2. Click **Add** → select **Web browser control**.
3. Set port to `8080`.
4. Enable it.
5. Visit `http://localhost:8080/` in a browser — confirms REAPER is reachable.

### Step 3 — Create test markers in the REAPER project

If the current project has no markers yet:
1. Position the playhead at the start of the first song/section.
2. Press **M** (or **Insert → Marker**) to add a marker.
3. In the marker dialog, set **ID = 1** and a name (e.g. "Intro").
4. Repeat for a second marker at a different position (ID = 2, name = "Verse").
5. Save the project (`Cmd+S`).

### Step 4 — Run the read-markers script

```bash
# From the repo root:
node scripts/read-markers.js "/path/to/your/Project.rpp"
```

Expected output example:

```
════════════════════════════════════════════
  REAPER Project: MyShow
  Source file:    /Users/.../MyShow.rpp
════════════════════════════════════════════

┌─ MARKERS (2) ────────────────────────────┐
│  ID   Position      Name                  │
│  ──   ────────────  ─────────────────     │
│  1    00:00.000     Intro                  │
│  2    01:43.520     Verse                  │
└───────────────────────────────────────────┘
```

### Step 5 — Run with OSC feedback enabled

```bash
# Install the osc package first (once):
npm install osc

# Then run with --osc flag (keep terminal open):
node scripts/read-markers.js "/path/to/your/Project.rpp" --osc
```

Then in REAPER, press **Play** — you should see OSC feedback lines appear:

```
← /play  1   [from 127.0.0.1:8000]
← /time  0.0  [from 127.0.0.1:8000]
← /beat/str  "1.1.00"  [from 127.0.0.1:8000]
```

### Step 6 — Test sending OSC commands to REAPER

You can quickly test if REAPER receives commands via Node.js (one-liner):

```bash
node -e "
const osc = require('osc');
const p = new osc.UDPPort({ remoteAddress: '127.0.0.1', remotePort: 8000 });
p.on('ready', () => { p.send({ address: '/action/40044', args: [] }); setTimeout(() => p.close(), 500); });
p.open();
"
```

REAPER should start playing. `/action/40047` stops it.

---

## What to Report Back (hand back to Linux PC)

When the above steps are complete, create a new handoff file at:

`.agent-relay/handoff/2026-03-25-osc-cross-device.md`

Include:
- ✅/❌ status for each Pending Work item above
- The exact REAPER OSC feedback messages observed (copy/paste from terminal)
- Any differences from the schema in `docs/osc-reaper-schema.md`
- REAPER version number on the dawice
- The macOS network interface name and IP to use for cross-device testing
  (run `ipconfig getifaddr en0` in Terminal — or whichever interface is on the shared network)
- Any REAPER setup quirks encountered

---

## Environment Notes (no secrets)

- REAPER project files are at: `~/Documents/REAPER Media/` or `~/Documents/REAPER/Projects/` (default macOS paths)
- Node.js location on dawice: TBD (check with `which node` or `brew list node`)
- OSC listen port for same-device: **8000** (REAPER) ↔ **9000** (script)
- OSC listen port for cross-device: **8000** (REAPER, same) — Android phone IP replaces `127.0.0.1`
- Firebase credentials location: not yet set up — deferred

---

*Picked up by: — (awaiting dawice)*
