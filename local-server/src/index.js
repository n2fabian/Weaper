/**
 * Weaper Local Sync Server
 *
 * A lightweight Express server that runs on the MacBook to handle:
 * - File uploads from the Android app (delta sync)
 * - File listing with MD5 hashes for sync comparison
 *
 * Architecture note: This server is intentionally simple and stateless.
 * It acts as a bridge between the Android app and the local filesystem.
 * All files are stored directly to disk — no database required.
 */

require('dotenv').config();

const express = require('express');
const cors = require('cors');
const morgan = require('morgan');
const path = require('path');
const fs = require('fs');
const { router: filesRouter } = require('./routes/files');

const app = express();
const PORT = process.env.PORT || 3000;
const UPLOAD_DIR = process.env.UPLOAD_DIR || path.join(__dirname, '../uploads');

// Ensure upload directory exists
if (!fs.existsSync(UPLOAD_DIR)) {
  fs.mkdirSync(UPLOAD_DIR, { recursive: true });
  console.log(`Created upload directory: ${UPLOAD_DIR}`);
}

// Middleware
app.use(cors({ origin: process.env.ALLOWED_ORIGINS || '*' }));
app.use(morgan('dev'));
app.use(express.json());

// Make upload dir available to routes
app.locals.uploadDir = UPLOAD_DIR;

// Routes
app.use('/', filesRouter);

// Health check
app.get('/health', (req, res) => {
  res.json({
    status: 'ok',
    uploadDir: UPLOAD_DIR,
    timestamp: new Date().toISOString()
  });
});

// 404 handler
app.use((req, res) => {
  res.status(404).json({ error: 'Not found' });
});

// Error handler
app.use((err, req, res, next) => {
  console.error('Server error:', err);
  res.status(500).json({ error: err.message || 'Internal server error' });
});

app.listen(PORT, () => {
  console.log(`\n🎵 Weaper Sync Server running on port ${PORT}`);
  console.log(`📁 Upload directory: ${UPLOAD_DIR}`);
  console.log(`\nEndpoints:`);
  console.log(`  GET  /health       - Health check`);
  console.log(`  GET  /files        - List files with hashes`);
  console.log(`  POST /upload       - Upload a file`);
  console.log(`  DELETE /files/:name - Delete a file\n`);
});

module.exports = app;
