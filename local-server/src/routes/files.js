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
const rateLimit = require('express-rate-limit');
const { computeFileHash, getAllFiles, isAllowedExtension } = require('../utils/fileUtils');

const router = express.Router();

const apiLimiter = rateLimit({
  windowMs: 60 * 1000, // 1 minute
  max: 100,
  standardHeaders: true,
  legacyHeaders: false,
  message: { error: 'Too many requests, please try again later.' }
});

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
router.get('/files', apiLimiter, async (req, res) => {
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
router.post('/upload', apiLimiter, upload.single('file'), (req, res) => {
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
router.delete('/files/:name', apiLimiter, (req, res) => {
  const uploadDir = req.app.locals.uploadDir;
  // Sanitize to prevent path traversal
  const safeName = path.basename(req.params.name);
  const filePath = path.join(uploadDir, safeName);

  if (!fs.existsSync(filePath)) {
    return res.status(404).json({ success: false, message: 'File not found' });
  }

  fs.promises.unlink(filePath).then(() => {
    console.log(`Deleted: ${safeName}`);
    res.json({ success: true, message: `${safeName} deleted` });
  }).catch((err) => {
    console.error('Error deleting file:', err);
    res.status(500).json({ success: false, message: 'Failed to delete file' });
  });
});

module.exports = { router };
