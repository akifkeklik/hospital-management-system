'use client';
import { useState, useEffect } from 'react';
import { useSettings } from '../context/SettingsContext';
import styles from './HospitalMap.module.css';

/**
 * 🗺️ Hastane İçi İnteraktif Navigasyon Haritası
 * SVG tabanlı, bölüm bazlı vurgulama ve animasyonlu pin ile.
 */

// Bölüm koordinatları (temsili hastane planı)
const DEPARTMENT_MAP = {
  'dahiliye':      { x: 120, y: 180, floor: 1, block: 'A', room: '101' },
  'kardiyoloji':   { x: 320, y: 180, floor: 1, block: 'A', room: '102' },
  'nöroloji':      { x: 520, y: 180, floor: 2, block: 'A', room: '201' },
  'ortopedi':      { x: 120, y: 340, floor: 2, block: 'B', room: '202' },
  'göz':           { x: 320, y: 340, floor: 1, block: 'B', room: '103' },
  'dermatoloji':   { x: 520, y: 340, floor: 1, block: 'B', room: '104' },
  'kbb':           { x: 120, y: 500, floor: 3, block: 'A', room: '301' },
  'üroloji':       { x: 320, y: 500, floor: 3, block: 'A', room: '302' },
  'genel cerrahi': { x: 520, y: 500, floor: 3, block: 'B', room: '303' },
};

export default function HospitalMap({ departmentName, isOpen, onClose }) {
  const { t } = useSettings();
  const [isAnimated, setIsAnimated] = useState(false);

  // Bölüm adını normalize et ve eşleştir
  const normalizedName = departmentName?.toLowerCase()?.trim() || '';
  const matchedKey = Object.keys(DEPARTMENT_MAP).find(key => 
    normalizedName.includes(key) || key.includes(normalizedName)
  );
  const target = matchedKey ? DEPARTMENT_MAP[matchedKey] : null;

  useEffect(() => {
    if (isOpen) {
      const timer = setTimeout(() => setIsAnimated(true), 100);
      return () => {
        clearTimeout(timer);
        setIsAnimated(false);
      };
    }
  }, [isOpen]);

  if (!isOpen) return null;

  return (
    <div className={styles.overlay} onClick={onClose}>
      <div className={styles.modal} onClick={e => e.stopPropagation()}>
        {/* Header */}
        <div className={styles.header}>
          <div>
            <h2 className={styles.title}>🗺️ {t('hospital_map')}</h2>
            {target && (
              <p className={styles.subtitle}>
                📍 {departmentName} — {target.block} {t('block')}, {t('floor')} {target.floor}, {t('room')} {target.room}
              </p>
            )}
          </div>
          <button className={styles.closeBtn} onClick={onClose}>✕</button>
        </div>

        {/* SVG Harita */}
        <div className={styles.mapContainer}>
          <svg viewBox="0 0 700 620" className={`${styles.map} ${isAnimated ? styles.animated : ''}`}>
            {/* Arka plan */}
            <rect x="0" y="0" width="700" height="620" rx="16" fill="var(--background)" stroke="var(--border)" strokeWidth="2" />
            
            {/* Başlık */}
            <text x="350" y="40" textAnchor="middle" fill="var(--text-main)" fontSize="18" fontWeight="700">
              {t('hospital_floor_plan')}
            </text>

            {/* A Blok etiketi */}
            <text x="350" y="130" textAnchor="middle" fill="var(--text-muted)" fontSize="12" fontWeight="600">
              ── A BLOK ──
            </text>

            {/* B Blok etiketi */}
            <text x="350" y="290" textAnchor="middle" fill="var(--text-muted)" fontSize="12" fontWeight="600">
              ── B BLOK ──
            </text>
            
            {/* Koridor çizgileri */}
            <line x1="50" y1="260" x2="650" y2="260" stroke="var(--border)" strokeWidth="1" strokeDasharray="8,4" />
            <line x1="50" y1="420" x2="650" y2="420" stroke="var(--border)" strokeWidth="1" strokeDasharray="8,4" />

            {/* Bölüm odaları */}
            {Object.entries(DEPARTMENT_MAP).map(([key, pos]) => {
              const isTarget = key === matchedKey;
              return (
                <g key={key}>
                  {/* Oda dikdörtgeni */}
                  <rect
                    x={pos.x - 60} y={pos.y - 35}
                    width="140" height="70"
                    rx="10"
                    fill={isTarget ? 'rgba(var(--primary-rgb), 0.2)' : 'var(--surface)'}
                    stroke={isTarget ? 'var(--primary)' : 'var(--border)'}
                    strokeWidth={isTarget ? 3 : 1}
                    className={isTarget ? styles.targetRoom : ''}
                  />
                  {/* Bölüm adı */}
                  <text
                    x={pos.x + 10} y={pos.y - 5}
                    textAnchor="middle"
                    fill={isTarget ? 'var(--primary)' : 'var(--text-main)'}
                    fontSize="11"
                    fontWeight={isTarget ? '700' : '500'}
                  >
                    {key.charAt(0).toUpperCase() + key.slice(1)}
                  </text>
                  {/* Oda numarası */}
                  <text
                    x={pos.x + 10} y={pos.y + 15}
                    textAnchor="middle"
                    fill="var(--text-muted)"
                    fontSize="9"
                  >
                    {pos.block} Blok - Oda {pos.room}
                  </text>
                </g>
              );
            })}

            {/* Hedef bölüm üzerinde animasyonlu pin */}
            {target && isAnimated && (
              <g className={styles.pinBounce}>
                <circle cx={target.x + 10} cy={target.y - 50} r="14" fill="var(--danger)" opacity="0.9" />
                <text x={target.x + 10} y={target.y - 45} textAnchor="middle" fill="white" fontSize="14">📍</text>
                {/* Pin çizgisi */}
                <line x1={target.x + 10} y1={target.y - 36} x2={target.x + 10} y2={target.y - 35} stroke="var(--danger)" strokeWidth="2" />
              </g>
            )}

            {/* Giriş */}
            <g>
              <rect x="290" y="570" width="120" height="35" rx="8" fill="var(--success)" opacity="0.2" stroke="var(--success)" />
              <text x="350" y="593" textAnchor="middle" fill="var(--success)" fontSize="12" fontWeight="700">
                🚪 {t('entrance')}
              </text>
            </g>
          </svg>
        </div>

        {/* Alt bilgi */}
        {!target && (
          <div className={styles.notFound}>
            ⚠️ {t('dept_not_on_map')}
          </div>
        )}
      </div>
    </div>
  );
}
