# REAPER OSC Setup Guide

Step-by-step guide for configuring REAPER to accept OSC commands from the Weaper Android app.

## Prerequisites

- REAPER installed on your MacBook
- Weaper app installed on your Android device
- Both devices connected to the **same WiFi network** (XR18 AP or router)

## Step 1: Find Your MacBook's IP Address

Open Terminal on your Mac:
```bash
ipconfig getifaddr en0   # WiFi
# or
ifconfig | grep "inet "
```

Note this IP — you'll enter it in the Weaper app settings.

## Step 2: Configure OSC in REAPER

1. Open REAPER
2. Navigate to: **Options → Preferences** (or `Cmd+,`)
3. In the left panel, scroll to **Control/OSC/Web**
4. Click **Add**
5. In the **Control surface settings** dialog:
   - **Control surface mode**: `OSC (Open Sound Control)`
6. Click the **Options...** or configure button:
   - **Mode**: `Configure device IP+local port`
   - **Local listen port**: `8000`
   - **Device IP**: *your Android device's IP on the network*
   - **Device port**: `9000` (optional — for REAPER→App feedback)
7. Click **OK**
8. Make sure the device is **enabled** (checkbox checked)

## Step 3: Test the Connection

In Weaper app:
1. Go to **Settings**
2. Enter your MacBook's IP address
3. Set port to `8000`
4. Go to **Setlist**, add a song with Marker ID `1`
5. Tap the play button — REAPER should jump to marker 1

If it doesn't work:
- Check that both devices are on the same subnet
- Check macOS Firewall: System Preferences → Security → Firewall → Allow REAPER
- Try `ping <android-ip>` from Terminal to verify connectivity

## Step 4: Set Up Markers for Setlist

For each song in your setlist:
1. In REAPER, position the playhead at the start of the song
2. **Insert → Marker** (or press **M**)
3. Assign a sequential number (1 for first song, 2 for second, etc.)
4. In Weaper, set that number as the **Marker ID** for the setlist item

## Step 5: Configure Soundboard Tracks

For each sample/sound on your soundboard:
1. In REAPER, create a track for the sample
2. Load the audio file onto that track (in an empty item or via FX)
3. Note the track number (shown in the TCP)
4. In Weaper, create a soundboard button with that **Track ID**

## Firewall Configuration (if needed)

If REAPER doesn't receive OSC messages, allow UDP traffic:

```bash
# Check if REAPER is allowed
sudo /usr/libexec/ApplicationFirewall/socketfilterfw --getappblocked /Applications/REAPER.app

# Allow REAPER if blocked
sudo /usr/libexec/ApplicationFirewall/socketfilterfw --add /Applications/REAPER.app
sudo /usr/libexec/ApplicationFirewall/socketfilterfw --unblock /Applications/REAPER.app
```

## Troubleshooting

| Issue | Solution |
|-------|----------|
| REAPER doesn't respond to OSC | Check IP and port in app Settings; verify REAPER OSC is enabled |
| Can't connect to sync server | Make sure `npm start` is running in `local-server/`; check URL in Settings |
| Firebase sync not working | Verify `google-services.json` is correctly placed; check internet connection |
| Soundboard button shows MISSING | File not synced to server yet; use Sync screen to upload |
| High latency | Switch to 5GHz WiFi; avoid crowded 2.4GHz channels |
