import { test } from 'node:test';
import assert from 'node:assert/strict';
import { translateError } from '../src/utils/errorTranslator.js';

test('translates a known backend message into the reader language', () => {
  assert.equal(
    translateError('Hata: Bu e-posta adresi zaten kullanılıyor.', 'en'),
    'This email address is already in use.'
  );
});

test('matches a known message inside a longer one', () => {
  assert.equal(
    translateError('400 BAD_REQUEST Hata: Bu e-posta adresi zaten kullanılıyor.', 'de'),
    'Diese E-Mail-Adresse wird bereits verwendet.'
  );
});

test('returns unknown messages unchanged and empty input as empty', () => {
  assert.equal(translateError('  Beklenmeyen bir durum.  ', 'en'), 'Beklenmeyen bir durum.');
  assert.equal(translateError('', 'en'), '');
  assert.equal(translateError(undefined, 'en'), '');
});
