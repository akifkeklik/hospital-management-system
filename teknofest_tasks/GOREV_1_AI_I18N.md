# Görev 1/5: AI Semptom Analizi — i18n Tamamlama

## Bağlam
Hastane Randevu Sistemi projesi. Frontend: Next.js (App Router), CSS Modules, i18n Context API.
Proje kökü: `c:\Users\MehmetAkif\Desktop\HastaneRandevuSistm`

`SymptomAnalyzer.js` bileşeni zaten yazılmış ve `book-appointment/page.js`'e Step 0 olarak entegre edilmiş durumda. Ama kullandığı çeviri keyleri `locales/index.js`'te **tanımlı değil**. Bu yüzden sayfa açıldığında ham key isimleri (ör. "ai_title") görünüyor.

## Yapılacak İş

### 1. `frontend/src/locales/index.js` Güncelleme

Bu dosya 2848 satırlık dev bir sözlük. İçinde 8 dil var:
- `tr:` → satır 12'de başlar, satır 405'te biter (`"see_all_notifications"` son key)
- `en:` → satır 407'de başlar, satır 797'de biter (`"see_all_notifications"` son key)
- `de:` → satır 799'da başlar
- `fr:` → satır 1194'te başlar
- `es:` → satır 1525'te başlar
- `ru:` → satır 1856'da başlar
- `ar:` → satır 2187'de başlar
- `zh:` → satır 2518'de başlar

Her dil bölümünün **son key'inden sonra** (kapanış `}` den önce), virgül ekleyip aşağıdaki keyleri ekle.

Yani pattern: `"see_all_notifications": "..."` → virgül ekle → yeni keyler.

### Eklenecek 12 Key (SymptomAnalyzer.js'te kullanılıyor):

**TR:**
```
"ai_title": "Akıllı Semptom Analizi",
"ai_subtitle": "Şikayetlerinizi yazın, AI size uygun bölümü önersin.",
"ai_placeholder": "Örn: 3 gündür başım ağrıyor, bulanık görüyorum, mide bulantısı var...",
"ai_analyze_btn": "Analiz Et",
"ai_analyzing": "Yapay zeka semptomlarınızı analiz ediyor",
"ai_min_chars": "Lütfen en az 10 karakter yazın.",
"ai_error_generic": "AI servisi şu an yanıt veremiyor. Lütfen bölümünüzü manuel seçin.",
"ai_suggested_dept": "Önerilen Bölüm",
"ai_confidence": "Güven Oranı",
"ai_book_this_dept": "Bu Bölümden Randevu Al",
"ai_retry": "Tekrar Dene",
"ai_skip": "AI kullanmadan doğrudan bölüm seçin"
```

**EN:**
```
"ai_title": "Smart Symptom Analysis",
"ai_subtitle": "Describe your symptoms and AI will suggest the right department.",
"ai_placeholder": "E.g.: I've had a headache for 3 days, blurred vision, nausea...",
"ai_analyze_btn": "Analyze",
"ai_analyzing": "AI is analyzing your symptoms",
"ai_min_chars": "Please enter at least 10 characters.",
"ai_error_generic": "AI service is currently unavailable. Please select your department manually.",
"ai_suggested_dept": "Suggested Department",
"ai_confidence": "Confidence",
"ai_book_this_dept": "Book from This Department",
"ai_retry": "Try Again",
"ai_skip": "Skip AI and select department directly"
```

**DE:**
```
"ai_title": "Intelligente Symptomanalyse",
"ai_subtitle": "Beschreiben Sie Ihre Symptome und die KI schlägt die richtige Abteilung vor.",
"ai_placeholder": "Z.B.: Ich habe seit 3 Tagen Kopfschmerzen, verschwommenes Sehen...",
"ai_analyze_btn": "Analysieren",
"ai_analyzing": "KI analysiert Ihre Symptome",
"ai_min_chars": "Bitte geben Sie mindestens 10 Zeichen ein.",
"ai_error_generic": "Der KI-Dienst ist derzeit nicht verfügbar. Bitte wählen Sie Ihre Abteilung manuell.",
"ai_suggested_dept": "Vorgeschlagene Abteilung",
"ai_confidence": "Konfidenz",
"ai_book_this_dept": "Termin in dieser Abteilung buchen",
"ai_retry": "Erneut versuchen",
"ai_skip": "KI überspringen und Abteilung direkt wählen"
```

**FR:**
```
"ai_title": "Analyse Intelligente des Symptômes",
"ai_subtitle": "Décrivez vos symptômes et l'IA vous suggérera le bon département.",
"ai_placeholder": "Ex: J'ai mal à la tête depuis 3 jours, vision floue, nausées...",
"ai_analyze_btn": "Analyser",
"ai_analyzing": "L'IA analyse vos symptômes",
"ai_min_chars": "Veuillez saisir au moins 10 caractères.",
"ai_error_generic": "Le service IA est actuellement indisponible. Veuillez sélectionner votre département manuellement.",
"ai_suggested_dept": "Département Suggéré",
"ai_confidence": "Confiance",
"ai_book_this_dept": "Réserver dans ce Département",
"ai_retry": "Réessayer",
"ai_skip": "Passer l'IA et sélectionner directement"
```

**ES:**
```
"ai_title": "Análisis Inteligente de Síntomas",
"ai_subtitle": "Describa sus síntomas y la IA le sugerirá el departamento adecuado.",
"ai_placeholder": "Ej: Tengo dolor de cabeza desde hace 3 días, visión borrosa...",
"ai_analyze_btn": "Analizar",
"ai_analyzing": "La IA está analizando sus síntomas",
"ai_min_chars": "Por favor ingrese al menos 10 caracteres.",
"ai_error_generic": "El servicio de IA no está disponible. Seleccione su departamento manualmente.",
"ai_suggested_dept": "Departamento Sugerido",
"ai_confidence": "Confianza",
"ai_book_this_dept": "Reservar en este Departamento",
"ai_retry": "Intentar de Nuevo",
"ai_skip": "Omitir IA y seleccionar directamente"
```

**RU:**
```
"ai_title": "Интеллектуальный анализ симптомов",
"ai_subtitle": "Опишите свои симптомы, и ИИ предложит подходящий отдел.",
"ai_placeholder": "Напр.: У меня болит голова 3 дня, размытое зрение, тошнота...",
"ai_analyze_btn": "Анализировать",
"ai_analyzing": "ИИ анализирует ваши симптомы",
"ai_min_chars": "Пожалуйста, введите не менее 10 символов.",
"ai_error_generic": "Сервис ИИ временно недоступен. Выберите отдел вручную.",
"ai_suggested_dept": "Рекомендуемый Отдел",
"ai_confidence": "Уверенность",
"ai_book_this_dept": "Записаться в этот Отдел",
"ai_retry": "Попробовать снова",
"ai_skip": "Пропустить ИИ и выбрать напрямую"
```

**AR:**
```
"ai_title": "تحليل الأعراض الذكي",
"ai_subtitle": "صف أعراضك وسيقترح الذكاء الاصطناعي القسم المناسب.",
"ai_placeholder": "مثال: أعاني من صداع منذ 3 أيام، رؤية ضبابية، غثيان...",
"ai_analyze_btn": "تحليل",
"ai_analyzing": "يقوم الذكاء الاصطناعي بتحليل أعراضك",
"ai_min_chars": "الرجاء إدخال 10 أحرف على الأقل.",
"ai_error_generic": "خدمة الذكاء الاصطناعي غير متوفرة حالياً. يرجى اختيار القسم يدوياً.",
"ai_suggested_dept": "القسم المقترح",
"ai_confidence": "نسبة الثقة",
"ai_book_this_dept": "حجز موعد في هذا القسم",
"ai_retry": "حاول مرة أخرى",
"ai_skip": "تخطي الذكاء الاصطناعي والاختيار مباشرة"
```

**ZH:**
```
"ai_title": "智能症状分析",
"ai_subtitle": "描述您的症状，AI将为您推荐合适的科室。",
"ai_placeholder": "例如：我头痛3天了，视力模糊，恶心...",
"ai_analyze_btn": "分析",
"ai_analyzing": "AI正在分析您的症状",
"ai_min_chars": "请至少输入10个字符。",
"ai_error_generic": "AI服务暂时不可用。请手动选择科室。",
"ai_suggested_dept": "建议科室",
"ai_confidence": "置信度",
"ai_book_this_dept": "预约该科室",
"ai_retry": "重试",
"ai_skip": "跳过AI，直接选择科室"
```

## 2. Git Commit

```bash
cd c:\Users\MehmetAkif\Desktop\HastaneRandevuSistm
git add -A
git commit -m "feat(ai): add i18n translations for AI symptom analyzer (8 languages)"
git push origin main
```

## Doğrulama Kriterleri
- [ ] `SymptomAnalyzer.js`'teki tüm `t('ai_...')` çağrıları doğru metin döndürüyor
- [ ] 8 dilin hepsinde 12 AI keyi var
- [ ] Mevcut çeviriler bozulmamış (JSON syntax hatasız)
- [ ] Git push başarılı
