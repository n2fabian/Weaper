#!/usr/bin/env node
/**
 * read-markers.js
 *
 * Reads all markers from a REAPER project file (.RPP) and prints them.
 * Optionally starts an OSC listener to show live feedback from REAPER.
 *
 * Usage:
 *   node scripts/read-markers.js path/to/project.rpp
 *   node scripts/read-markers.js path/to/project.rpp --osc
 *
 * --osc flag: also opens a UDP listener on port 9000 and shows all
 *             OSC messages REAPER sends back in real time.
 *
 * Requirements:
 *   npm install osc          (only needed for --osc flag)
 *
 * REAPER setup needed (see docs/reaper-osc-setup.md):
 *   - For marker reading: no REAPER changes needed (reads .RPP file directly)
 *   - For --osc feedback:
 *       Options → Preferences → Control/OSC/Web → Add → OSC
 *       Device IP: <this machine's IP>
 *       Device port: 9000
 *       Local listen port: 8000
 *       Mode: Configure device IP+local port
 */

'use strict';

const fs = require('fs');
const path = require('path');

// ─── Argument parsing ───────────────────────────────────────────────────────

const args = process.argv.slice(2);
const rppPath = args.find(a => !a.startsWith('--'));
const oscMode = args.includes('--osc');

if (!rppPath) {
  console.error('Usage: node scripts/read-markers.js <project.rpp> [--osc]');
  console.error('');
  console.error('Example:');
  console.error('  node scripts/read-markers.js ~/Documents/REAPER/Projects/MyShow.rpp');
  console.error('  node scripts/read-markers.js ~/Documents/REAPER/Projects/MyShow.rpp --osc');
  process.exit(1);
}

// ─── RPP Marker Parser ───────────────────────────────────────────────────────

/**
 * Parse a REAPER .RPP file and extract all markers and region boundaries.
 *
 * REAPER marker line format inside .RPP:
 *   <MARKER <id> <position_seconds> <name> <isRegion> <showInTimeline> <color> ...>
 *
 *   isRegion = 0  → plain marker
 *   isRegion = 1  → region start
 *   isRegion = -1 → region end
 *
 * @param {string} filePath - absolute or relative path to the .RPP file
 * @returns {{ markers: Marker[], regions: Region[] }}
 */
function parseRpp(filePath) {
  const absPath = path.resolve(filePath);

  if (!fs.existsSync(absPath)) {
    console.error(`File not found: ${absPath}`);
    process.exit(1);
  }

  const raw = fs.readFileSync(absPath, 'utf8');
  const lines = raw.split(/\r?\n/);

  const markerRaw = [];

  for (const line of lines) {
    const trimmed = line.trim();
    // Match lines that start a MARKER block: <MARKER id pos name isRegion ...
    if (/^<MARKER\s/i.test(trimmed)) {
      markerRaw.push(trimmed);
    }
  }

  const markers = [];
  const regionStarts = new Map(); // id → { id, posStart, name }
  const regions = [];

  for (const line of markerRaw) {
    // Strip the leading '<' and trailing '>' if present on same line
    const stripped = line.replace(/^</, '').replace(/>$/, '').trim();

    // Tokenise: handles quoted names
    const tokens = tokenise(stripped);

    // tokens[0] = 'MARKER'
    // tokens[1] = id
    // tokens[2] = position (seconds)
    // tokens[3] = name
    // tokens[4] = isRegion flag
    // tokens[5] = showInTimeline
    // tokens[6] = color (hex string or 'B')

    if (tokens.length < 5) continue;

    const id       = parseInt(tokens[1], 10);
    const posSec   = parseFloat(tokens[2]);
    const name     = tokens[3] || '';
    const isRegion = parseInt(tokens[4], 10);
    const color    = tokens[6] || '';

    const posFormatted = secondsToTimecode(posSec);

    if (isRegion === 0) {
      // Plain marker
      markers.push({ id, posSec, posFormatted, name, color });
    } else if (isRegion === 1) {
      // Region start
      regionStarts.set(id, { id, posStart: posSec, posStartFormatted: posFormatted, name, color });
    } else if (isRegion === -1) {
      // Region end — pair with start
      const start = regionStarts.get(id);
      if (start) {
        regions.push({
          id,
          posStart: start.posStart,
          posStartFormatted: start.posStartFormatted,
          posEnd: posSec,
          posEndFormatted: posFormatted,
          name: start.name,
          color: start.color,
        });
        regionStarts.delete(id);
      }
    }
  }

  // Sort by position
  markers.sort((a, b) => a.posSec - b.posSec);
  regions.sort((a, b) => a.posStart - b.posStart);

  return { markers, regions };
}

/**
 * Simple tokeniser that respects quoted strings.
 * "Hello World" → single token 'Hello World' (without quotes).
 */
function tokenise(str) {
  const tokens = [];
  let i = 0;
  while (i < str.length) {
    // Skip whitespace
    while (i < str.length && /\s/.test(str[i])) i++;
    if (i >= str.length) break;

    if (str[i] === '"') {
      // Quoted token
      i++;
      let start = i;
      while (i < str.length && str[i] !== '"') i++;
      tokens.push(str.slice(start, i));
      i++; // skip closing "
    } else {
      let start = i;
      while (i < str.length && !/\s/.test(str[i])) i++;
      tokens.push(str.slice(start, i));
    }
  }
  return tokens;
}

/**
 * Convert fractional seconds to HH:MM:SS.mmm string.
 */
function secondsToTimecode(seconds) {
  const h = Math.floor(seconds / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  const s = Math.floor(seconds % 60);
  const ms = Math.round((seconds % 1) * 1000);
  return [
    h > 0 ? String(h).padStart(2, '0') + ':' : '',
    String(m).padStart(2, '0') + ':',
    String(s).padStart(2, '0') + '.',
    String(ms).padStart(3, '0'),
  ].join('');
}

// ─── Output ──────────────────────────────────────────────────────────────────

function printMarkers({ markers, regions }) {
  const projectName = path.basename(rppPath, '.rpp').replace('.RPP', '');
  console.log('');
  console.log(`════════════════════════════════════════════`);
  console.log(`  REAPER Project: ${projectName}`);
  console.log(`  Source file:    ${path.resolve(rppPath)}`);
  console.log(`════════════════════════════════════════════`);

  // ── Markers ──
  console.log('');
  console.log(`┌─ MARKERS (${markers.length}) ${'─'.repeat(Math.max(0, 38 - String(markers.length).length))}┐`);
  if (markers.length === 0) {
    console.log('│  (none)                                   │');
  } else {
    console.log('│  ID   Position      Name                  │');
    console.log('│  ──   ────────────  ─────────────────     │');
    for (const m of markers) {
      const id   = String(m.id).padEnd(4);
      const pos  = m.posFormatted.padEnd(12);
      const name = (m.name || '(unnamed)').substring(0, 22).padEnd(22);
      console.log(`│  ${id} ${pos}  ${name}     │`);
    }
  }
  console.log('└───────────────────────────────────────────┘');

  // ── Regions ──
  console.log('');
  console.log(`┌─ REGIONS (${regions.length}) ${'─'.repeat(Math.max(0, 38 - String(regions.length).length))}┐`);
  if (regions.length === 0) {
    console.log('│  (none)                                   │');
  } else {
    console.log('│  ID   Start         End           Name    │');
    console.log('│  ──   ───────────   ───────────── ─────── │');
    for (const r of regions) {
      const id    = String(r.id).padEnd(4);
      const start = r.posStartFormatted.padEnd(13);
      const end   = r.posEndFormatted.padEnd(13);
      const name  = (r.name || '(unnamed)').substring(0, 8).padEnd(8);
      console.log(`│  ${id} ${start}  ${end} ${name} │`);
    }
  }
  console.log('└───────────────────────────────────────────┘');
  console.log('');
}

// ─── OSC Listener (optional) ─────────────────────────────────────────────────

function startOscListener() {
  let osc;
  try {
    osc = require('osc');
  } catch (_) {
    console.error('');
    console.error('  OSC mode requires the "osc" npm package.');
    console.error('  Install it with:  npm install osc');
    console.error('');
    process.exit(1);
  }

  const LISTEN_PORT  = 9000;  // Port REAPER sends feedback to (set in REAPER prefs)
  const REAPER_PORT  = 8000;  // Port REAPER listens on (for sending commands)
  const REAPER_HOST  = '127.0.0.1';

  const udpPort = new osc.UDPPort({
    localAddress: '0.0.0.0',
    localPort: LISTEN_PORT,
    remoteAddress: REAPER_HOST,
    remotePort: REAPER_PORT,
    metadata: true,
  });

  udpPort.on('ready', () => {
    console.log(`OSC listener ready on UDP port ${LISTEN_PORT}`);
    console.log(`Sending OSC to REAPER at ${REAPER_HOST}:${REAPER_PORT}`);
    console.log('');
    console.log('Waiting for OSC messages from REAPER...');
    console.log('(Press Ctrl+C to stop)');
    console.log('');

    // Send a Play/Stop ping to test the connection
    console.log('→ Sending transport status query (transport feedback should appear below)');
    // REAPER doesn't have a "ping" command — just send a no-op action
    // Action 65535 does nothing but will show communication is alive
    udpPort.send({ address: '/action/40047', args: [] }); // Stop (safe no-op if already stopped)
  });

  udpPort.on('message', (oscMsg, timeTag, info) => {
    const args = (oscMsg.args || []).map(a =>
      typeof a === 'object' && a !== null ? JSON.stringify(a) : String(a)
    );
    const argStr = args.length ? `  ${args.join('  ')}` : '';
    console.log(`← ${oscMsg.address}${argStr}   [from ${info.address}:${info.port}]`);
  });

  udpPort.on('error', (err) => {
    console.error(`OSC error: ${err.message}`);
  });

  udpPort.open();
}

// ─── Main ────────────────────────────────────────────────────────────────────

const result = parseRpp(rppPath);
printMarkers(result);

if (oscMode) {
  startOscListener();
} else {
  console.log('Tip: add --osc flag to also see live OSC feedback from REAPER.');
  console.log('');
}
