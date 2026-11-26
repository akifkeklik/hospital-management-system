/**
 * Looks a key up in the reader's language, then English, then Turkish.
 * A reader never sees a raw key such as "status_pending" because one language lags behind.
 */
export function translate(dictionaries, language, key) {
  if (!key) return key;
  const normalizedKey = typeof key === 'string' ? key.normalize('NFC').trim() : key;
  return (
    dictionaries[language]?.[normalizedKey] ||
    dictionaries.en?.[normalizedKey] ||
    dictionaries.tr?.[normalizedKey] ||
    normalizedKey
  );
}
