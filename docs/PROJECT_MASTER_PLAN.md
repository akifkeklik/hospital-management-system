# Hospital Appointment System - Project Master Plan

Bu doküman, Hospital Appointment System projesinin geçmişte tamamlanan aşamalarını, mevcut mimarisini, teknik borçlarını ve gelecekteki teknik / yapay zeka (AI) yol haritasını özetleyen **Tek Kaynak (Single Source of Truth)** belgesidir. 

---

## 1. Project Vision
Projenin temel amacı, modern yazılım mimarisi prensipleriyle geliştirilmiş, sağlam, ölçeklenebilir ve yapay zeka destekli bir hastane yönetim ve randevu sistemi oluşturmaktır. Gelecekteki AI özellikleri için güçlü bir teknik temel (foundation) atmayı hedefler. Proje, yoğun sağlık sistemlerindeki bekleme sürelerini optimize etmeyi, hasta ve doktor deneyimini iyileştirmeyi vizyon edinir.

---

## 2. Current Architecture
Mevcut sistem, sağlam temeller üzerine oturtulmuş modern bir Client-Server mimarisine dayanmaktadır.

*   **Backend:** Spring Boot, JPA/Hibernate, Spring Security (JWT tabanlı stateless auth).
*   **Frontend:** Next.js (App Router, tamamen Client-Side rendering ağırlıklı mimari), Vanilla CSS Modülleri.
*   **Database:** MySQL (JDBC driver `com.mysql.cj.jdbc.Driver`).
*   **Authentication Flow:** JWT tabanlı, localStorage/sessionStorage'da saklanan token üzerinden çalışan hybrid `AuthContext + ClientLayout` mekanizması. (Middleware tabanlı bir yapı şu anda kullanılmamaktadır, istemci tarafı state kontrolü uygulanmaktadır).

---

## 3. Completed Phases
Projenin bugüne kadar tamamlanan aşamaları (F1 - F4) kesinleşmiş gerçek geliştirmelere dayanmaktadır:

*   **Phase F1 (Initial MVP):** 
    Veritabanı modellemesi, temel CRUD operasyonları (Randevu, Hasta, Doktor, Bölüm vb.), Spring Security entegrasyonu ve temel JWT kimlik doğrulaması.
*   **Phase F2 (Frontend Request Safety):** 
    Frontend API isteklerinin güvenliği artırıldı, `AbortController` ile component unmount durumlarında oluşan memory leak sorunları önlendi. Backend tarafında N+1 sorgu problemleri `EntityGraph` ile ve Double Booking (aynı saate iki randevu) concurrency problemi `Unique Constraint + 409 Conflict` ile çözüldü.
*   **Phase F3 (API Standardization):** 
    Frontend tarafında `useApi` Custom Hook implementasyonu yapılarak GET isteklerinin, loading statelerinin ve error handling mekanizmalarının standartlaştırılması sağlandı.
*   **Phase F4 (Authentication State Modernization):** 
    Merkezi bir `AuthContext` (Provider) implementasyonu yapıldı. İstemci tarafında yaşanan N+1 user data fetch problemi (farklı componentlerin aynı anda `getMe` çağırması) çözüldü. Initial render'da oluşan UI flickering engellenerek global ve güvenli bir oturum yönetimi sağlandı.

---

## 4. Current Technical Debt
Şu an sistemde bulunan ve çözülmesi gereken (veya gelecekte planlanacak olan) teknik borçlar:

*   **Component Duplication:** `PatientHeader`, `DoctorHeader`, ve `Header` gibi ana UI bileşenlerinde ciddi oranlarda yapısal kod tekrarları bulunmaktadır.
*   **Mutation Lifecycle Safety:** Frontend tarafındaki mutasyon işlemlerinde (POST/PUT/DELETE) henüz merkezi bir lifecycle yönetimi bulunmamaktadır. Şu anda native `fetch`/`try-catch` blokları component içlerine dağılmış durumdadır.
*   **Testing:** Gerek backend gerekse frontend tarafında kapsamlı Unit/Integration veya E2E test suitleri henüz entegre edilmemiştir.

---

## 5. Remaining Foundation Work
F5 ve F10 yol haritasına geçilmeden veya AI entegrasyonlarına başlanmadan önce tamamlanması kritik olan foundational görevler:

*   Tekrarlayan layout ve navigasyon componentlerinin (`MainHeader`, `BaseLayout` vb.) soyutlanıp tek bir sistemde birleştirilmesi (Deduplication).
*   Frontend Mutation isteklerinin güvenlik ve loading lifecycle'larını standartlaştıracak bir `useMutation` hook'unun geliştirilmesi.

---

## 6. Proposed Technical Roadmap (F5-F10 Candidate Phases)
*Bu fazlar kesin bir uygulama sözü (guarantee) içermez. Mevcut vizyona göre önceliklendirilmiş aday (candidate) teknik iyileştirmelerdir.*

*   **Phase F5 (Layout & Component Cleanup):** UI component deduplication ve layout basitleştirme. *(Dependency: F4)*
*   **Phase F6 (Mutation Lifecycle Safety):** `useMutation` hook implementasyonu ve tüm state/mutate işlemlerinin refaktörizasyonu. *(Dependency: F3, F5)*
*   **Phase F7 (Global Error Boundaries):** İstemci tarafında React Error Boundaries ile unexpected crash raporlama (ileride Sentry gibi araçlar eklenebilir).
*   **Phase F8 (PWA Enhancements):** Gelişmiş çevrimdışı (offline) kullanım ve service worker iyileştirmeleri.
*   **Phase F9 (Real-time Capabilities):** Randevu durum değişiklikleri, çağrı ekranı güncellemeleri için WebSockets veya SSE (Server-Sent Events) altyapısı.
*   **Phase F10 (Advanced State Management):** Projenin ölçeğinin gerektirdiği noktada (Sadece ihtiyaç olursa) dış kütüphanelerin (SWR, Redux vb.) veya cache mekanizmalarının sisteme entegrasyonu.

---

## 7. Testing Roadmap
*   **Unit Testing:** Backend'de JUnit5/Mockito; Frontend'de Jest ve React Testing Library.
*   **Integration Testing:** Veritabanı ve endpoint testleri için Testcontainers / Spring Boot Test.
*   **End-to-End (E2E) Testing:** Kullanıcı akışlarının doğrulanması için Cypress veya Playwright entegrasyonu.

---

## 8. Security Roadmap
*   CORS ve CSRF yapılandırmalarının production standartlarında sıkılaştırılması.
*   API uç noktalarında Rate Limiting & kaba kuvvet (Brute Force) korumalarının devreye alınması (Şu an properties'de mevcut, kapsamı genişletilecek).
*   Daha granüler bir RBAC (Role-Based Access Control) denetimi.

---

## 9. Deployment Roadmap
*   Docker & Docker Compose entegrasyonu ile containerized bir yapı.
*   GitHub Actions veya GitLab CI ile sürekli entegrasyon ve dağıtım (CI/CD) süreçlerinin otomasyonu.
*   Environment Variable bazlı multi-environment (Dev, Staging, Prod) desteği.

---

## 10. Performance Roadmap
*   **Backend:** Veritabanı query optimizasyonları, kritik okuma yollarında (Read-Heavy) Redis veya benzeri Caching çözümleri.
*   **Frontend:** Code splitting, lazy loading, gereksiz re-render'ların (memoization ile) önlenmesi.
*   **Image/Asset Optimization:** Profil fotoları veya dökümanlar için Next.js Image bileşeni adaptasyonu.

---

## 11. Documentation Roadmap
*   **API Referansları:** Backend API endpointlerinin belgelendirilmesi için Swagger/OpenAPI entegrasyonu.
*   **Kullanıcı Kılavuzları:** Hasta, Doktor ve Admin modüllerinin nasıl kullanılacağına dair dijital dokümanlar.

---

## 12. Future Product Features (FEATURE CANDIDATES)
Gelecekte sisteme entegre edilebilecek klinik ürün geliştirmeleri:
*   Tele-sağlık (Video Görüşme) modülü.
*   Online Ödeme ve Faturalandırma sistemi.
*   Gelişmiş Laboratuvar ve Görüntüleme (PACS) sonuç görüntüleme modülü.

---

## 13. Future Intelligent/AI Features (FEATURE CANDIDATES)

**Önemli Klinik Güvenlik Prensibi:** 
*Sistem hiçbir şekilde bağımsız tıbbi teşhis koymak veya tedavi amaçlı klinik karar vermek (Diagnosis/Treatment) için tasarlanmamalıdır. Tüm AI özellikleri "Karar Destek Sistemi (Decision Support System - DSS)" ve "Klinik Asistan" yaklaşımıyla geliştirilmelidir. Son değerlendirme ve tıbbi sorumluluk DAİMA insan hekime aittir.*

### 13.1 Intelligent Appointment Scheduling
*   **Priority:** High
*   **Amaç:** Hastanın semptomlarına göre onu doğru branşa ve doğru alt-uzmanlığa sahip doktora yönlendirmek.
*   **Kullanıcı:** Hasta
*   **Gerekli Veri:** Hastanın serbest metin şikayeti, doktorların uzmanlık/keywords metadatası.
*   **Teknik Yaklaşım:** LLM tabanlı metin sınıflandırma (Symptom -> Department mapping) ve RAG.
*   **Risk:** Yanlış branşa yönlendirme (acil vakaları gözden kaçırma).
*   **Privacy/Security:** Semptom verilerinin anonimleştirilerek işlenmesi.
*   **Öğrenilecek Kavramlar:** Prompt Engineering, LLM function calling.
*   **Tahmini Complexity:** Medium

### 13.2 Smart Queue / Waiting Time Prediction
*   **Priority:** High
*   **Amaç:** Poliklinikteki hastaların anlık bekleme sürelerini tahmin ederek yoğunluk hissini azaltmak.
*   **Kullanıcı:** Hasta ve Hastane Yönetimi
*   **Gerekli Veri:** Geçmiş muayene süreleri, doktor hızı, anlık sıradaki hasta sayısı.
*   **Teknik Yaklaşım:** Regression algoritması veya basit Machine Learning tahmini.
*   **Risk:** Bekleme süresinin hatalı öngörülüp hastada güvensizlik yaratması.
*   **Privacy/Security:** Düşük risk (Sadece sayısal süre verisi kullanılır).
*   **Öğrenilecek Kavramlar:** Time-series analizi, regresyon algoritmaları.
*   **Tahmini Complexity:** High

### 13.3 AI Clinical Note Assistant
*   **Priority:** Medium
*   **Amaç:** Doktor muayene sırasında aldığı kısa veya dağınık notları anlamlı, yapılandırılmış ICD formatlı tıbbi bir özete dönüştürmek.
*   **Kullanıcı:** Doktor
*   **Gerekli Veri:** Doktorun girdiği ham taslak metin (draft).
*   **Teknik Yaklaşım:** LLM summarization and structuring.
*   **Risk:** Modelin "Halüsinasyon" (Hallucination) yaparak hastada olmayan bir bulguyu rapora eklemesi.
*   **Privacy/Security:** En Yüksek Risk (KVKK). Gönderilen notlardan hasta kimliğinin arındırılması (PHI Masking).
*   **Öğrenilecek Kavramlar:** Data masking, LLM parsing.
*   **Tahmini Complexity:** Medium

### 13.4 Smart Notification / Reminder System
*   **Priority:** Medium
*   **Amaç:** Hastaların randevularını kaçırmasını (No-show) engellemek için doğru zamanda dinamik bildirim göndermek.
*   **Kullanıcı:** Hasta
*   **Gerekli Veri:** Randevu saati, hastanın geçmiş no-show alışkanlıkları.
*   **Teknik Yaklaşım:** Kural tabanlı (Rule-based) veya basit sınıflandırma.
*   **Risk:** Yanlış zamanda veya gereğinden fazla bildirim (Spam) gitmesi.
*   **Privacy/Security:** Düşük risk.
*   **Öğrenilecek Kavramlar:** Push notifications (Web Push/Firebase).
*   **Tahmini Complexity:** Low

### 13.5 Patient Priority / Risk Scoring
*   **Priority:** Medium
*   **Amaç:** Bekleme listesindeki riskli (örn. yaşlı veya ağır semptomlu) hastaları belirleyip doktora öncelik önerisi sunmak.
*   **Kullanıcı:** Doktor, Kayıt Kabul Personeli
*   **Gerekli Veri:** Yaş, şikayet türü, triyaj bilgisi.
*   **Teknik Yaklaşım:** Basit skorlama algoritması veya Kural motoru.
*   **Risk:** Gerçekten acil olan bir vakaya düşük skor verilmesi. Kesinlikle bir doktor onayı mekanizmasına (Manual override) tabi olmalıdır.
*   **Privacy/Security:** Hastanın yaş ve tıbbi verilerinin işlenmesi.
*   **Öğrenilecek Kavramlar:** Rule engines, Classification.
*   **Tahmini Complexity:** Medium

### 13.6 Doctor Workload Analytics
*   **Priority:** Low
*   **Amaç:** Hastane yönetimine doktorların doluluk oranını sunmak, "burnout" (tükenmişlik) risklerini tahmin etmek.
*   **Kullanıcı:** Hastane Yönetimi (Admin)
*   **Gerekli Veri:** İptal edilen, tamamlanan randevular ve çalışma saatleri.
*   **Teknik Yaklaşım:** İstatistiksel Veri Analizi ve Dashboarding.
*   **Risk:** Yanlış hesaplanan metriklerin idari yanlış kararlara yol açması.
*   **Privacy/Security:** Düşük. (Doktorların operasyonel performans verisi).
*   **Öğrenilecek Kavramlar:** Data visualization, BI reporting.
*   **Tahmini Complexity:** Low

### 13.7 Intelligent Hospital Analytics
*   **Priority:** Low
*   **Amaç:** Hangi günlerde hangi polikliniklerin daha yoğun olacağını tahmin ederek personel vardiyasını optimize etmek.
*   **Kullanıcı:** Hastane Yönetimi (Admin)
*   **Gerekli Veri:** Geçmiş aylara/yıllara ait randevu hacimleri, hava durumu, mevsimsel faktörler.
*   **Teknik Yaklaşım:** Time-series Forecasting (ARIMA vb.)
*   **Risk:** Tahminlerin tutmaması sonucu personel yetersizliği.
*   **Privacy/Security:** Tamamen anonimleştirilmiş, toplu (aggregated) veri işlenir.
*   **Öğrenilecek Kavramlar:** Data aggregation, forecasting.
*   **Tahmini Complexity:** High

### 13.8 Hospital Resource / Capacity Prediction
*   **Priority:** Low
*   **Amaç:** Laboratuvar veya yatak kapasitesinin tükenme noktasını öngörmek.
*   **Kullanıcı:** Admin
*   **Gerekli Veri:** Donanım ve oda kullanım oranları.
*   **Teknik Yaklaşım:** Trend analizi.
*   **Risk:** Kapasite doluluk tahminlerinin gecikmeli gelmesi.
*   **Privacy/Security:** Düşük risk (Hastane envanter verisi).
*   **Öğrenilecek Kavramlar:** Resource management algorithms.
*   **Tahmini Complexity:** Medium

---

## 14. Production Readiness Checklist
Uygulamanın tam canlı (production) ortama çıkmadan önce doğrulanması gereken minimum kriterler:

*   **Architecture:** Uygulamanın tamamen stateless mimaride olduğunun doğrulanması (Yatay ölçeklenebilirlik için).
*   **Security:** JWT gizliliğinin (Secret Key) environment variable ile gizlenmesi, SQL Injection ve XSS açıklarının denetlenmesi (Prepared statements, React escape vb.).
*   **Testing:** Kritik iş kurallarının (örn. randevu alma) %80+ oranında Unit/Integration test ile korunması.
*   **Observability:** Logların merkezi bir noktaya toplanması, MDC TraceId kontrolü, Prometheus/Actuator üzerinden memory/CPU metriklerinin izlenmesi.
*   **Deployment:** Dockerizasyonun tamamlanması, portların izole edilmesi ve prod buildlerin boyut optimizasyonu.
*   **Backup/Recovery:** Veritabanının düzenli olarak otomatik yedeğinin (cron/dump) alınması ve felaket anında (Disaster Recovery) geri dönüş adımlarının denenmesi.
*   **Performance:** Veritabanı Connection Pool (HikariCP vb.) ayarlarının yapılması, kritik endpointlerin Stress/Load testlerine tabi tutulması.
*   **Documentation:** Deployment süreci (Kurulum) belgelerinin README içerisinde (veya ayrı docs altında) açıkça yer alması.

---

## 15. Teknofest Presentation & Product Roadmap
Teknofest gibi yarışmalarda projenin teknik bir üründen çıkıp, jüriye katma değer üreten inovatif bir çözüm olarak sunulması için stratejik yol haritası:

*   **Problem Definition:** Türkiye sağlık sistemindeki "Poliklinik önlerinde uzun kuyruklar ve verimsiz doktor-hasta eşleşmesi" problemi.
*   **Target Users:** Kamu/Özel hastaneler, randevu arayan vatandaşlar, iş yükü artan doktorlar.
*   **Differentiating Features:** Yalnızca klasik bir CRUD randevu sistemi değil, *Karar Destek Sistemleri* (AI) ile güçlendirilmiş, ölçeklenebilir ve sağlam altyapılı bir mimari.
*   **Technical Architecture:** Spring Boot ve Next.js kullanılarak sektör standartlarında, concurrency ve n+1 gibi problemlere karşı dirençli (resilient) sistem.
*   **Intelligent Features:** AI Feature Candidate'lerden 1 veya 2 tanesinin çalışan prototipi (Örn: AI Clinical Note Assistant veya Intelligent Appointment Scheduling).
*   **Metrics:** Uygulamanın "bekleme süresini %X azaltabileceği" veya "doktor raporlama yükünü %Y düşürebileceği" gibi teorik veya simüle edilmiş metrikler.
*   **Demo Scenario:** Hastanın (AI desteğiyle) doğru poliklinikten randevu alması -> Doktor sırasına düşmesi -> Doktorun (AI asistan ile) muayeneyi hızlıca tamamlaması. Uçtan uca senaryo.
*   **Risk Management:** Veri gizliliği (KVKK/GDPR uyumu) ve halüsinasyon (AI yalanlama) risklerine karşı sistem mimarisinde alınan önlemlerin (Anonimleştirme, Doktor Manuel Onayı) jüriye açıkça sunulması.
*   **Documentation:** Tüm teknik borçların, mimari kararların ve vizyonun açıkça belgelendiği "Project Master Plan" gibi profesyonel mühendislik eserleri.
