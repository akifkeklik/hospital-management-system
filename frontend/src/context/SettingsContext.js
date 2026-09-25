'use client';
import { createContext, useContext, useEffect, useCallback, useSyncExternalStore } from 'react';
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

function subscribeToStorage(key) {
  return (callback) => {
    if (typeof window === 'undefined') return () => {};
    const handleStorage = (e) => {
      if (e.key === key) callback();
    };
    const handleCustom = () => callback();
    
    window.addEventListener('storage', handleStorage);
    window.addEventListener(`local-storage-${key}`, handleCustom);
    
    return () => {
      window.removeEventListener('storage', handleStorage);
      window.removeEventListener(`local-storage-${key}`, handleCustom);
    };
  };
}

function useLocalStorage(key, initialValue) {
  const subscribe = useCallback((callback) => subscribeToStorage(key)(callback), [key]);
  const getSnapshot = () => {
    if (typeof window === 'undefined') return initialValue;
    return localStorage.getItem(key) || initialValue;
  };
  const getServerSnapshot = () => initialValue;
  
  const value = useSyncExternalStore(subscribe, getSnapshot, getServerSnapshot);
  
  const setValue = useCallback((newValue) => {
    if (typeof window !== 'undefined') {
      localStorage.setItem(key, newValue);
      window.dispatchEvent(new Event(`local-storage-${key}`));
    }
  }, [key]);
  
  return [value, setValue];
}

function useMounted() {
  const subscribe = useCallback(() => () => {}, []);
  return useSyncExternalStore(subscribe, () => true, () => false);
}

export function SettingsProvider({ children }) {
  const [language, setLanguage] = useLocalStorage('language', 'tr');
  const [themeColor, setThemeColor] = useLocalStorage('themeColor', 'indigo');
  const [theme, setTheme] = useLocalStorage('theme', 'light');
  const mounted = useMounted();

  const applyThemeColor = useCallback((colorId) => {
    setThemeColor(colorId);
  }, [setThemeColor]);

  const toggleTheme = () => {
    setTheme(theme === 'light' ? 'dark' : 'light');
  };

  const changeLanguage = (langId) => {
    setLanguage(langId);
  };

  useEffect(() => {
    if (!mounted) return;
    const colorTheme = THEMES.find(t => t.id === themeColor) || THEMES[0];
    document.documentElement.style.setProperty('--primary', colorTheme.hex);
    document.documentElement.style.setProperty('--primary-hover', colorTheme.hover);
    document.documentElement.style.setProperty('--primary-rgb', colorTheme.rgb);
    document.documentElement.setAttribute('data-theme', theme);
  }, [themeColor, theme, mounted]);

  // Translation function
  const t = (key) => {
    if (!key) return key;
    const normalizedKey = typeof key === 'string' ? key.normalize('NFC').trim() : key;
    if (!dictionaries[language]) return normalizedKey;
    return dictionaries[language][normalizedKey] || normalizedKey;
  };

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
