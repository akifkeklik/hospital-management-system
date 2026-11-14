package com.hospital.appointmentsystem.appointment.impl;

public enum AppointmentStatus {

    SCHEDULED,    // Planlandı — randevu oluşturuldu, bekliyor
    ARRIVED,      // Hastaneye Geldi — Check-in yapıldı, bekleme salonunda
    IN_EXAMINATION, // Muayenede — Doktor hastayı içeri aldı
    COMPLETED,    // Tamamlandı — muayene bitti
    CANCELLED,    // İptal edildi — hasta veya doktor iptal etti
    NO_SHOW       // Gelmedi — hasta randevuya gelmedi
}
