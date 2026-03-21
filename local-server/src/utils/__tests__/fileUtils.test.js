const { computeFileHash, isAllowedExtension } = require('../fileUtils');
const fs = require('fs');
const path = require('path');
const os = require('os');

describe('isAllowedExtension', () => {
  test('allows mp3 files', () => {
    expect(isAllowedExtension('song.mp3')).toBe(true);
  });

  test('allows wav files', () => {
    expect(isAllowedExtension('kick.wav')).toBe(true);
  });

  test('rejects exe files', () => {
    expect(isAllowedExtension('malware.exe')).toBe(false);
  });

  test('rejects files without extension', () => {
    expect(isAllowedExtension('noextension')).toBe(false);
  });

  test('is case insensitive', () => {
    expect(isAllowedExtension('SONG.MP3')).toBe(true);
  });
});

describe('computeFileHash', () => {
  test('computes consistent MD5 hash', async () => {
    const tmpFile = path.join(os.tmpdir(), 'weaper-test.txt');
    fs.writeFileSync(tmpFile, 'hello world');

    const hash1 = await computeFileHash(tmpFile);
    const hash2 = await computeFileHash(tmpFile);

    expect(hash1).toBe(hash2);
    expect(hash1).toMatch(/^[a-f0-9]{32}$/);

    fs.unlinkSync(tmpFile);
  });

  test('different content produces different hash', async () => {
    const tmpFile1 = path.join(os.tmpdir(), 'weaper-test1.txt');
    const tmpFile2 = path.join(os.tmpdir(), 'weaper-test2.txt');

    fs.writeFileSync(tmpFile1, 'content one');
    fs.writeFileSync(tmpFile2, 'content two');

    const hash1 = await computeFileHash(tmpFile1);
    const hash2 = await computeFileHash(tmpFile2);

    expect(hash1).not.toBe(hash2);

    fs.unlinkSync(tmpFile1);
    fs.unlinkSync(tmpFile2);
  });
});
