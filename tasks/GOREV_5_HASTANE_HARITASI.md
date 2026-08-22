# Görev 5/5: Hastane İçi İnteraktif Navigasyon (SVG Harita)

## Bağlam
Hastane Randevu Sistemi. Frontend: Next.js, CSS Modules.
Proje kökü: `c:\Users\MehmetAkif\Desktop\HastaneRandevuSistm`

**Amaç:** Randevu aldıktan sonra polikliniğin hastane içinde nerede olduğunu gösteren animasyonlu bir SVG harita. TEKNOFEST sunumunda jüriyi görsel olarak etkiler.

**NOT:** Bu tamamen frontend özelliği. Backend'e hiç dokunulmayacak.

## Yapılacak İş

### 1. HospitalMap Bileşeni — YENİ DOSYA
**`frontend/src/components/HospitalMap.js`**

```jsx
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
      setTimeout(() => setIsAnimated(true), 100);
    } else {
      setIsAnimated(false);
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
```

### 2. HospitalMap CSS — YENİ DOSYA
**`frontend/src/components/HospitalMap.module.css`**

```css
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.2s ease;
  padding: 1rem;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 20px;
  width: 100%;
  max-width: 800px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: var(--shadow-xl);
  animation: slideUp 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 1.5rem 2rem;
  border-bottom: 1px solid var(--border);
}

.title {
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--text-main);
  margin: 0;
}

.subtitle {
  font-size: 0.9rem;
  color: var(--primary);
  font-weight: 600;
  margin: 0.25rem 0 0 0;
}

.closeBtn {
  background: var(--background);
  border: 1px solid var(--border);
  color: var(--text-muted);
  width: 36px;
  height: 36px;
  border-radius: 50%;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.closeBtn:hover {
  background: var(--danger);
  color: white;
  border-color: var(--danger);
}

.mapContainer {
  padding: 1.5rem 2rem;
}

.map {
  width: 100%;
  height: auto;
}

.targetRoom {
  animation: glow 1.5s ease-in-out infinite;
}

@keyframes glow {
  0%, 100% { filter: drop-shadow(0 0 4px rgba(var(--primary-rgb), 0.3)); }
  50% { filter: drop-shadow(0 0 12px rgba(var(--primary-rgb), 0.6)); }
}

.pinBounce {
  animation: bounce 0.8s cubic-bezier(0.36, 0.07, 0.19, 0.97) infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

.notFound {
  padding: 1rem 2rem 1.5rem;
  color: var(--warning);
  font-size: 0.9rem;
  text-align: center;
}

@media (max-width: 768px) {
  .modal { border-radius: 16px; }
  .header { padding: 1rem 1.25rem; }
  .mapContainer { padding: 1rem; }
  .title { font-size: 1rem; }
}
```

### 3. PatientDashboard Entegrasyonu
**`PatientDashboard.js`**'de her randevu kartına "📍 Haritada Gör" butonu ekle:

**Import:**
```jsx
import HospitalMap from './HospitalMap';
```

**State:**
```jsx
const [mapDept, setMapDept] = useState(null);
```

**Kart içine buton:**
```jsx
<button onClick={() => setMapDept(app.departmentName)} className={styles.mapBtn}>
  📍 {t('view_on_map')}
</button>
```

**Component sonuna HospitalMap modalı:**
```jsx
<HospitalMap
  departmentName={mapDept}
  isOpen={!!mapDept}
  onClose={() => setMapDept(null)}
/>
```

### 4. Book Appointment Başarı Ekranı Entegrasyonu
**`book-appointment/page.js`**'de Step 5'e "Haritada Gör" butonu ekle:

```jsx
import HospitalMap from '../../components/HospitalMap';
// State:
const [showMap, setShowMap] = useState(false);
// Başarı ekranında:
<button onClick={() => setShowMap(true)} className={styles.mapViewBtn}>
  📍 {t('view_on_map')}
</button>
<HospitalMap departmentName={selectedDept?.name} isOpen={showMap} onClose={() => setShowMap(false)} />
```

### 5. CSS Buton Stilleri
**`PatientDashboard.module.css`**'e ekle:
```css
.mapBtn {
  background: none;
  border: 1px solid var(--border);
  color: var(--text-muted);
  padding: 0.3rem 0.6rem;
  border-radius: 6px;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.2s;
}

.mapBtn:hover {
  border-color: var(--primary);
  color: var(--primary);
  background: rgba(var(--primary-rgb), 0.05);
}
```

### 6. i18n Keyleri (Her 8 dile)
```
"hospital_map": "Hastane Haritası" / "Hospital Map"
"hospital_floor_plan": "Hastane Kat Planı" / "Hospital Floor Plan"
"block": "Blok" / "Block"
"floor": "Kat" / "Floor"
"room": "Oda" / "Room"
"entrance": "GİRİŞ" / "ENTRANCE"
"view_on_map": "Haritada Gör" / "View on Map"
"dept_not_on_map": "Bu bölüm haritada tanımlı değil." / "This department is not on the map."
```

## Git Commit
```bash
git add -A
git commit -m "feat(navigation): add interactive SVG hospital map with animated department pins"
git push origin main
```

## Doğrulama Kriterleri
- [ ] "Haritada Gör" butonuna tıklanınca SVG harita modal olarak açılıyor
- [ ] Hedef bölüm vurgulanmış (kenarlık + glow animasyonu)
- [ ] Pin (📍) hedef üzerinde bounce animasyonu yapıyor
- [ ] Modal backdrop'a tıklanınca kapanıyor
- [ ] Haritada olmayan bölüm için uyarı mesajı görünüyor
- [ ] Mobile responsive çalışıyor
- [ ] Git push başarılı
