package com.hospital.appointmentsystem.appointment.impl;

import com.hospital.appointmentsystem.doctor.impl.Doctor;
import com.hospital.appointmentsystem.patient.impl.Patient;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║  📅 APPOINTMENT ENTITY — Randevu Tablosu (EN KARMAŞIK ENTITY!) ║
 * ╠══════════════════════════════════════════════════════════════════╣
 * ║                                                                  ║
 * ║  🆕 YENİ KAVRAMLAR:                                             ║
 * ║                                                                  ║
 * ║  1. ÇOKLU @ManyToOne İLİŞKİ                                    ║
 * ║     → Randevu hem Hasta'ya hem Doktor'a bağlı                  ║
 * ║     → İki tane foreign key var!                                 ║
 * ║                                                                  ║
 * ║  2. @Enumerated                                                  ║
 * ║     → Enum tipini veritabanında saklamak için kullanılır         ║
 * ║                                                                  ║
 * ║  3. LocalDateTime                                                ║
 * ║     → Tarih ve saat bilgisini tutar                              ║
 * ║                                                                  ║
 * ║  Veritabanında oluşacak tablo:                                   ║
 * ║  ┌────┬────────────┬───────────┬─────────────────┬────────┐     ║
 * ║  │ id │ patient_id │ doctor_id │ appointment_date│ status │     ║
 * ║  ├────┼────────────┼───────────┼─────────────────┼────────┤     ║
 * ║  │ 1  │ 1 (Ahmet)  │ 2 (Dr.X)  │ 2026-07-15 10:00│SCHEDULED│   ║
 * ║  │ 2  │ 1 (Ahmet)  │ 3 (Dr.Y)  │ 2026-07-20 14:30│COMPLETED│   ║
 * ║  └────┴────────────┴───────────┴─────────────────┴────────┘     ║
 * ║                                                                  ║
 * ║  Dikkat: Bir hasta birden fazla randevu alabilir!               ║
 * ║  Bir doktorun birden fazla randevusu olabilir!                  ║
 * ║                                                                  ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.Version;

@Entity
@Table(name = "appointments", indexes = {
    @Index(name = "IDX_APPOINTMENT_DOC_STAT_DATE", columnList = "doctor_id, status, appointment_date")
})
@SQLDelete(sql = "UPDATE appointments SET is_active = false WHERE id=? AND version=?")
@SQLRestriction("is_active = true")
@EntityListeners(AuditingEntityListener.class)
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ══════════════════════════════════════════════════════════
    //  ⭐ ÇOKLU İLİŞKİLER — İki tane @ManyToOne
    // ══════════════════════════════════════════════════════════

    // ──────────────────────────────────────────────────────────
    // 📌 İLİŞKİ 1: Randevu → Hasta (Many-to-One)
    // Birçok randevu, bir hastaya ait olabilir
    // (Bir hasta birden fazla randevu alabilir)
    // ──────────────────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    // ──────────────────────────────────────────────────────────
    // 📌 İLİŞKİ 2: Randevu → Doktor (Many-to-One)
    // Birçok randevu, bir doktora ait olabilir
    // (Bir doktorun birden fazla randevusu olabilir)
    // ──────────────────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    // ──────────────────────────────────────────────────────────
    // 📌 LocalDateTime — Tarih ve Saat
    //
    // Java'da tarih/saat için LocalDateTime kullanılır.
    // Örnek: 2026-07-15T10:30:00 → 15 Temmuz 2026, saat 10:30
    //
    // Veritabanında TIMESTAMP olarak saklanır.
    // ──────────────────────────────────────────────────────────
    @Column(name = "appointment_date", nullable = false)
    private LocalDateTime appointmentDate;

    // ──────────────────────────────────────────────────────────
    // 📌 @Enumerated(EnumType.STRING)
    //
    // Enum'u veritabanında NASIL saklayacağını belirler:
    //
    // EnumType.STRING  → "SCHEDULED", "COMPLETED" gibi METİN olarak
    //                    (Önerilen! Okunabilir ve güvenli)
    //
    // EnumType.ORDINAL → 0, 1, 2, 3 gibi SAYI olarak
    //                    (Tehlikeli! Enum sırası değişirse veri bozulur)
    //
    // Örnek:
    // STRING  → veritabanında "SCHEDULED" yazar
    // ORDINAL → veritabanında "0" yazar (anlaşılmaz!)
    // ──────────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private AppointmentStatus status;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // --- Enterprise Audit & Version Fields ---
    @Version
    @Column(name = "version")
    private Long version;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;

    // JPA için ZORUNLU
    public Appointment() {
    }

    // ── Getter ve Setter ──

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public LocalDateTime getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDateTime appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    // ══════════════════════════════════════════════════════════
    //  ⭐ CONCURRENCY PROTECTION (DOUBLE BOOKING PREVENTER)
    // ══════════════════════════════════════════════════════════
    
    @Column(name = "active_slot_id", unique = true, length = 100)
    private String activeSlotId;
    
    public String getActiveSlotId() {
        return activeSlotId;
    }
    
    @PrePersist
    @PreUpdate
    private void updateActiveSlotId() {
        if (this.isActive != null && this.isActive && this.doctor != null && this.appointmentDate != null) {
            if (this.status == AppointmentStatus.SCHEDULED || 
                this.status == AppointmentStatus.ARRIVED || 
                this.status == AppointmentStatus.IN_EXAMINATION) {
                // Ensure strict formatting (e.g., 2026-08-23T14:30) to avoid seconds discrepancy
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
                this.activeSlotId = this.doctor.getId() + "_" + this.appointmentDate.format(formatter);
            } else {
                // COMPLETED, CANCELLED, NO_SHOW free up the slot uniqueness constraint
                this.activeSlotId = null;
            }
        } else {
            this.activeSlotId = null;
        }
    }
}
