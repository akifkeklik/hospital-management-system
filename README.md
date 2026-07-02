# Hastane Randevu ve Yönetim Sistemi (HBYS) 🏥

Modern, modüler ve güvenli bir hastane bilgi yönetim sistemi. Clean Architecture prensiplerine ve mikroservis standartlarına uygun olarak, doktor, hasta ve yönetim süreçlerini tek bir merkezde otomatize etmek için geliştirilmiştir.

## 🌟 Temel Özellikler

- **Poliklinik ve Randevu Yönetimi:** Takvim bazlı gelişmiş randevu alma, iptal etme ve poliklinik yönetimi.
- **Akıllı Doktor İzin Otomasyonu:** Doktor izin talepleri onaylandığında, o tarihteki randevular otomatik iptal edilir ve hastalara anında bildirim gider.
- **Muayene ve Tıbbi Kayıtlar:** Doktorlar için hasta sırası ekranı; ICD-10 teşhis ve e-reçete modülü. Hastalar (E-Nabız tarzı) geçmiş kayıtlarına erişebilir.
- **Dinamik Bildirim Sistemi:** Kurumsal bildirim ekranı ile hastalar anlık duyuru ve randevu bildirimlerini takip eder.

## 🛠️ Teknoloji Yığını

### Backend
- **Java 17 & Spring Boot 3.x**
- **Spring Data JPA & Hibernate:** `FetchType.LAZY` optimizasyonlu, ilişkisel veritabanı modeli.
- **Spring Security & JWT:** Rol bazlı yetkilendirme (Admin, Doktor, Hasta).
- **H2 Database (In-Memory):** Test ve geliştirme için anında hazır veritabanı.

### Frontend
- **Next.js & React:** Hızlı ve SEO dostu modern kullanıcı arayüzü.
- **Vanilla CSS:** Bağımlılık olmadan, hafif ve tam özelleştirilmiş şık arayüz (Gece/Gündüz modu).
- **Context API:** Global state yönetimi (Tema, Dil, Kimlik Doğrulama).

## 🚀 Hızlı Başlangıç

### 1. Backend'i Başlatın
```bash
# Projeyi derleyin
mvn clean package -DskipTests

# Sunucuyu başlatın (Varsayılan port: 8080)
java -jar target/appointment-system-0.0.1-SNAPSHOT.jar
```

### 2. Frontend'i Başlatın
```bash
cd frontend

# Bağımlılıkları yükleyin
npm install

# İstemciyi başlatın (Varsayılan port: 3000)
npm run dev
```

## 🔒 Güvenlik Notları
Kimlik doğrulama süreçlerinde JWT token tabanlı `AuthService` kullanılmış olup, kullanıcı id verileri frontend tarafında açıkta bırakılmamıştır. Tüm endpointler `PreAuthorize` rolleriyle korunmaktadır.

---
*Clean Code, YAGNI ve Senior Software Engineering pratikleri gözetilerek kodlanmıştır.*
