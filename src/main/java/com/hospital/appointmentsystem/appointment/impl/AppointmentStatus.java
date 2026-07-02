package com.hospital.appointmentsystem.appointment.impl;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║  📊 APPOINTMENT STATUS ENUM — Randevu Durum Değerleri           ║
 * ╠══════════════════════════════════════════════════════════════════╣
 * ║                                                                  ║
 * ║  📌 ENUM NEDİR?                                                 ║
 * ║  Enum (Enumeration), SABİT değerler kümesidir.                  ║
 * ║  Randevu durumu sadece belirli değerler alabilir:               ║
 * ║  - SCHEDULED  → Planlandı (randevu oluşturuldu)                 ║
 * ║  - COMPLETED  → Tamamlandı (hasta geldi, muayene oldu)          ║
 * ║  - CANCELLED  → İptal edildi                                    ║
 * ║  - NO_SHOW    → Hasta gelmedi                                   ║
 * ║                                                                  ║
 * ║  Neden String yerine Enum kullanıyoruz?                         ║
 * ║  → String olsa "scheduled", "Scheduled", "SCHEDULED"           ║
 * ║    hepsi farklı olur, hata riski yüksek                        ║
 * ║  → Enum ile sadece tanımlı değerler kullanılabilir              ║
 * ║  → Derleme zamanında (compile time) hata yakalar                ║
 * ║                                                                  ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */
public enum AppointmentStatus {

    SCHEDULED,    // Planlandı — randevu oluşturuldu, bekliyor
    ARRIVED,      // Hastaneye Geldi — Check-in yapıldı, bekleme salonunda
    IN_EXAMINATION, // Muayenede — Doktor hastayı içeri aldı
    COMPLETED,    // Tamamlandı — muayene bitti
    CANCELLED,    // İptal edildi — hasta veya doktor iptal etti
    NO_SHOW       // Gelmedi — hasta randevuya gelmedi
}
