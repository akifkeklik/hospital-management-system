# Görev 2/5: PWA (Progressive Web App) Entegrasyonu

## Bağlam
Hastane Randevu Sistemi. Frontend: Next.js 16, App Router. Proje kökü: `c:\Users\MehmetAkif\Desktop\HastaneRandevuSistm`

Mevcut `next.config.mjs` boş (sadece `const nextConfig = {};`). `package.json`'da `next: "16.2.9"` kullanılıyor.

**Amaç:** Uygulamayı telefona kurulabilir (installable) hale getirmek. TEKNOFEST'te "Mobil uygulama desteği" diyebilmek için.

## Yapılacak İş

### 1. `frontend/public/manifest.json` — YENİ DOSYA
```json
{
  "name": "Akıllı Hastane Bilgi Yönetim Sistemi",
  "short_name": "HBYS",
  "description": "Yapay zeka destekli hastane randevu ve bilgi yönetim sistemi",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#090c10",
  "theme_color": "#818cf8",
  "orientation": "portrait-primary",
  "icons": [
    {
      "src": "/icons/icon-192x192.png",
      "sizes": "192x192",
      "type": "image/png",
      "purpose": "any maskable"
    },
    {
      "src": "/icons/icon-512x512.png",
      "sizes": "512x512",
      "type": "image/png",
      "purpose": "any maskable"
    }
  ]
}
```

### 2. İkon Dosyaları Oluşturma
`frontend/public/icons/` dizini oluştur. İçine SVG tabanlı basit ikon dosyaları yarat.

**`frontend/public/icons/icon-192x192.svg`** → Sonra PNG'ye dönüştürülecek.

Basit yaklaşım: Mevcut `favicon.ico`'yu kopyala ve yeniden adlandır. Önemli olan `manifest.json`'da ikon tanımlı olması.

```bash
mkdir -p frontend/public/icons
# Basit placeholder PNG (1x1 indigo pixel — sonra gerçek logo ile değiştirilir)
cp frontend/public/favicon.ico frontend/public/icons/icon-192x192.png
cp frontend/public/favicon.ico frontend/public/icons/icon-512x512.png
```

### 3. `frontend/src/app/layout.js` — META TAG EKLEMESİ
Mevcut `<head>` bölümüne şu meta tag'leri ekle:

```jsx
<link rel="manifest" href="/manifest.json" />
<meta name="theme-color" content="#818cf8" />
<meta name="apple-mobile-web-app-capable" content="yes" />
<meta name="apple-mobile-web-app-status-bar-style" content="black-translucent" />
<meta name="apple-mobile-web-app-title" content="HBYS" />
<link rel="apple-touch-icon" href="/icons/icon-192x192.png" />
```

### 4. PWA Install Prompt Hook — YENİ DOSYA
**`frontend/src/hooks/usePwaInstall.js`**

```jsx
'use client';
import { useState, useEffect } from 'react';

export function usePwaInstall() {
  const [deferredPrompt, setDeferredPrompt] = useState(null);
  const [isInstallable, setIsInstallable] = useState(false);
  const [isInstalled, setIsInstalled] = useState(false);

  useEffect(() => {
    // PWA zaten kurulu mu kontrol et
    if (window.matchMedia('(display-mode: standalone)').matches) {
      setIsInstalled(true);
      return;
    }

    const handler = (e) => {
      e.preventDefault();
      setDeferredPrompt(e);
      setIsInstallable(true);
    };

    window.addEventListener('beforeinstallprompt', handler);
    
    window.addEventListener('appinstalled', () => {
      setIsInstalled(true);
      setIsInstallable(false);
      setDeferredPrompt(null);
    });

    return () => window.removeEventListener('beforeinstallprompt', handler);
  }, []);

  const installApp = async () => {
    if (!deferredPrompt) return;
    deferredPrompt.prompt();
    const { outcome } = await deferredPrompt.userChoice;
    if (outcome === 'accepted') {
      setIsInstalled(true);
    }
    setDeferredPrompt(null);
    setIsInstallable(false);
  };

  return { isInstallable, isInstalled, installApp };
}
```

### 5. Header'lara "Uygulamayı Kur" Butonu Ekleme

3 ayrı Header var: `Header.js`, `PatientHeader.js`, `DoctorHeader.js`. Her birine:

**Import ekle:**
```jsx
import { usePwaInstall } from '../hooks/usePwaInstall';
```

**Fonksiyon içinde hook çağır:**
```jsx
const { isInstallable, installApp } = usePwaInstall();
```

**Actions bölümüne (tema toggle'ından önce) buton ekle:**
```jsx
{isInstallable && (
  <button onClick={installApp} className={styles.installBtn} title="Uygulamayı Kur">
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
      <polyline points="7 10 12 15 17 10"></polyline>
      <line x1="12" y1="15" x2="12" y2="3"></line>
    </svg>
  </button>
)}
```

**Her Header'ın CSS Module dosyasına `.installBtn` stili ekle:**
```css
.installBtn {
  background: linear-gradient(135deg, #8b5cf6, #6366f1);
  border: none;
  color: white;
  padding: 0.5rem;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(139, 92, 246, 0.3);
}

.installBtn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.4);
}
```

### 6. i18n Keyleri (Her 8 dile ekle)
Önceki görevdeki pattern ile aynı şekilde:
- `"pwa_install": "Uygulamayı Kur"` (TR)
- `"pwa_install": "Install App"` (EN)
- vb.

## Git Commit
```bash
git add -A
git commit -m "feat(pwa): add Progressive Web App support with install prompt"
git push origin main
```

## Doğrulama Kriterleri
- [ ] `manifest.json` erişilebilir: `http://localhost:3000/manifest.json`
- [ ] Chrome DevTools → Application → Manifest hatasız görünüyor
- [ ] Mobil Chrome'da "Ana Ekrana Ekle" seçeneği çıkıyor
- [ ] Header'da install butonu görünüyor (sadece destekleyen tarayıcılarda)
- [ ] Git push başarılı
