'use client';
import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { dictionaries } from '../locales';
import { translateError } from '../utils/errorTranslator';

export const THEMES = [
  { id: 'indigo', name: 'İndigo', hex: '#4f46e5', hover: '#4338ca', rgb: '79, 70, 229' },
  { id: 'rose', name: 'Gül', hex: '#e11d48', hover: '#be123c', rgb: '225, 29, 72' },
  { id: 'emerald', name: 'Zümrüt', hex: '#10b981', hover: '#059669', rgb: '16, 185, 129' },
  { id: 'amber', name: 'Kehribar', hex: '#d97706', hover: '#b45309', rgb: '217, 119, 6' },
  { id: 'cyan', name: 'Siyan', hex: '#0891b2', hover: '#0e7490', rgb: '8, 145, 178' },
  { id: 'violet', name: 'Menekşe', hex: '#7c3aed', hover: '#6d28d9', rgb: '124, 58, 237' },
  { id: 'blue', name: 'Mavi', hex: '#2563eb', hover: '#1d4ed8', rgb: '37, 99, 235' },
  { id: 'orange', name: 'Turuncu', hex: '#ea580c', hover: '#c2410c', rgb: '234, 88, 12' },
  { id: 'fuchsia', name: 'Fuşya', hex: '#c026d3', hover: '#a21caf', rgb: '192, 38, 211' },
  { id: 'teal', name: 'Çam Yeşili', hex: '#0d9488', hover: '#0f766e', rgb: '13, 148, 136' },
  { id: 'lime', name: 'Misket Limonu', hex: '#65a30d', hover: '#4d7c0f', rgb: '101, 163, 13' },
  { id: 'slate', name: 'Arduvaz', hex: '#475569', hover: '#334155', rgb: '71, 85, 105' }
];

export const LANGUAGES = [
  { id: 'tr', name: 'Türkçe', flag: '🇹🇷' },
  { id: 'en', name: 'English', flag: '🇬🇧' },
  { id: 'de', name: 'Deutsch', flag: '🇩🇪' },
  { id: 'fr', name: 'Français', flag: '🇫🇷' },
  { id: 'es', name: 'Español', flag: '🇪🇸' },
  { id: 'ru', name: 'Русский', flag: '🇷🇺' },
  { id: 'ar', name: 'العربية', flag: '🇸🇦' },
  { id: 'zh', name: '中文', flag: '🇨🇳' }
];

const SettingsContext = createContext({
  language: 'tr',
  changeLanguage: () => {},
  t: (key) => key,
  tErr: (msg) => msg,
  themeColor: 'indigo',
  applyThemeColor: () => {},
  theme: 'light',
  toggleTheme: () => {},
  THEMES,
  LANGUAGES,
});



export function SettingsProvider({ children }) {
  const [language, setLanguage] = useState('tr');
  const [themeColor, setThemeColor] = useState('indigo');
  const [theme, setTheme] = useState('light');
  const [mounted, setMounted] = useState(false);

  const applyThemeColor = useCallback((colorId) => {
    const colorTheme = THEMES.find(t => t.id === colorId) || THEMES[0];
    setThemeColor(colorId);
    localStorage.setItem('themeColor', colorId);

    // Uygula (CSS Variable injection)
    document.documentElement.style.setProperty('--primary', colorTheme.hex);
    document.documentElement.style.setProperty('--primary-hover', colorTheme.hover);
    document.documentElement.style.setProperty('--primary-rgb', colorTheme.rgb);
  }, []);

  useEffect(() => {
    const savedLang = localStorage.getItem('language') || 'tr';
    const savedThemeColor = localStorage.getItem('themeColor') || 'indigo';
    const savedTheme = localStorage.getItem('theme') || 'light';

    setLanguage(savedLang);
    applyThemeColor(savedThemeColor);
    setTheme(savedTheme);
    document.documentElement.setAttribute('data-theme', savedTheme);
    setMounted(true);
  }, [applyThemeColor]);

  const toggleTheme = () => {
    const newTheme = theme === 'light' ? 'dark' : 'light';
    setTheme(newTheme);
    localStorage.setItem('theme', newTheme);
    document.documentElement.setAttribute('data-theme', newTheme);
  };

  const changeLanguage = (langId) => {
    setLanguage(langId);
    localStorage.setItem('language', langId);
  };

  // Translation function
  const t = (key) => {
    if (!key) return key;
    // Normalize unicode (e.g. ğ vs g+breve) and trim whitespace
    const normalizedKey = typeof key === 'string' ? key.normalize('NFC').trim() : key;
    if (!dictionaries[language]) return normalizedKey;
    return dictionaries[language][normalizedKey] || normalizedKey;
  };

  // Error translation function (backend hata mesajlarını kullanıcı diline çevirir)
  const tErr = (rawMessage) => translateError(rawMessage, language);

  return (
    <SettingsContext.Provider value={{
      language, changeLanguage, t, tErr,
      themeColor, applyThemeColor,
      theme, toggleTheme,
      THEMES, LANGUAGES
    }}>
      <div style={{ visibility: mounted ? 'visible' : 'hidden' }}>
        {children}
      </div>
    </SettingsContext.Provider>
  );
}

export function useSettings() {
  return useContext(SettingsContext);
}
