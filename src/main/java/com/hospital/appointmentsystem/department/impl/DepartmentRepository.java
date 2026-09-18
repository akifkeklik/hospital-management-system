package com.hospital.appointmentsystem.department.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║  🗄️ DEPARTMENT REPOSITORY — Veritabanı İşlemleri Arayüzü       ║
 * ╠══════════════════════════════════════════════════════════════════╣
 * ║                                                                  ║
 * ║  📌 REPOSITORY NEDİR?                                           ║
 * ║  Repository, veritabanı ile konuşan katmandır.                  ║
 * ║  "Şu veriyi kaydet", "Şu veriyi getir", "Şu veriyi sil"       ║
 * ║  gibi işlemleri yapar.                                          ║
 * ║                                                                  ║
 * ║  📌 JpaRepository NEDİR?                                        ║
 * ║  Spring Data JPA'nın sihirli arayüzüdür!                       ║
 * ║  Bu interface'i extend ettiğinde, aşağıdaki metotlar            ║
 * ║  OTOMATİK olarak kullanıma hazır olur:                          ║
 * ║                                                                  ║
 * ║  ┌────────────────────┬──────────────────────────────────┐      ║
 * ║  │ Metot              │ Ne Yapar?                         │      ║
 * ║  ├────────────────────┼──────────────────────────────────┤      ║
 * ║  │ save(entity)       │ Kaydet veya güncelle              │      ║
 * ║  │ findById(id)       │ ID ile bul                        │      ║
 * ║  │ findAll()          │ Tümünü getir                      │      ║
 * ║  │ deleteById(id)     │ ID ile sil                        │      ║
 * ║  │ count()            │ Toplam kayıt sayısı               │      ║
 * ║  │ existsById(id)     │ Bu ID var mı?                     │      ║
 * ║  └────────────────────┴──────────────────────────────────┘      ║
 * ║                                                                  ║
 * ║  🤯 TEK SATIR SQL YAZMADAN tüm bu işlemler hazır!              ║
 * ║                                                                  ║
 * ║  📌 JpaRepository<Department, Long> ne anlama geliyor?          ║
 * ║     - Department → Hangi Entity ile çalışacak                   ║
 * ║     - Long       → Entity'nin ID'sinin tipi (@Id alanının tipi) ║
 * ║                                                                  ║
 * ║  📌 @Repository                                                  ║
 * ║  Bu anotasyon Spring'e der ki:                                  ║
 * ║  "Bu sınıf veritabanı işlemleri yapan bir bileşendir,           ║
 * ║   onu yönet ve gerektiğinde kullanıma sun!"                     ║
 * ║                                                                  ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    // ──────────────────────────────────────────────────────────
    // 🎁 BONUS: Custom Query Metotları
    //
    // Spring Data JPA'nın süper gücü: Metot adından SQL üretir!
    // Metot adını belirli bir kurala göre yazarsan, Spring
    // otomatik olarak SQL sorgusunu oluşturur.
    //
    // Kural: findBy + AlanAdı
    //
    // Örnek: findByName(String name)
    // → SELECT * FROM departments WHERE name = ?
    //
    // Bu metodu sadece tanımlıyorsun, içini YAZMIYORSUN!
    // Spring senin yerine implement ediyor. 🤯
    // ──────────────────────────────────────────────────────────

    // İsme göre bölüm bul
    // Spring bu metot adını okuyup otomatik SQL üretir:
    // SELECT * FROM departments WHERE name = ?
    Department findByName(String name);

    // Bu isimde bir bölüm var mı?
    // SELECT COUNT(*) > 0 FROM departments WHERE name = ?
    boolean existsByName(String name);

    @org.springframework.data.jpa.repository.Query("SELECT d FROM Department d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    org.springframework.data.domain.Page<Department> searchDepartments(@org.springframework.data.repository.query.Param("query") String query, org.springframework.data.domain.Pageable pageable);
}
