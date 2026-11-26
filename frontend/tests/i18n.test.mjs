import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync, readdirSync, statSync } from 'node:fs';
import { join, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';
import { dictionaries } from '../src/locales/index.js';
import { translate } from '../src/utils/translate.js';

const SRC = join(dirname(fileURLToPath(import.meta.url)), '..', 'src');

function sourceFiles(dir) {
  return readdirSync(dir).flatMap((name) => {
    const path = join(dir, name);
    if (statSync(path).isDirectory()) return name === 'locales' ? [] : sourceFiles(path);
    return /\.jsx?$/.test(name) ? [path] : [];
  });
}

test('every language has every Turkish key, none of them empty', () => {
  const keys = Object.keys(dictionaries.tr);
  for (const [language, dictionary] of Object.entries(dictionaries)) {
    const missing = keys.filter((key) => typeof dictionary[key] !== 'string' || !dictionary[key].trim());
    assert.deepEqual(missing, [], `${language} is missing ${missing.length} keys`);
  }
});

test('every literal key the code asks for exists, so no screen shows a raw key', () => {
  const missing = new Set();
  for (const file of sourceFiles(SRC)) {
    for (const match of readFileSync(file, 'utf8').matchAll(/\bt\(\s*['"]([a-z0-9_]+)['"]\s*\)/g)) {
      if (!(match[1] in dictionaries.tr)) missing.add(`${match[1]} (${file.slice(SRC.length + 1)})`);
    }
  }
  assert.deepEqual([...missing], []);
});

test('translate uses the reader language, then English, then Turkish, then the key', () => {
  const d = { tr: { a: 'A-tr', b: 'B-tr', c: 'C-tr' }, en: { a: 'A-en', b: 'B-en' }, fr: { a: 'A-fr' } };
  assert.equal(translate(d, 'fr', 'a'), 'A-fr');
  assert.equal(translate(d, 'fr', 'b'), 'B-en');
  assert.equal(translate(d, 'fr', 'c'), 'C-tr');
  assert.equal(translate(d, 'fr', 'zzz'), 'zzz');
  assert.equal(translate(d, 'xx', 'a'), 'A-en', 'an unknown language falls back too');
});

test('translate normalises the key and passes empty keys through', () => {
  const d = { tr: { 'Göz Hastalıkları': 'x' }, en: {} };
  assert.equal(translate(d, 'tr', '  Göz Hastalıkları '.normalize('NFD')), 'x');
  assert.equal(translate(d, 'tr', ''), '');
  assert.equal(translate(d, 'tr', undefined), undefined);
});
