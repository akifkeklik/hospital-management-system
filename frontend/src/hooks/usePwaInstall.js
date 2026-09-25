'use client';
import { useState, useEffect, useSyncExternalStore } from 'react';

let isAppInstalledSession = false;
const listeners = new Set();

const notifyListeners = () => {
  listeners.forEach(listener => listener());
};

const subscribeToAppInstalled = (callback) => {
  if (typeof window === 'undefined') return () => {};
  listeners.add(callback);
  
  const mql = window.matchMedia('(display-mode: standalone)');
  const handleAppInstalled = () => {
    isAppInstalledSession = true;
    notifyListeners();
  };

  mql.addEventListener('change', callback);
  window.addEventListener('appinstalled', handleAppInstalled);
  
  return () => {
    listeners.delete(callback);
    mql.removeEventListener('change', callback);
    window.removeEventListener('appinstalled', handleAppInstalled);
  };
};

const getInstalledSnapshot = () => {
  if (typeof window === 'undefined') return false;
  return isAppInstalledSession || window.matchMedia('(display-mode: standalone)').matches;
};

const getServerInstalledSnapshot = () => false;

export function usePwaInstall() {
  const [deferredPrompt, setDeferredPrompt] = useState(null);
  const [isInstallable, setIsInstallable] = useState(false);
  
  const isInstalled = useSyncExternalStore(
    subscribeToAppInstalled, 
    getInstalledSnapshot, 
    getServerInstalledSnapshot
  );

  useEffect(() => {
    if (isInstalled) return;

    const handler = (e) => {
      e.preventDefault();
      setDeferredPrompt(e);
      setIsInstallable(true);
    };

    window.addEventListener('beforeinstallprompt', handler);
    return () => window.removeEventListener('beforeinstallprompt', handler);
  }, [isInstalled]);

  const installApp = async () => {
    if (!deferredPrompt) return;
    deferredPrompt.prompt();
    const { outcome } = await deferredPrompt.userChoice;
    if (outcome === 'accepted') {
      isAppInstalledSession = true;
      notifyListeners();
    }
    setDeferredPrompt(null);
    setIsInstallable(false);
  };

  return { isInstallable, isInstalled, installApp };
}
