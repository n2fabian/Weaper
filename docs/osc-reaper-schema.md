# REAPER OSC — Full Control & Feedback Schema

Reference for all OSC communication between Weaper/scripts and REAPER.

---

## OSC Message Format (Protocol)

OSC messages travel over **UDP**. Each message has:

```
/address/path  [type-tag-string]  [arg1] [arg2] ...
```

| Type Tag | Meaning          | Example                  |
|----------|------------------|--------------------------|
| `f`      | 32-bit float     | `0.75`                   |
| `i`      | 32-bit int       | `1`                      |
| `s`      | UTF-8 string     | `"Song One"`             |
| `T`      | True (no value)  | —                        |
| `F`      | False (no value) | —                        |

REAPER sends and receives OSC 1.0 bundles on configurable UDP ports.

---

## Commands — Weaper → REAPER (Outbound)

### Transport

| Action              | OSC Address               | Args     | Notes                        |
|---------------------|---------------------------|----------|------------------------------|
| Play                | `/action/40044`           | —        |                              |
| Stop                | `/action/40047`           | —        |                              |
| Pause               | `/action/40046`           | —        | Toggles pause                |
| Record              | `/action/1013`            | —        |                              |
| Go to start         | `/action/40042`           | —        |                              |
| Go to end           | `/action/40043`           | —        |                              |
| Toggle repeat       | `/action/40625`           | —        |                              |
| Next marker         | `/action/40173`           | —        |                              |
| Previous marker     | `/action/40172`           | —        |                              |

### Navigation

| Action              | OSC Address               | Args     | Notes                              |
|---------------------|---------------------------|----------|------------------------------------|
| Go to Marker N      | `/marker/N`               | —        | N = 1-based marker number          |
| Go to Region N      | `/region/N`               | —        | N = 1-based region number          |
| Jump to time (s)    | `/time`                   | `f`      | Position in seconds                |

### Tracks

| Action              | OSC Address               | Args          | Notes                          |
|---------------------|---------------------------|---------------|--------------------------------|
| Track volume        | `/track/N/volume`         | `f` (0.0–1.0) | N = 1-based track number       |
| Track pan           | `/track/N/pan`            | `f` (-1.0–1.0)|                                |
| Mute track          | `/track/N/mute`           | `i` (0 or 1)  |                                |
| Solo track          | `/track/N/solo`           | `i` (0 or 1)  |                                |
| Arm for record      | `/track/N/recarm`         | `i` (0 or 1)  |                                |

### Actions (Generic)

Any REAPER action can be triggered by its numeric ID:

```
/action/<id>
```

Find action IDs in REAPER via: **Actions → Show action list → right-click → Copy selected action command ID**

---

## Feedback — REAPER → Weaper (Inbound)

REAPER sends feedback on a configurable **Device port** when `Allow binding messages to REAPER actions` is enabled and the remote device has a listen port configured.

### Transport Feedback

| OSC Address     | Type | Description                             | Example            |
|-----------------|------|-----------------------------------------|--------------------|
| `/play`         | `f`  | 1.0 when playing, else 0.0             | `1.0`              |
| `/stop`         | `f`  | 1.0 when stopped                        | `1.0`              |
| `/record`       | `f`  | 1.0 when recording                      | `0.0`              |
| `/pause`        | `f`  | 1.0 when paused                         | `0.0`              |
| `/repeat`       | `f`  | 1.0 if repeat is on                     | `0.0`              |
| `/time`         | `f`  | Current playback position (seconds)     | `42.5`             |
| `/beat/str`     | `s`  | Beat position as string                 | `"3.2.00"`         |
| `/timesig/num`  | `i`  | Time signature numerator                | `4`                |
| `/timesig/denom`| `i`  | Time signature denominator              | `4`                |
| `/tempo`        | `f`  | Current BPM                             | `120.0`            |

### Track Feedback (N = track number, 1-based)

| OSC Address              | Type | Description                         |
|--------------------------|------|-------------------------------------|
| `/track/N/name`          | `s`  | Track name string                   |
| `/track/N/volume`        | `f`  | Volume 0.0–4.0 (1.0 = 0 dB)        |
| `/track/N/pan`           | `f`  | Pan -1.0 (left) to 1.0 (right)      |
| `/track/N/mute`          | `f`  | 1.0 if muted                        |
| `/track/N/solo`          | `f`  | 1.0 if soloed                       |
| `/track/N/recarm`        | `f`  | 1.0 if armed for record             |
| `/track/N/vu/L`          | `f`  | VU meter left channel (0.0–1.0)     |
| `/track/N/vu/R`          | `f`  | VU meter right channel (0.0–1.0)    |

### Marker Feedback

> ⚠️ REAPER does **not** send marker listings via OSC by default.
> Markers must be read by parsing the `.RPP` project file directly or via
> REAPER's Web Remote HTTP API (when enabled at Options → Preferences →
> Control/OSC/Web → Web browser control).
>
> See `scripts/read-markers.js` for the file-parsing approach.

Navigating to a marker via OSC does trigger position-change feedback:

```
/time  f  <new_position_in_seconds>
```

---

## REAPER Project File — Marker Format (.RPP)

REAPER saves markers inline in the `.RPP` project text file:

```
<MARKER 1 42.500000000 "Song One" 0 1 B 0 0 1 ""
>
<MARKER 2 97.230000000 "Song Two" 0 1 B 0 0 1 ""
>
```

| Field Position | Value               | Description                                       |
|----------------|---------------------|---------------------------------------------------|
| 0              | `1`                 | Marker ID (1-based, sequential)                   |
| 1              | `42.500000000`      | Position in seconds                               |
| 2              | `"Song One"`        | Name (quoted string)                              |
| 3              | `0`                 | isRegion: `0` = marker, `1` = region start, `-1` = region end |
| 4              | `1`                 | showInTimeline flag                               |
| 5              | `B`                 | Color encoding (hex or `B` = default blue)        |
| 6–7            | `0 0`               | Reserved / extended flags                        |

Regions consist of two lines: a `<MARKER ... 1 ...>` (start) and `<MARKER ... -1 ...>` (end) with the same ID.

---

## Default Port Convention (Weaper Project)

| Direction        | Port | Protocol | Who listens              |
|------------------|------|----------|--------------------------|
| App → REAPER     | 8000 | UDP      | REAPER (local or remote) |
| REAPER → Scripts | 9000 | UDP      | `read-markers.js` / app  |
| Web Remote       | 8080 | TCP/HTTP | Scripts (same device only)|

---

## Quick Reference — Useful Action IDs

| ID    | Description                            |
|-------|----------------------------------------|
| 40044 | Transport: Play                        |
| 40047 | Transport: Stop                        |
| 40046 | Transport: Pause (toggle)              |
| 1013  | Transport: Record                      |
| 40042 | Transport: Go to start of project      |
| 40043 | Transport: Go to end of project        |
| 40172 | Navigate: Go to previous marker        |
| 40173 | Navigate: Go to next marker            |
| 40625 | View: Toggle repeat                    |
| 40029 | View: Show/hide track mixer            |
| 1007  | Markers: Add/edit current marker       |
| 40115 | Markers: Delete current marker         |
