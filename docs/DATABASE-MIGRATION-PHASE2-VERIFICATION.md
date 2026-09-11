# DATABASE MIGRATION PHASE 2 — REPOSITORY VERIFICATION

## 1. VERIFIED

Aşağıdaki özellikler audit raporuyla birebir örtüşmektedir ve gerçek kodda doğrulanmıştır:

* **Entity Anotasyonları:** Tüm Entity sınıfları `@Entity` ve `@Table(name="...")` anotasyonlarına sahip.
* **ID generation:** Tüm Entity'lerde ID `Long` tipinde ve `@GeneratedValue(strategy = GenerationType.IDENTITY)` stratejisiyle tanımlanmış.
* **Soft Delete:** `Patient`, `Doctor`, `Department` ve `Appointment` sınıflarında `@SQLDelete` ve `@SQLRestriction` mevcut. `Patient` ve `Doctor` sınıflarında `tc_identity_number` unique index'ini bozmamak için silinirken `_del_id` ekleme mantığı (`CONCAT`) bulunuyor.
* **Double Booking Prevention:** `Appointment` entity'si üzerinde `active_slot_id` unique kolonu ve `@PrePersist` / `@PreUpdate` içinde `doctor.getId() + "_" + appointmentDate` formatında slot ataması yapılıyor.
* **Enum Mapping:** `Appointment.status` alanı `@Enumerated(EnumType.STRING)` olarak maplenmiş.
* **Date/Time Mapping:** `Appointment.appointmentDate` alanı `LocalDateTime`, `DoctorLeave.startDate` ve `endDate` alanları `LocalDate` olarak doğru tiplerde maplenmiş.
* **System Setting:** `SystemSetting` sınıfında ID `1L` olarak hardcoded edilmiş ve uygulama katmanında ilk istendiğinde otomatik olarak oluşturulduğu doğrulanmıştır (`SystemSettingServiceImpl`).
* **Indexes:** 
  * `Notification` sınıfında `IDX_NOTIFICATION_PATIENT_DATE` ve `IDX_NOTIFICATION_DOCTOR_DATE` indexleri,
  * `DoctorLeave` sınıfında `IDX_DOCTOR_LEAVE_DOC_DATES` indexi,
  * `Appointment` sınıfında `IDX_APPOINTMENT_DOC_STAT_DATE` indexleri tanımlı.

## 2. DISCREPANCIES

Audit raporu beklentileri ile gerçek kod arasında bulunan farklılıklar:

1. **Polyclinic Entity Foreign Key:**
   `Polyclinic` entity'sinde `Department` ilişkisi `@ManyToOne` ile değil, basit bir primitif kolon olarak tanımlanmış:
   ```java
   @Column(name = "department_id", nullable = false)
   private Long departmentId;
   ```
   *Bu durum veritabanı düzeyinde Flyway'de foreign key oluşturulurken sorun yaratmaz, ancak uygulama katmanında doğrudan nesne ilişkisi (`polyclinic.getDepartment()`) kullanılmasını engeller.*

2. **DoctorRegistrationRequest Entity Foreign Key:**
   Aynı durum burada da mevcut. `department_id` alanı basit bir kolon olarak tutulmuş:
   ```java
   @Column(name = "department_id")
   private Long departmentId;
   ```

3. **DoctorLeave Entity Foreign Key:**
   Burada da `doctor_id` alanı bir ilişki (`@ManyToOne`) yerine primitif tip olarak tanımlanmış:
   ```java
   @Column(name = "doctor_id", nullable = false)
   private Long doctorId;
   ```

4. **Flyway Configuration:**
   `pom.xml` ve `application.properties` içerisinde Flyway dependency'si veya konfigürasyonu şu an **yok**. Proje tamamen `hibernate.ddl-auto=update` ile çalışıyor.

## 3. MIGRATION RISKS

Flyway V1 yazılırken dikkat edilmesi gereken riskler:

1. **`spring.jpa.hibernate.ddl-auto=update`:**
   Şu an uygulama veritabanını otomatik güncelliyor. Flyway eklendikten sonra bu `validate` olarak değiştirilmelidir. Aksi takdirde Hibernate'in oluşturduğu şema ile Flyway'in çalıştırdığı scriptler arasında çakışmalar (conflict) yaşanabilir.
2. **`HospitalAppointmentApplication` Seeder Mekanizması:**
   `CommandLineRunner` içinde `departmentRepository.count() == 0` kontrolü var. Eğer Flyway V1 migration'ı içine data (örneğin standart departmanlar) eklerseniz, bu seeder bir daha hiç çalışmaz. Eğer V1 sadece DDL (tablo oluşturma) olursa, seeder varlığını sürdürüp otomatik seed atabilir.
3. **`appointments.active_slot_id` Unique Constraint:**
   Bu alan null değerler alabilir (COMPLETED veya CANCELLED olduğunda). Veritabanlarında unique bir kolonda birden fazla NULL değere izin verilip verilmemesi konusunda dikkatli olunmalıdır (MySQL ve PostgreSQL birden fazla NULL değere izin verir, bu nedenle güvenlidir).
4. **Gizli Foreign Key Uyumsuzlukları:**
   Entity'lerde `@ManyToOne` olmayan, sadece ID (Long) olarak tutulan alanlar (`polyclinic.department_id` vb.) için Hibernate veritabanında `FOREIGN KEY` constraint'i otomatik oluşturmamış olabilir. Ancak veritabanı tutarlılığı için Flyway V1 sql scriptinde bunları gerçek `FOREIGN KEY` olarak tanımlamak önemlidir.

## 4. FINAL SCHEMA BLUEPRINT

Flyway V1 DDL için kullanılacak doğrulanmış tablo ve kolon yapıları:

* **users**: id (PK, BIGINT AI), username (VARCHAR(255) UQ NN), email (VARCHAR(255) UQ NN), password (VARCHAR(255) NN), role (VARCHAR(255) NN), reference_id (BIGINT), needs_password_change (BOOLEAN NN).
* **system_settings**: id (PK, BIGINT), appointment_duration (INT NN), work_start_time (VARCHAR(255) NN), work_end_time (VARCHAR(255) NN), lunch_break_start (VARCHAR(255) NN), lunch_break_end (VARCHAR(255) NN), maintenance_mode (BOOLEAN NN).
* **departments**: id (PK, BIGINT AI), name (VARCHAR(100) UQ NN), description (VARCHAR(500)), is_active (BOOLEAN).
* **patients**: id (PK, BIGINT AI), first_name (VARCHAR(50) NN), last_name (VARCHAR(50) NN), tc_identity_number (VARCHAR(50) UQ NN), phone_number (VARCHAR(15)), email (VARCHAR(100)), is_active (BOOLEAN).
* **polyclinics**: id (PK, BIGINT AI), name (VARCHAR(255) UQ NN), room_number (VARCHAR(255)), department_id (BIGINT NN, FK -> departments(id)).
* **doctors**: id (PK, BIGINT AI), first_name (VARCHAR(50) NN), last_name (VARCHAR(50) NN), specialization (VARCHAR(100) NN), tc_identity_number (VARCHAR(50) UQ NN), phone_number (VARCHAR(15)), email (VARCHAR(100)), is_active (BOOLEAN), department_id (BIGINT NN, FK -> departments(id)), polyclinic_id (BIGINT, FK -> polyclinics(id)).
* **doctor_registration_requests**: id (PK, BIGINT AI), tc_identity_number (VARCHAR(11) NN), first_name (VARCHAR(255) NN), last_name (VARCHAR(255) NN), email (VARCHAR(255) NN), phone_number (VARCHAR(255)), specialization (VARCHAR(255)), department_id (BIGINT, FK -> departments(id)), password (VARCHAR(255) NN), status (VARCHAR(255) NN), request_date (TIMESTAMP NN).
* **doctor_leaves**: id (PK, BIGINT AI), doctor_id (BIGINT NN, FK -> doctors(id)), start_date (DATE NN), end_date (DATE NN), reason (VARCHAR(255) NN), status (VARCHAR(255) NN). Index: IDX_DOCTOR_LEAVE_DOC_DATES.
* **notifications**: id (PK, BIGINT AI), patient_id (BIGINT, FK -> patients(id)), doctor_id (BIGINT, FK -> doctors(id)), message (VARCHAR(500) NN), is_read (BOOLEAN NN), created_at (TIMESTAMP NN). Indexes: IDX_NOTIFICATION_PATIENT_DATE, IDX_NOTIFICATION_DOCTOR_DATE.
* **appointments**: id (PK, BIGINT AI), patient_id (BIGINT NN, FK -> patients(id)), doctor_id (BIGINT NN, FK -> doctors(id)), appointment_date (TIMESTAMP NN), status (VARCHAR(50) NN), notes (VARCHAR(500)), is_active (BOOLEAN), active_slot_id (VARCHAR(100) UQ), version (BIGINT), created_at (TIMESTAMP), updated_at (TIMESTAMP), created_by (VARCHAR(255)), updated_by (VARCHAR(255)). Index: IDX_APPOINTMENT_DOC_STAT_DATE.
* **diagnoses**: id (PK, BIGINT AI), appointment_id (BIGINT NN, FK -> appointments(id)), icd10_code (VARCHAR(10) NN), description (VARCHAR(255) NN).
* **prescriptions**: id (PK, BIGINT AI), appointment_id (BIGINT NN, FK -> appointments(id)), medication_name (VARCHAR(100) NN), dosage (VARCHAR(50) NN), usage_instruction (VARCHAR(255) NN).

*(Kısaltmalar: AI = Auto Increment, UQ = UNIQUE, NN = NOT NULL, FK = FOREIGN KEY)*

## 5. RECOMMENDATION

Gerçek kod bazında incelendiğinde şema büyük oranda audit beklentileri ile tutarlıdır. Sadece 3 farklı Entity'de (`Polyclinic`, `DoctorLeave`, `DoctorRegistrationRequest`) ilişkiler nesne düzeyinde `@ManyToOne` ile değil de, sadece ID tutularak sağlanmıştır. Ancak SQL ve veritabanı şeması tasarımında bunların doğrudan Foreign Key olarak tanımlanmasında bir sakınca yoktur.

`pom.xml` içerisinde henüz Flyway'in olmaması da bir diğer farklılıktır. Migration uygulandığı an `application.properties` üzerinden `ddl-auto` kapatılmalıdır.

**Karar:** `READY FOR V1 MIGRATION`
