package com.hospital.appointmentsystem.doctor.impl;

import com.hospital.appointmentsystem.department.impl.Department;
import com.hospital.appointmentsystem.polyclinic.impl.Polyclinic;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║  👨‍⚕️ DOCTOR ENTITY — Doktor Tablosu (İLİŞKİLİ ENTITY!)         ║
 * ╠══════════════════════════════════════════════════════════════════╣
 * ║                                                                  ║
 * ║  🆕 YENİ KAVRAM: ENTITY İLİŞKİLERİ                             ║
 * ║                                                                  ║
 * ║  Gerçek dünyada tablolar birbirleriyle İLİŞKİLİDİR:            ║
 * ║  - Bir BÖLÜM'de birden fazla DOKTOR çalışır                    ║
 * ║  - Her DOKTOR bir BÖLÜM'e bağlıdır                             ║
 * ║                                                                  ║
 * ║  Bu ilişkiye "Many-to-One" (Çoktan Bire) denir:                ║
 * ║  → MANY doktor → ONE bölüm                                     ║
 * ║  → Birçok doktor, bir bölüme bağlıdır                          ║
 * ║                                                                  ║
 * ║  Veritabanında şöyle görünür:                                    ║
 * ║                                                                  ║
 * ║  DEPARTMENTS                  DOCTORS                           ║
 * ║  ┌────┬────────────┐         ┌────┬───────┬──────────────┐      ║
 * ║  │ id │ name       │         │ id │ name  │ department_id│      ║
 * ║  ├────┼────────────┤         ├────┼───────┼──────────────┤      ║
 * ║  │ 1  │ Cardiology │◄────────│ 1  │ Ahmet │ 1            │      ║
 * ║  │    │            │◄────────│ 2  │ Mehmet│ 1            │      ║
 * ║  │ 2  │ Neurology  │◄────────│ 3  │ Ayşe  │ 2            │      ║
 * ║  └────┴────────────┘         └────┴───────┴──────────────┘      ║
 * ║                                                                  ║
 * ║  Doktor tablosunda "department_id" kolonu var.                  ║
 * ║  Bu kolon, doktorun hangi bölüme ait olduğunu gösterir.         ║
 * ║  Buna FOREIGN KEY (Yabancı Anahtar) denir.                     ║
 * ║                                                                  ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */
@Entity
@Table(name = "doctors")
@SQLDelete(sql = "UPDATE doctors SET is_active = false, tc_identity_number = CONCAT(tc_identity_number, '_del_', id) WHERE id=?")
@SQLRestriction("is_active = true OR is_active IS NULL")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    // ──────────────────────────────────────────────────────────
    // 📌 Uzmanlık alanı
    // Doktorun uzmanlığı (Kardiyoloji Uzmanı, Nörolog vb.)
    // ──────────────────────────────────────────────────────────
    @Column(name = "specialization", nullable = false, length = 100)
    private String specialization;

    @Column(name = "tc_identity_number", nullable = false, unique = true, length = 50)
    private String tcIdentityNumber;

    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // ══════════════════════════════════════════════════════════
    //  ⭐ İLİŞKİ TANIMLAMASI — En önemli kısım!
    // ══════════════════════════════════════════════════════════

    // ──────────────────────────────────────────────────────────
    // 📌 @ManyToOne — Çoktan Bire İlişki
    //
    // Bu anotasyon JPA'ya der ki:
    // "Birçok Doctor, bir Department'a bağlıdır"
    //
    // fetch = FetchType.EAGER
    // → Doktoru çektiğinde, bağlı olduğu bölümü de HEMEN çek
    //
    // İki seçenek var:
    //   EAGER → Doktorla birlikte bölümü de hemen yükle
    //   LAZY  → Bölümü sadece ihtiyaç olduğunda yükle (performans için)
    //
    // Şimdilik EAGER kullanıyoruz (öğrenmesi kolay).
    //
    // 📌 @JoinColumn(name = "department_id")
    // → Veritabanındaki FOREIGN KEY kolonunun adını belirler
    // → doctors tablosunda "department_id" adında bir kolon oluşturur
    // → Bu kolon, departments tablosunun id'sine referans verir
    //
    // nullable = false → Her doktor bir bölüme bağlı OLMALI
    // ──────────────────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "polyclinic_id", nullable = true)
    private Polyclinic polyclinic;

    // JPA için ZORUNLU
    public Doctor() {
    }

    public Doctor(String firstName, String lastName, String tcIdentityNumber, String specialization,
                  String phoneNumber, String email, Department department) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.tcIdentityNumber = tcIdentityNumber;
        this.specialization = specialization;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.department = department;
    }

    public Doctor(String firstName, String lastName, String tcIdentityNumber, String specialization,
                  String phoneNumber, String email, Department department, Polyclinic polyclinic) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.tcIdentityNumber = tcIdentityNumber;
        this.specialization = specialization;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.department = department;
        this.polyclinic = polyclinic;
    }

    // ── Getter ve Setter ──

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getTcIdentityNumber() {
        return tcIdentityNumber;
    }

    public void setTcIdentityNumber(String tcIdentityNumber) {
        this.tcIdentityNumber = tcIdentityNumber;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    // ⭐ Department ilişkisi getter/setter
    // Bu sayede doktor.getDepartment() diyerek
    // doktorun bağlı olduğu bölümü alabilirsin
    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Polyclinic getPolyclinic() {
        return polyclinic;
    }

    public void setPolyclinic(Polyclinic polyclinic) {
        this.polyclinic = polyclinic;
    }
}
