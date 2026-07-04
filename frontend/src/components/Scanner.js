'use client';
import { useState, useEffect, useRef } from 'react';
import Tesseract from 'tesseract.js';
import styles from './Scanner.module.css';
import { useSettings } from '../context/SettingsContext';

export default function Scanner({ onScan }) {
  const { t } = useSettings();
  const [mode, setMode] = useState('keyboard'); // 'keyboard' | 'camera'
  const [isScanning, setIsScanning] = useState(false);
  const [scanStatus, setScanStatus] = useState('');
  const videoRef = useRef(null);
  const canvasRef = useRef(null);
  const streamRef = useRef(null);
  const intervalRef = useRef(null);
  
  // Barcode / Keyboard Listener
  useEffect(() => {
    let keyBuffer = '';
    let timeoutId = null;

    const handleKeyDown = (e) => {
      if (mode !== 'keyboard') return;
      // Ignore if user is typing in an input field (unless we force it, but let's be safe)
      if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') return;

      if (e.key === 'Enter') {
        if (keyBuffer.length === 11 && /^\d+$/.test(keyBuffer)) {
          onScan(keyBuffer);
          setScanStatus('Okundu: ' + keyBuffer);
          setTimeout(() => setScanStatus(''), 3000);
        }
        keyBuffer = '';
        return;
      }

      // Barcode scanners type very fast. If delay > 50ms, it's probably a human.
      if (timeoutId) clearTimeout(timeoutId);
      keyBuffer += e.key;

      timeoutId = setTimeout(() => {
        keyBuffer = '';
      }, 50);
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [mode, onScan]);

  // Camera Logic
  const startCamera = async () => {
    setMode('camera');
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ video: { facingMode: 'environment' } });
      if (videoRef.current) {
        videoRef.current.srcObject = stream;
        streamRef.current = stream;
      }
      setIsScanning(true);
      setScanStatus('Kamera başlatıldı. Kimliği okutun...');
      startOCR();
    } catch (err) {
      console.error(err);
      setScanStatus('Kamera açılamadı!');
      setMode('keyboard');
    }
  };

  const stopCamera = () => {
    if (streamRef.current) {
      streamRef.current.getTracks().forEach(track => track.stop());
    }
    if (intervalRef.current) clearInterval(intervalRef.current);
    setIsScanning(false);
    setMode('keyboard');
    setScanStatus('');
  };

  const startOCR = () => {
    intervalRef.current = setInterval(async () => {
      if (!videoRef.current || !canvasRef.current) return;
      const video = videoRef.current;
      const canvas = canvasRef.current;
      if (video.videoWidth === 0) return;

      canvas.width = video.videoWidth;
      canvas.height = video.videoHeight;
      const ctx = canvas.getContext('2d');
      ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
      
      const imageData = canvas.toDataURL('image/png');
      try {
        setScanStatus('Taranıyor...');
        const result = await Tesseract.recognize(imageData, 'eng');
        
        const text = result.data.text;
        // Regex for 11 digit TC
        const tcRegex = /\b[1-9][0-9]{10}\b/g;
        const matches = text.match(tcRegex);
        if (matches && matches.length > 0) {
          const tc = matches[0];
          onScan(tc);
          stopCamera();
          setScanStatus('TC Bulundu: ' + tc);
        } else {
          setScanStatus('Bulunamadı, lütfen kimliği yaklaştırın.');
        }
      } catch (err) {
        console.error(err);
      }
    }, 2000); // Check every 2 seconds
  };

  // cleanup
  useEffect(() => {
    return () => {
      if (intervalRef.current) clearInterval(intervalRef.current);
      if (streamRef.current) streamRef.current.getTracks().forEach(track => track.stop());
    };
  }, []);

  return (
    <div className={styles.scannerContainer}>
      <div className={styles.header}>
        <h3>
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path><line x1="8" y1="10" x2="16" y2="10"></line><line x1="8" y1="14" x2="16" y2="14"></line></svg>
          {t('scanner_title')}
        </h3>
        {mode === 'keyboard' ? (
          <button onClick={startCamera} className={styles.startBtn}>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"></path><circle cx="12" cy="13" r="4"></circle></svg>
            {t('open_camera')}
          </button>
        ) : (
          <button onClick={stopCamera} className={styles.stopBtn}>{t('close_camera')}</button>
        )}
      </div>
      
      <div className={styles.content}>
        {mode === 'camera' && (
          <div className={styles.videoWrapper}>
            <video ref={videoRef} autoPlay playsInline muted className={styles.video}></video>
            <canvas ref={canvasRef} style={{ display: 'none' }}></canvas>
            <div className={styles.scanOverlay}></div>
          </div>
        )}
        <div className={styles.statusBox}>
          {scanStatus || (mode === 'keyboard' ? (t('scanner_placeholder')) : (t('camera_ready')))}
        </div>
      </div>
    </div>
  );
}
