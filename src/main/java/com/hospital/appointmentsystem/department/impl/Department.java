package com.hospital.appointmentsystem.department.impl;

// ⭐ JPA anotasyonlarını import ediyoruz
// jakarta.persistence paketi, JPA'nın tüm anotasyonlarını içerir
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
 * ║  🏢 DEPARTMENT ENTITY — Veritabanı Tablosunu Temsil Eden Sınıf ║
 * ╠══════════════════════════════════════════════════════════════════╣
 * ║                                                                  ║
 * ║  📌 ENTITY NEDİR?                                               ║
 * ║  Entity, veritabanındaki bir TABLOYU temsil eden Java sınıfıdır.║
 * ║  Bu sınıftaki her alan (field) tablodaki bir KOLONA karşılık    ║
 * ║  gelir.                                                          ║
 * ║                                                                  ║
 * ║  Yani sen bu sınıfı yazıyorsun, JPA otomatik olarak            ║
 * ║  veritabanında şöyle bir tablo oluşturuyor:                     ║
 * ║                                                                  ║
 * ║  ┌─────────────────────────────────────────────┐                ║
 * ║  │           DEPARTMENTS Tablosu                │                ║
 * ║  ├──────────┬──────────────┬───────────────────┤                ║
 * ║  │ id (PK)  │ name         │ description       │                ║
 * ║  ├──────────┼──────────────┼───────────────────┤                ║
 * ║  │ 1        │ Cardiology   │ Heart diseases     │                ║
 * ║  │ 2        │ Neurology    │ Brain and nerves   │                ║
 * ║  │ 3        │ Orthopedics  │ Bone and joints    │                ║
 * ║  └──────────┴──────────────┴───────────────────┘                ║
 * ║                                                                  ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */

// ──────────────────────────────────────────────────────────────
// 📌 @Entity
// Bu anotasyon JPA'ya der ki:
// "Bu sınıf sıradan bir Java sınıfı değil, veritabanındaki
//  bir tabloyu temsil ediyor!"
// Bu olmadan JPA bu sınıfı görmezden gelir.
// ──────────────────────────────────────────────────────────────
@Entity

// ──────────────────────────────────────────────────────────────
// 📌 @Table(name = "departments")
// Veritabanındaki tablo adını belirler.
// Yazmazsak → JPA sınıf adını kullanır (Department)
// Yazarsak  → Tablo adını biz kontrol ederiz (departments)
// Genellikle çoğul isim kullanılır: department → departments
// ──────────────────────────────────────────────────────────────
@Table(name = "departments")
@SQLDelete(sql = "UPDATE departments SET is_active = false WHERE id=?")
@SQLRestriction("is_active = true OR is_active IS NULL")
public class Department {

    // ──────────────────────────────────────────────────────────
    // 📌 @Id
    // Bu alan tablonun PRIMARY KEY'idir (benzersiz kimlik).
    // Her tabloda mutlaka bir @Id olmalı!
    // Primary Key nedir? → Her satırı benzersiz yapan alan.
    // Mesela TC Kimlik No gibi — iki kişinin aynı olamaz.
    //
    // 📌 @GeneratedValue(strategy = GenerationType.IDENTITY)
    // "ID değerini sen verme, veritabanı otomatik oluştursun"
    // IDENTITY stratejisi → 1, 2, 3, 4... diye otomatik artar
    //
    // Diğer stratejiler:
    //   AUTO     → JPA kendisi en uygun stratejiyi seçer
    //   SEQUENCE → Veritabanı sequence kullanır (PostgreSQL için ideal)
    //   TABLE    → Ayrı bir tablo ile ID üretir
    // ──────────────────────────────────────────────────────────
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ──────────────────────────────────────────────────────────
    // 📌 @Column
    // Bu alan veritabanındaki bir kolonu temsil eder.
    //
    // Özellikleri:
    //   name     → Kolon adı (yazmazsak alan adını kullanır)
    //   nullable → null olabilir mi? (false = boş bırakılamaz)
    //   unique   → Benzersiz olmalı mı? (true = aynı isimde iki bölüm olamaz)
    //   length   → Maksimum karakter uzunluğu
    //
    // @Column yazmazsak ne olur?
    // → JPA yine de bir kolon oluşturur, ama varsayılan ayarlarla.
    //   Yani @Column isteğe bağlıdır ama kontrol sağlar.
    // ──────────────────────────────────────────────────────────
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // ──────────────────────────────────────────────────────────
    // 📌 CONSTRUCTOR'LAR (Yapıcı Metotlar)
    //
    // 1. Boş constructor (parametresiz)
    //    → JPA ZORUNLU OLARAK İSTER! JPA veritabanından veri
    //      okurken önce boş bir obje oluşturur, sonra alanları
    //      doldurur. Boş constructor olmadan çalışmaz!
    //
    // 2. Parametreli constructor
    //    → Biz kod yazarken kolaylık olsun diye kullanırız.
    //      new Department("Cardiology", "Heart diseases") gibi.
    // ──────────────────────────────────────────────────────────

    // JPA için ZORUNLU — parametresiz constructor
    public Department() {
    }

    // Bizim kullanımımız için — parametreli constructor
    public Department(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // ──────────────────────────────────────────────────────────
    // 📌 GETTER ve SETTER Metotları
    //
    // Neden gerekli?
    // → Java'da alanlar "private" olduğu için dışarıdan
    //   doğrudan erişilemez. Getter/Setter bu erişimi sağlar.
    //
    // Getter → Değeri OKU  (getId(), getName()...)
    // Setter → Değeri YAZ  (setName("Cardiology")...)
    //
    // JPA, Entity'deki verileri okumak ve yazmak için
    // getter/setter metotlarını kullanır.
    //
    // 💡 Not: Lombok kütüphanesi ile @Getter @Setter yazarak
    // bu metotları otomatik oluşturabilirsin. Ama şimdilik
    // elle yazıyoruz ki ne olduğunu anlayasın!
    // ──────────────────────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
