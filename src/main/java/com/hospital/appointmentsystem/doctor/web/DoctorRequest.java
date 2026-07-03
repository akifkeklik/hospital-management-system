package com.hospital.appointmentsystem.doctor.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 📥 Doctor Request — Doktor oluşturma/güncelleme isteği.
 *
 * ⭐ departmentId alanına dikkat!
 * İstemci doktor oluştururken, hangi bölüme ait olduğunu
 * departmentId ile belirtir.
 *
 * Örnek JSON:
 * {
 *   "firstName": "Ahmet",
 *   "lastName": "Yılmaz",
 *   "specialization": "Cardiologist",
 *   "phoneNumber": "05551234567",
 *   "email": "ahmet@hospital.com",
 *   "departmentId": 1
 * }
 */
public class DoctorRequest {

    @NotBlank(message = "Ad alanı zorunludur")
    @Size(max = 50, message = "Ad en fazla 50 karakter olabilir")
    private String firstName;

    @NotBlank(message = "Soyad alanı zorunludur")
    @Size(max = 50, message = "Soyad en fazla 50 karakter olabilir")
    private String lastName;

    @NotBlank(message = "Uzmanlık alanı zorunludur")
    @Size(max = 100, message = "Uzmanlık alanı en fazla 100 karakter olabilir")
    private String specialization;

    @Size(max = 15, message = "Telefon numarası en fazla 15 karakter olabilir")
    private String phoneNumber;

    @Email(message = "Geçerli bir e-posta adresi giriniz")
    @Size(max = 100, message = "E-posta adresi çok uzun")
    private String email;

    @NotNull(message = "Bölüm seçimi zorunludur")
    private Long departmentId; // ⭐ Hangi bölüme ait?

    private Long polyclinicId;

    public DoctorRequest() {
    }

    // ── Getter ve Setter ──

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

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public Long getPolyclinicId() {
        return polyclinicId;
    }

    public void setPolyclinicId(Long polyclinicId) {
        this.polyclinicId = polyclinicId;
    }
}
