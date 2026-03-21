/**
 * File utility functions for the Weaper sync server.
 * Provides MD5 hash computation and directory scanning.
 */

const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

const AUDIO_EXTENSIONS = (process.env.ALLOWED_EXTENSIONS || 'mp3,wav,aif,aiff,flac')
  .split(',')
  .map(ext => ext.trim().toLowerCase());

/**
 * Computes MD5 hash of a file for delta sync comparison.
 * Using MD5 here for speed — not security.
 * @param {string} filePath - Absolute path to file
 * @returns {Promise<string>} Hex-encoded MD5 hash
 */
function computeFileHash(filePath) {
  return new Promise((resolve, reject) => {
    const hash = crypto.createHash('md5');
    const stream = fs.createReadStream(filePath);
    stream.on('error', reject);
    stream.on('data', chunk => hash.update(chunk));
    stream.on('end', () => resolve(hash.digest('hex')));
  });
}

/**
 * Returns all audio files in the given directory (non-recursive).
 * @param {string} dirPath - Directory to scan
 * @returns {Promise<string[]>} Array of absolute file paths
 */
async function getAllFiles(dirPath) {
  if (!fs.existsSync(dirPath)) return [];

  const entries = await fs.promises.readdir(dirPath, { withFileTypes: true });
  return entries
    .filter(entry => entry.isFile() && isAllowedExtension(entry.name))
    .map(entry => path.join(dirPath, entry.name));
}

/**
 * Checks if a filename has an allowed audio extension.
 * @param {string} filename
 * @returns {boolean}
 */
function isAllowedExtension(filename) {
  const ext = path.extname(filename).replace('.', '').toLowerCase();
  return AUDIO_EXTENSIONS.includes(ext);
}

module.exports = { computeFileHash, getAllFiles, isAllowedExtension };
