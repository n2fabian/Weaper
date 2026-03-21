/**
 * File management routes for the Weaper sync server.
 *
 * Endpoints:
 * - GET /files       List all audio files with their MD5 hashes and sizes
 * - POST /upload     Upload a new audio file (multipart/form-data)
 * - DELETE /files/:name  Delete a specific file
 */

const express = require('express');
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const { computeFileHash, getAllFiles, isAllowedExtension } = require('../utils/fileUtils');

const router = express.Router();

// Configure multer for file uploads
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    const uploadDir = req.app.locals.uploadDir;
    cb(null, uploadDir);
  },
  filename: (req, file, cb) => {
    // Sanitize filename: remove path traversal characters
    const safeName = path.basename(file.originalname).replace(/[^a-zA-Z0-9._-]/g, '_');
    cb(null, safeName);
  }
});

const fileFilter = (req, file, cb) => {
  if (isAllowedExtension(file.originalname)) {
    cb(null, true);
  } else {
    cb(new Error(`File type not allowed. Allowed: ${process.env.ALLOWED_EXTENSIONS || 'mp3,wav,aif,aiff,flac'}`));
  }
};

const upload = multer({
  storage,
  fileFilter,
  limits: {
    fileSize: parseInt(process.env.MAX_FILE_SIZE) || 52428800 // 50MB default
  }
});

/**
 * GET /files
 * Returns a list of all audio files in the upload directory,
 * including their MD5 hash and size for delta sync comparison.
 */
router.get('/files', async (req, res) => {
  try {
    const uploadDir = req.app.locals.uploadDir;
    const files = await getAllFiles(uploadDir);

    const fileList = await Promise.all(
      files.map(async (filePath) => {
        const stats = fs.statSync(filePath);
        const hash = await computeFileHash(filePath);
        return {
          name: path.basename(filePath),
          hash,
          size: stats.size
        };
      })
    );

    res.json({ files: fileList });
  } catch (err) {
    console.error('Error listing files:', err);
    res.status(500).json({ error: 'Failed to list files' });
  }
});

/**
 * POST /upload
 * Accepts a multipart form upload with a 'file' field.
 * Returns success/failure with the stored filename.
 */
router.post('/upload', upload.single('file'), (req, res) => {
  if (!req.file) {
    return res.status(400).json({ success: false, message: 'No file provided' });
  }

  console.log(`Uploaded: ${req.file.filename} (${req.file.size} bytes)`);

  res.json({
    success: true,
    message: `File uploaded successfully`,
    filename: req.file.filename,
    size: req.file.size
  });
});

/**
 * DELETE /files/:name
 * Deletes a specific file by name.
 */
router.delete('/files/:name', (req, res) => {
  const uploadDir = req.app.locals.uploadDir;
  // Sanitize to prevent path traversal
  const safeName = path.basename(req.params.name);
  const filePath = path.join(uploadDir, safeName);

  if (!fs.existsSync(filePath)) {
    return res.status(404).json({ success: false, message: 'File not found' });
  }

  fs.unlinkSync(filePath);
  console.log(`Deleted: ${safeName}`);
  res.json({ success: true, message: `${safeName} deleted` });
});

module.exports = { router };
