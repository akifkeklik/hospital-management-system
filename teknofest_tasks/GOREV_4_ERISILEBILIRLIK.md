# Görev 4/5: Erişilebilirlik (High Contrast + Text-to-Speech)

## Bağlam
Hastane Randevu Sistemi. Frontend: Next.js, Vanilla CSS Modules. 
Proje kökü: `c:\Users\MehmetAkif\Desktop\HastaneRandevuSistm`

Mevcut tema sistemi:
- `globals.css`'de `:root` (dark) ve `[data-theme='light']` tanımlı
- Tema toggle 3 ayrı Header'da var: `Header.js` (admin), `PatientHeader.js`, `DoctorHeader.js`
- Hepsi `document.documentElement.setAttribute('data-theme', ...)` kullanıyor
- Toggle şu an sadece `dark ↔ light` arası geçiş yapıyor

**Amaç:** Görme engelli/zorluk çeken hastalar için yüksek kontrast modu + sesli okuma özelliği.

## Yapılacak İş

### 1. High Contrast Tema — globals.css
**`frontend/src/app/globals.css`** dosyasının SONUNA (responsive media query'lerden ÖNCE) ekle:

```css
/* ── YÜKSEK KONTRAST MODU (Erişilebilirlik) ── */
[data-theme='high-contrast'] {
  --primary: #ffcc00;
  --primary-rgb: 255, 204, 0;
  --primary-hover: #e6b800;
  
  --base-background: #000000;
  --base-surface: #0a0a0a;
  --base-border: #ffffff;
  --base-sidebar: #000000;

  --background: #000000;
  --surface: #0a0a0a;
  --border: #ffffff;
  --sidebar-bg: #000000;

  --text-main: #ffffff;
  --text-muted: #e0e0e0;
  --secondary: #ffcc00;
  --danger: #ff4444;
  --success: #00ff88;
  --warning: #ffcc00;
  --info: #44aaff;

  --surface-rgb: 10, 10, 10;
  --background-rgb: 0, 0, 0;
  --cancel-bg: rgba(255, 68, 68, 0.2);
  --cancel-border: rgba(255, 68, 68, 0.5);

  --shadow-sm: 0 1px 2px 0 rgba(255, 255, 255, 0.1);
  --shadow-md: 0 4px 6px -1px rgba(255, 255, 255, 0.1);
  --shadow-lg: 0 10px 15px -3px rgba(255, 255, 255, 0.1);
  --shadow-xl: 0 20px 25px -5px rgba(255, 255, 255, 0.1);
}
```

### 2. Tema Toggle Güncelleme — 3 Header

Her Header'da (`Header.js`, `PatientHeader.js`, `DoctorHeader.js`) tema toggle mantığını güncelle:

**ESKİ** (dark ↔ light):
```javascript
const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
```

**YENİ** (dark → light → high-contrast → dark döngüsü):
```javascript
const themeOrder = ['dark', 'light', 'high-contrast'];
const currentIndex = themeOrder.indexOf(currentTheme);
const newTheme = themeOrder[(currentIndex + 1) % themeOrder.length];
```

**Toggle butonunun ikonunu da güncelle:**
```jsx
// Mevcut ☀️/🌙 yerine:
{theme === 'dark' ? '🌙' : theme === 'light' ? '☀️' : '👁️'}
```

**Tooltip/title güncelle:**
```jsx
title={theme === 'dark' ? t('light_mode') : theme === 'light' ? t('high_contrast_mode') : t('dark_mode')}
```

### 3. Text-to-Speech Hook — YENİ DOSYA
**`frontend/src/hooks/useSpeech.js`**

```jsx
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
    if (!window.speechSynthesis) return;

    // Önceki konuşmayı durdur
    window.speechSynthesis.cancel();

    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = lang;
    utterance.rate = 0.9;
    utterance.pitch = 1;

    // Türkçe ses varsa onu seç
    const voices = window.speechSynthesis.getVoices();
    const turkishVoice = voices.find(v => v.lang.startsWith('tr'));
    if (turkishVoice) utterance.voice = turkishVoice;

    utterance.onstart = () => setIsSpeaking(true);
    utterance.onend = () => setIsSpeaking(false);
    utterance.onerror = () => setIsSpeaking(false);

    utteranceRef.current = utterance;
    window.speechSynthesis.speak(utterance);
  }, []);

  const stop = useCallback(() => {
    window.speechSynthesis.cancel();
    setIsSpeaking(false);
  }, []);

  return { speak, stop, isSpeaking };
}
```

### 4. Sesli Oku Butonu — SymptomAnalyzer.js Güncelleme
`SymptomAnalyzer.js`'de AI sonuç kartına "🔊 Sesli Oku" butonu ekle.

**Import ekle:**
```jsx
import { useSpeech } from '../hooks/useSpeech';
```

**Hook çağır:**
```jsx
const { speak, stop, isSpeaking } = useSpeech();
```

**Result body'deki `explanation` paragrafından SONRA buton ekle:**
```jsx
<button 
  className={styles.speakBtn}
  onClick={() => {
    if (isSpeaking) {
      stop();
    } else {
      speak(`Önerilen bölüm: ${result.suggestedDepartment}. ${result.explanation}`);
    }
  }}
>
  {isSpeaking ? '⏹️' : '🔊'} {isSpeaking ? t('stop_speaking') : t('read_aloud')}
</button>
```

### 5. CSS — SymptomAnalyzer.module.css'e ekle:
```css
.speakBtn {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.5rem 1rem;
  background: rgba(var(--primary-rgb), 0.1);
  border: 1px solid rgba(var(--primary-rgb), 0.2);
  border-radius: 8px;
  color: var(--primary);
  font-size: 0.85rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.speakBtn:hover {
  background: rgba(var(--primary-rgb), 0.2);
}
```

### 6. Book Appointment Success Sayfasına da Sesli Oku Ekle
**`frontend/src/app/book-appointment/page.js`**'de Step 5 (başarılı) bölümüne:

```jsx
import { useSpeech } from '../../hooks/useSpeech';
// ...
const { speak, stop, isSpeaking } = useSpeech();
// ...
// Başarılı bölümünde:
<button onClick={() => speak(`Randevunuz başarıyla oluşturuldu. ${selectedDept?.name} bölümünde, Doktor ${selectedDoctor?.firstName} ${selectedDoctor?.lastName} ile ${selectedDate} tarihinde saat ${selectedTime} için randevunuz alınmıştır.`)}>
  🔊 {t('read_aloud')}
</button>
```

### 7. ARIA Etiketleri
Ana form elemanlarına `aria-label` ekle:

**Login sayfası (`login/page.js`):**
```jsx
<input aria-label="TC Kimlik Numarası" ... />
<input aria-label="Şifre" ... />
```

**Book appointment (`book-appointment/page.js`):**
```jsx
<input type="date" aria-label="Randevu tarihi seçin" ... />
<textarea aria-label="Doktora notunuz" ... />
```

**SymptomAnalyzer.js:**
```jsx
<textarea aria-label="Şikayetlerinizi yazın" ... />
```

### 8. i18n Keyleri (Her 8 dile)
```
"high_contrast_mode": "Yüksek Kontrast" / "High Contrast"
"read_aloud": "Sesli Oku" / "Read Aloud"
"stop_speaking": "Durdur" / "Stop"
"accessibility": "Erişilebilirlik" / "Accessibility"
```

## Git Commit
```bash
git add -A
git commit -m "feat(a11y): add high contrast theme, text-to-speech, and ARIA labels"
git push origin main
```

## Doğrulama Kriterleri
- [ ] Tema toggle: dark → light → high-contrast → dark döngüsü çalışıyor
- [ ] High contrast modda siyah zemin, beyaz kenarlıklar, sarı vurgu renkleri
- [ ] 🔊 Sesli Oku butonu AI sonucunu Türkçe okuyor
- [ ] ARIA etiketleri eklenmiş (DevTools'ta kontrol)
- [ ] 3 Header'da da tema toggle tutarlı çalışıyor
- [ ] Git push başarılı
