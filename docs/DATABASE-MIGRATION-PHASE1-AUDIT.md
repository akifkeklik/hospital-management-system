# DATABASE MIGRATION PHASE 1 — FORENSIC AUDIT

## 1. Executive Summary
Projenin veritabanı şeması, mevcut entity modellemesi ve JPA/Hibernate yapılandırması üzerinde gerçekleştirilen salt okunur (read-only) adli inceleme (forensic audit) sonucudur. İncelenen kod bloklarında ve testlerde hiçbir değişiklik yapılmamış olup, tamamen analiz amaçlı hazırlanmıştır. Mevcut durumda veritabanı şeması ve senkronizasyonu Hibernate'in `ddl-auto` özelliği ile yönetilmektedir. Üretim ortamında stabiliteyi ve veri bütünlüğünü sağlamak adına Flyway üzerinden explicit veritabanı migration yönetimine geçilmesi planlanmaktadır.

## 2. Entity Inventory
| Entity Class | @Table | Primary Key | ID Strategy | Özel / Riskli Kolonlar |
| --- | --- | --- | --- | --- |
| `User` | `users` | `id` (Long) | `IDENTITY` | `username` (unique, nullable=false), `email` (unique, nullable=false) |
| `SystemSetting` | `system_settings` | `id` (Long) | Manuel 1L atanıyor | `appointment_duration` |
| `Polyclinic` | `polyclinics` | `id` (Long) | `IDENTITY` | `name` (unique, nullable=false), `department_id` (not null) |
| `Patient` | `patients` | `id` (Long) | `IDENTITY` | `tc_identity_number` (unique, length=50, nullable=false) |
| `Diagnosis` | `diagnoses` | `id` (Long) | `IDENTITY` | `icd10_code` (length=10), `description` (length=255) |
| `DoctorLeave` | `doctor_leaves` | `id` (Long) | `IDENTITY` | `start_date`, `end_date` (LocalDate tipi) |
| `Notification` | `notifications` | `id` (Long) | `IDENTITY` | `message` (length=500), `created_at` (LocalDateTime tipi) |
| `Prescription` | `prescriptions` | `id` (Long) | `IDENTITY` | `medication_name` (length=100) |
| `Doctor` | `doctors` | `id` (Long) | `IDENTITY` | `tc_identity_number` (unique, nullable=false, length=50) |
| `Department` | `departments` | `id` (Long) | `IDENTITY` | `name` (unique, nullable=false, length=100) |
| `DoctorRegistrationRequest`| `doctor_registration_requests` | `id` (Long) | `IDENTITY`| `tc_identity_number` (length=11, nullable=false) |
| `Appointment` | `appointments` | `id` (Long) | `IDENTITY` | `status` (Enum String), `appointment_date` (LocalDateTime), `active_slot_id` (unique, length=100) |

## 3. Relationship Map
- **Appointment -> Patient:** `@ManyToOne(fetch = FetchType.LAZY)`, `patient_id` FK (nullable=false)
- **Appointment -> Doctor:** `@ManyToOne(fetch = FetchType.LAZY)`, `doctor_id` FK (nullable=false)
- **Diagnosis -> Appointment:** `@ManyToOne(fetch = FetchType.LAZY)`, `appointment_id` FK (nullable=false)
- **Prescription -> Appointment:** `@ManyToOne(fetch = FetchType.LAZY)`, `appointment_id` FK (nullable=false)
- **Doctor -> Department:** `@ManyToOne(fetch = FetchType.LAZY)`, `department_id` FK (nullable=false)
- **Doctor -> Polyclinic:** `@ManyToOne(fetch = FetchType.LAZY)`, `polyclinic_id` FK (nullable=true)

## 4. Constraint & Index Inventory
- **Primary Keys:** Tüm tablolardaki benzersiz `id` (Long) kolonları.
- **Foreign Keys:**
  - `appointments` tablosunda `patient_id`, `doctor_id`
  - `diagnoses` tablosunda `appointment_id`
  - `prescriptions` tablosunda `appointment_id`
  - `doctors` tablosunda `department_id`, `polyclinic_id`
- **Unique Constraints:**
  - `users`: `username`, `email`
  - `polyclinics`: `name`
  - `patients`: `tc_identity_number`
  - `doctors`: `tc_identity_number`
  - `departments`: `name`
  - `appointments`: `active_slot_id`
- **Indexes:**
  - `doctor_leaves`: `IDX_DOCTOR_LEAVE_DOC_DATES` on (`doctor_id`, `start_date`, `end_date`)
  - `notifications`: `IDX_NOTIFICATION_PATIENT_DATE` on (`patient_id`, `created_at DESC`) ve `IDX_NOTIFICATION_DOCTOR_DATE` on (`doctor_id`, `created_at DESC`)
  - `appointments`: `IDX_APPOINTMENT_DOC_STAT_DATE` on (`doctor_id`, `status`, `appointment_date`)

## 5. Enum Inventory
- **`AppointmentStatus`:** `SCHEDULED`, `ARRIVED`, `IN_EXAMINATION`, `COMPLETED`, `CANCELLED`, `NO_SHOW`
- **Kullanıldığı Yer:** `Appointment` sınıfı `status` alanı.
- **Veritabanı Temsili:** `@Enumerated(EnumType.STRING)`
- **Migration Risk:** Veritabanına STRING olarak yazılması güvenlidir. Ancak yeni bir Enum değeri eklendiğinde SQL seviyesinde de desteklenmesi veya değerin silinmesi sırasında database üzerinde manual müdahale gerekebilir.

## 6. Date/Time Mapping
- `DoctorLeave` (`startDate`, `endDate`): `LocalDate` sınıfı. (SQL: `DATE`)
- `Notification` (`createdAt`): `LocalDateTime` sınıfı. (SQL: `DATETIME` veya `TIMESTAMP`)
- `DoctorRegistrationRequest` (`requestDate`): `LocalDateTime` sınıfı. (SQL: `DATETIME` veya `TIMESTAMP`)
- `Appointment` (`appointmentDate`, `createdAt`, `updatedAt`): `LocalDateTime` sınıfı. (SQL: `DATETIME` veya `TIMESTAMP`)
- **Uyumluluk Riski:** Veritabanı timezone farklılıkları LocalDateTime davranışını etkileyebilir. MySQL ve PostgreSQL üzerinde DATETIME/TIMESTAMP dönüşümleri doğru yönetilmelidir.

## 7. Current JPA/Database Configuration
- **DB Driver:** MySQL (`com.mysql.cj.jdbc.Driver`) default olarak tanımlı. PostgreSQL driver (`org.postgresql.Driver`) `pom.xml` içinde Render uyumluluğu için runtime scope ile yer almakta.
- **H2 Test DB:** `application-test.properties` yapılandırmasında `spring.jpa.hibernate.ddl-auto=create-drop` ve H2 dialect kullanılıyor.
- **Production/Dev ddl-auto:** `spring.jpa.hibernate.ddl-auto=${DDL_AUTO:update}` (Çok tehlikeli, Flyway sonrası `validate` olmalı).
- **Format SQL:** `spring.jpa.properties.hibernate.format_sql=true`

## 8. MySQL ↔ PostgreSQL Compatibility Risks
- **Auto Increment:** MySQL'de `AUTO_INCREMENT`, PostgreSQL'de `SERIAL` veya `IDENTITY` olarak karşılık bulur.
- **Boolean Fields:** MySQL arka planda `TINYINT(1)` (true/false yerine 1/0) tutarken, PostgreSQL native `BOOLEAN` tipi destekler. (`isActive`, `isRead` vs.)
- **Text/String Limits:** Unique constraint içeren alanlar case-sensitivity (büyük/küçük harf duyarlılığı) MySQL ve PostgreSQL'de collation ayarlarına göre farklılık gösterebilir.
- **DateTime:** MySQL'de `DATETIME(6)`, PostgreSQL'de `TIMESTAMP` kullanılır.

## 9. Existing SQL / Schema Files
- Proje dizininde önceden hazırlanmış statik `.sql`, `schema.sql`, `init.sql` ya da migration klasör yapısı (örneğin `db/migration/`) bulunmamaktadır.
- Tablolar tamamen Hibernate reflection ddl-auto davranışı ile şekillenmiştir.

## 10. Seed / Initialization Data
- Veritabanı açılışında `HospitalAppointmentApplication` içindeki `CommandLineRunner` aracılığı ile seed data eklenmektedir.
- Şayet `departmentRepository.count() == 0` ise admin hesabı, departmanlar, doktorlar, poliklinikler, örnek hastalar ve randevular veritabanına kod tarafında enjekte edilmektedir.
- Flyway aşamasına geçildiğinde bu startup mekanizması gözden geçirilmelidir.

## 11. Production Data Risks
- `active_slot_id` kolonu, Concurrency (double-booking) prevention için kod tarafında @PrePersist/@PreUpdate ile formata bağlanıp string (ör: `doctor_id_YYYY-MM-DDTHH:MM`) olarak oluşturuluyor. Unique constraint içerdiği için database'in mevcut validasyonunda eski veriler varsa konflikt oluşturabilir.
- Soft delete (`is_active=false`) özelliği veritabanında index performansına (NULL/TRUE kontrolleri) ve unique constraintlere (`tc_identity_number = CONCAT(tc_identity_number, '_del_', id)`) doğrudan temas etmektedir.
- `SystemSetting` `id=1L` varsayımı doğrudan kod içinde tanımlıdır, auto increment kullanan DB ile çatışmamalıdır.

## 12. Flyway Migration Strategy
- A. **Mevcut sistem için baseline mı gerekli?** Evet, mevcut production'da aktif tablolar varsa kesinlikle bir `V1__baseline.sql` veya DB'ye göre spesifik baseline senaryosu ile başlanmalı ve flyway baseline komutu çalıştırılmalıdır.
- B. **İlk migration:** Tabloları oluşturan tam DDL senaryosu `V1__init_schema.sql` olarak kurgulanmalıdır.
- C. **Mevcut production database için güvenli geçiş:** `flyway baseline` ile veritabanının `flyway_schema_history` tablosu başlatılmalı, sonrasında Hibernate devreden çıkartılmalıdır.
- D. **Yeni development database'ler nasıl oluşturulmalı:** Sıfır bir veritabanı oluşturulup, uygulamanın (ve Flyway'in) ayaklanarak scriptleri sırayla (V1, V2..) koşmasıyla sağlanmalıdır.
- E. **Hibernate ddl-auto hangi aşamada kaldırılmalı:** Flyway yapılandırması entegre edildiğinde, kod ayağa kaldırılmadan hemen önce kaldırılmalıdır.
- F. **Migration sonrası Hibernate:** `spring.jpa.hibernate.ddl-auto=validate` olarak yapılandırılmalıdır. Böylece kod değişikliklerinin veritabanı ile uyuşmazlığı erken fark edilir.
- G. **MySQL + PostgreSQL:** Tek bir Migration seti ANSI SQL ile yazılmaya çalışılsa bile `AUTO_INCREMENT`/`SERIAL` ve `TINYINT`/`BOOLEAN` tipleri yüzünden sorun çıkarır. Flyway'in vendor spesifik lokasyon ayarları ile (`db/migration/mysql` ve `db/migration/postgresql`) yönetilmesi önerilir.

## 13. Recommended Migration Order
1. Bağımsız Konfigürasyon ve Parent Tablolar: `system_settings`, `departments`, `patients`, `users`
2. Kısmi Bağımlı Tablolar: `polyclinics` (Department'a bağlı), `doctors` (Department ve Polyclinic'e bağlı)
3. Action/İşlem Tabloları: `doctor_registration_requests`, `doctor_leaves`, `notifications`
4. Kompleks İlişkili Core Tablo: `appointments`
5. Alt Child İşlemler: `diagnoses`, `prescriptions`
6. Son adım olarak İndeksler ve Unique kısıtlamalar.

## 14. Risks
- **[HIGH]** `SystemSetting` tablosu başlatma ID varsayımı (id=1L). Eğer DDL `AUTO_INCREMENT` kurarsa konflikt riski.
- **[MEDIUM]** `CommandLineRunner` tabanlı seed verilerinin yeni migration mantığıyla çatışması.
- **[MEDIUM]** MySQL ve PostgreSQL'e deploy uyumluluğu sebebiyle scriptlerin her iki syntax'a da kusursuz yazılması gerekliliği.
- **[MEDIUM]** Soft-delete `_del_id` concatenation yapısı sebebiyle uzun stringlerin `tc_identity_number` limitine (length=50) takılma ihtimali.

## 15. Recommended Next Step
- Spring projesine Flyway core ve veritabanı bağımlılıklarını dahil edip, statik `db/migration` klasörü altına MySQL ve PostgreSQL için ayrıştırılmış `V1__init_schema.sql` dosyasının yazılması. `ddl-auto` değerinin `validate` olarak ayarlanıp test edilmesi.

## 16. Final Verdict
**READY FOR FLYWAY IMPLEMENTATION**
Herhangi bir blocker bulunamadı, migration sürecine geçişe hazır.
