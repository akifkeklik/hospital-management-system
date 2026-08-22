# Görev 3/5: Akıllı Bekleme Süresi & Yoğunluk Tahmini

## Bağlam
Hastane Randevu Sistemi. Backend: Spring Boot 3, JPA. Frontend: Next.js, CSS Modules.
Proje kökü: `c:\Users\MehmetAkif\Desktop\HastaneRandevuSistm`

Mevcut yapı:
- `AppointmentServiceImpl.java` → randevu CRUD işlemleri yapıyor
- `AppointmentController.java` → REST endpoint'ler
- `PatientDashboard.js` → hasta randevu kartları (CSS Module ile)
- `PatientDashboard.module.css` → kartların stilleri

**Amaç:** Her randevu kartında "⏱️ Tahmini Bekleme: 15 dk" ve "🔥 Yoğunluk: Orta" bilgisi göstermek.

## Yapılacak İş

### 1. Backend — Yeni DTO
**`src/main/java/com/hospital/appointmentsystem/appointment/api/WaitTimeDto.java`** — YENİ DOSYA

```java
package com.hospital.appointmentsystem.appointment.api;

public class WaitTimeDto {
    private int estimatedMinutes;
    private String busyLevel; // LOW, MEDIUM, HIGH
    private int queuePosition; // Sıra numarası

    public WaitTimeDto() {}

    public WaitTimeDto(int estimatedMinutes, String busyLevel, int queuePosition) {
        this.estimatedMinutes = estimatedMinutes;
        this.busyLevel = busyLevel;
        this.queuePosition = queuePosition;
    }

    // Getters & Setters
    public int getEstimatedMinutes() { return estimatedMinutes; }
    public void setEstimatedMinutes(int estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; }
    public String getBusyLevel() { return busyLevel; }
    public void setBusyLevel(String busyLevel) { this.busyLevel = busyLevel; }
    public int getQueuePosition() { return queuePosition; }
    public void setQueuePosition(int queuePosition) { this.queuePosition = queuePosition; }
}
```

### 2. Backend — Service Metodu
**`AppointmentServiceImpl.java`** dosyasına yeni metod ekle:

```java
public WaitTimeDto getEstimatedWaitTime(Long appointmentId) {
    Appointment appointment = appointmentRepository.findById(appointmentId)
        .orElseThrow(() -> new RuntimeException("Randevu bulunamadı"));
    
    // Aynı doktorun o günkü randevularını al
    LocalDate appointmentDate = appointment.getAppointmentDate().toLocalDate();
    LocalDateTime dayStart = appointmentDate.atStartOfDay();
    LocalDateTime dayEnd = appointmentDate.atTime(23, 59);
    
    List<Appointment> sameDayAppointments = appointmentRepository
        .findByDoctorIdAndAppointmentDateBetweenAndStatus(
            appointment.getDoctor().getId(), dayStart, dayEnd, "SCHEDULED");
    
    // Randevu saatine göre sırala
    sameDayAppointments.sort(Comparator.comparing(Appointment::getAppointmentDate));
    
    // Bu randevudan önceki randevu sayısını hesapla
    int queuePosition = 0;
    for (Appointment a : sameDayAppointments) {
        if (a.getAppointmentDate().isBefore(appointment.getAppointmentDate())) {
            queuePosition++;
        }
    }
    
    // Ortalama muayene süresi: 15 dakika
    int avgExamMinutes = 15;
    int estimatedMinutes = queuePosition * avgExamMinutes;
    
    // Şu an geçmiş olan süreyi çıkar
    LocalDateTime now = LocalDateTime.now();
    if (now.toLocalDate().equals(appointmentDate) && now.isBefore(appointment.getAppointmentDate())) {
        long minutesUntilAppointment = java.time.Duration.between(now, appointment.getAppointmentDate()).toMinutes();
        estimatedMinutes = (int) Math.max(minutesUntilAppointment, 0);
    }
    
    // Yoğunluk seviyesi
    String busyLevel;
    int totalAppointments = sameDayAppointments.size();
    if (totalAppointments <= 5) busyLevel = "LOW";
    else if (totalAppointments <= 12) busyLevel = "MEDIUM";
    else busyLevel = "HIGH";
    
    return new WaitTimeDto(estimatedMinutes, busyLevel, queuePosition + 1);
}
```

Ayrıca `AppointmentRepository`'ye (eğer yoksa) şu query'yi ekle:
```java
List<Appointment> findByDoctorIdAndAppointmentDateBetweenAndStatus(
    Long doctorId, LocalDateTime start, LocalDateTime end, String status);
```

### 3. Backend — Controller Endpoint
**`AppointmentController.java`**'ya yeni endpoint ekle:

```java
@GetMapping("/{id}/wait-estimate")
public ResponseEntity<WaitTimeDto> getWaitEstimate(@PathVariable Long id) {
    WaitTimeDto waitTime = appointmentService.getEstimatedWaitTime(id);
    return ResponseEntity.ok(waitTime);
}
```

### 4. Frontend — API Service
**`frontend/src/services/api.js`** dosyasında `AppointmentService` objesine ekle:

```javascript
getWaitEstimate: (appointmentId) => fetchAPI(`/appointments/${appointmentId}/wait-estimate`),
```

### 5. Frontend — PatientDashboard.js Güncelleme
Randevu kartlarına bekleme bilgisi badge'i ekle.

**`useEffect` içinde** randevular yüklendikten sonra her biri için wait estimate çek:
```javascript
// fetchData fonksiyonu içinde, randevular set edildikten sonra:
const waitTimes = {};
for (const app of scheduled) {
  try {
    const wt = await AppointmentService.getWaitEstimate(app.id);
    waitTimes[app.id] = wt;
  } catch (e) { /* ignore */ }
}
setWaitTimeMap(waitTimes);
```

**State ekle:**
```javascript
const [waitTimeMap, setWaitTimeMap] = useState({});
```

**Kart içinde badge göster:**
```jsx
{waitTimeMap[app.id] && (
  <div className={styles.waitBadge}>
    <span className={styles[`busy${waitTimeMap[app.id].busyLevel}`]}>
      {waitTimeMap[app.id].busyLevel === 'LOW' ? '🟢' : waitTimeMap[app.id].busyLevel === 'MEDIUM' ? '🟡' : '🔴'}
      {t('busy_level')}: {t(`busy_${waitTimeMap[app.id].busyLevel.toLowerCase()}`)}
    </span>
    <span>⏱️ ~{waitTimeMap[app.id].estimatedMinutes} {t('minutes')}</span>
  </div>
)}
```

### 6. Frontend — CSS (PatientDashboard.module.css)
Yeni stil ekle:

```css
.waitBadge {
  display: flex;
  gap: 1rem;
  align-items: center;
  padding: 0.5rem 0.75rem;
  background: linear-gradient(135deg, rgba(var(--primary-rgb), 0.08), rgba(var(--primary-rgb), 0.03));
  border: 1px solid rgba(var(--primary-rgb), 0.15);
  border-radius: 8px;
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--text-muted);
  margin-top: 0.5rem;
}

.busyLOW { color: var(--success); }
.busyMEDIUM { color: var(--warning); }
.busyHIGH { color: var(--danger); }
```

### 7. i18n Keyleri (TR + EN + diğerleri)
```
"busy_level": "Yoğunluk" / "Busy Level"
"busy_low": "Düşük" / "Low"
"busy_medium": "Orta" / "Medium"
"busy_high": "Yüksek" / "High"
"minutes": "dk" / "min"
"estimated_wait": "Tahmini Bekleme" / "Estimated Wait"
```

## Git Commit
```bash
git add -A
git commit -m "feat(wait-time): add smart wait time estimation with busy level badges"
git push origin main
```

## Doğrulama Kriterleri
- [ ] `GET /api/appointments/{id}/wait-estimate` endpoint'i çalışıyor
- [ ] PatientDashboard'da her randevu kartında yoğunluk badge'i görünüyor
- [ ] Badge renkleri: yeşil (düşük), sarı (orta), kırmızı (yüksek)
- [ ] Backend hata vermeden boş randevularda da çalışıyor
- [ ] Git push başarılı
