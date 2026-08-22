'use client';
import { useState, useCallback, useRef } from 'react';

/**
 * 🔊 Text-to-Speech Hook
 * Tarayıcının yerleşik SpeechSynthesis API'sini kullanır.
 * Hiçbir harici kütüphane gerektirmez.
 */
export function useSpeech() {
  const [isSpeaking, setIsSpeaking] = useState(false);
  const utteranceRef = useRef(null);

  const speak = useCallback((text, lang = 'tr-TR') => {
    if (typeof window === 'undefined' || !window.speechSynthesis) {
      console.warn("Speech Synthesis API not supported.");
      return;
    }

    // Varsa önce durdur
    window.speechSynthesis.cancel();

    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = lang;
    utterance.rate = 0.9; // Biraz yavaş okusun (anlaşılırlık için)
    utterance.pitch = 1.0;

    utterance.onstart = () => setIsSpeaking(true);
    utterance.onend = () => setIsSpeaking(false);
    utterance.onerror = () => setIsSpeaking(false);

    utteranceRef.current = utterance;
    window.speechSynthesis.speak(utterance);
  }, []);

  const stop = useCallback(() => {
    if (typeof window !== 'undefined' && window.speechSynthesis) {
      window.speechSynthesis.cancel();
      setIsSpeaking(false);
    }
  }, []);

  return { speak, stop, isSpeaking };
}
