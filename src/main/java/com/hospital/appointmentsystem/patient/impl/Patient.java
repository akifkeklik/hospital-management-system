package com.hospital.appointmentsystem.patient.impl;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║  🧑 PATIENT ENTITY — Hasta Tablosu                              ║
 * ╠══════════════════════════════════════════════════════════════════╣
 * ║                                                                  ║
 * ║  Bu Entity, Department'tan biraz daha karmaşık çünkü:           ║
 * ║  - Daha fazla @Column özelliği kullanıyoruz                     ║
 * ║  - unique ve nullable kısıtlamaları var                          ║
 * ║                                                                  ║
 * ║  Veritabanında oluşacak tablo:                                   ║
 * ║  ┌────┬──────┬───────┬─────────────┬──────────┬────────────┐    ║
 * ║  │ id │ first│ last  │ tc_identity │ phone    │ email      │    ║
 * ║  │    │ _name│ _name │ _number     │ _number  │            │    ║
 * ├────┼──────┼───────┼─────────────┼──────────┼────────────┤    ║
 * ║  │ 1  │ Ahmet│ Yılmaz│ 12345678901 │ 05551234 │ a@mail.com │    ║
 * ║  └────┴──────┴───────┴─────────────┴──────────┴────────────┘    ║
 * ║                                                                  ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */
@Entity
@Table(name = "patients")
@SQLDelete(sql = "UPDATE patients SET is_active = false, tc_identity_number = CONCAT(tc_identity_number, '_del_', id) WHERE id=?")
@SQLRestriction("is_active = true OR is_active IS NULL")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ──────────────────────────────────────────────────────────
    // 📌 @Column DETAYLARI — Daha Fazla Kontrol
    //
    // nullable = false → Bu alan BOŞ BIRAKILAMAZ
    //   → Veritabanı seviyesinde "NOT NULL" kısıtlaması ekler
    //   → Hasta adı olmadan kayıt yapamazsın
    //
    // length = 50 → Maksimum 50 karakter
    //   → VARCHAR(50) olarak oluşturulur
    // ──────────────────────────────────────────────────────────
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    // ──────────────────────────────────────────────────────────
    // 📌 unique = true
    // Bu alan BENZERSİZ olmalı — aynı TC ile iki hasta kaydı olamaz!
    // Veritabanında UNIQUE INDEX oluşturur.
    //
    // Gerçek hayatta TC Kimlik No benzersizdir,
    // o yüzden unique=true diyoruz.
    // ──────────────────────────────────────────────────────────
    @Column(name = "tc_identity_number", nullable = false, unique = true, length = 50)
    private String tcIdentityNumber;

    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // JPA için ZORUNLU — parametresiz constructor
    public Patient() {
    }

    public Patient(String firstName, String lastName, String tcIdentityNumber,
                   String phoneNumber, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.tcIdentityNumber = tcIdentityNumber;
        this.phoneNumber = phoneNumber;
        this.email = email;
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
}
