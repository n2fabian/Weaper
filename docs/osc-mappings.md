# OSC Mappings for REAPER

This document lists the OSC commands sent by the Weaper app and how to configure REAPER to respond to them.

## Transport Commands

| Action | OSC Address | REAPER Action ID | Notes |
|--------|-------------|-----------------|-------|
| Play | `/action/40044` | 40044 | Main transport play |
| Stop | `/action/40047` | 40047 | Main transport stop |
| Pause | `/action/40046` | 40046 | Toggle pause |
| Record | `/action/1013` | 1013 | Arm & record |
| Rewind to start | `/action/40042` | 40042 | Go to start of project |

## Navigation Commands

| Action | OSC Address | Example |
|--------|-------------|---------|
| Go to Marker N | `/marker/{N}` | `/marker/1` |
| Go to Region N | `/region/{N}` | `/region/3` |
| Next marker | `/action/40173` | — |
| Previous marker | `/action/40172` | — |

## Track Commands

| Action | OSC Address | Args | Notes |
|--------|-------------|------|-------|
| Trigger track N | `/track/{N}/play` | — | Custom — use with MIDI item or action |
| Mute track N | `/track/{N}/mute` | `1` (mute) or `0` (unmute) | |
| Solo track N | `/track/{N}/solo` | `1` or `0` | |
| Track volume | `/track/{N}/volume` | `0.0–1.0` (float) | |

## REAPER Action IDs Reference

REAPER uses numeric action IDs for most commands. These can be sent via:
```
/action/{id}
```

Commonly used IDs:

| ID | Description |
|----|-------------|
| 40044 | Transport: Play |
| 40047 | Transport: Stop |
| 40046 | Transport: Pause |
| 1013 | Transport: Record |
| 40042 | Transport: Go to start of project |
| 40172 | Navigate: Go to previous marker |
| 40173 | Navigate: Go to next marker |
| 40625 | View: Toggle repeat |

## Configuring REAPER

### Enable OSC in REAPER

1. Open REAPER on your MacBook
2. Go to **Options → Preferences → Control/OSC/Web**
3. Click **Add** to add a new control surface
4. Select **OSC (Open Sound Control)**
5. Configure:
   - **Mode**: `Configure device IP+local port`
   - **Local listen port**: `8000` (must match app setting)
   - **Device IP**: your Android phone's IP address
   - **Device port**: `9000` (if you want REAPER to send feedback)
6. Click **OK** and enable the device

### Setting Up Markers

For setlist navigation to work:
1. In REAPER, position the playhead at the start of each song
2. Press **Shift+M** to add a marker (or **Insert → Marker**)
3. Number markers sequentially (1, 2, 3...)
4. These numbers correspond to `markerId` in your Weaper setlist items

### Setting Up Regions

For region-based navigation:
1. Select a time range in the arrange view
2. Press **Shift+R** to add a region
3. Number regions sequentially
4. Use `regionId` in your Weaper setlist items

## Custom OSC Paths

You can define custom OSC paths for soundboard buttons in Weaper.
REAPER's OSC surface supports binding OSC messages to actions.

Example custom paths:
- `/weaper/sample/1` → bound to a REAPER action that triggers a sample on track 1
- `/weaper/fx/on` → bound to enable an FX chain

To bind a custom OSC path in REAPER:
1. In the OSC device settings, open the OSC pattern file
2. Add a pattern like: `MSG /weaper/sample/1 ACTION 40044`
3. Save and reload

See the [REAPER OSC documentation](https://www.reaper.fm/sdk/osc/osc.php) for full syntax.
