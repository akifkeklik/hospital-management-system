package com.hospital.appointmentsystem.appointment.impl;

import com.hospital.appointmentsystem.appointment.api.AppointmentDto;
import com.hospital.appointmentsystem.appointment.api.AppointmentService;
import com.hospital.appointmentsystem.appointment.api.WaitTimeDto;
import com.hospital.appointmentsystem.doctor.impl.Doctor;
import com.hospital.appointmentsystem.doctor.impl.DoctorRepository;
import com.hospital.appointmentsystem.exception.BusinessRuleException;
import com.hospital.appointmentsystem.exception.ResourceNotFoundException;
import com.hospital.appointmentsystem.patient.impl.Patient;
import com.hospital.appointmentsystem.patient.impl.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.hospital.appointmentsystem.setting.api.SystemSettingService;
import com.hospital.appointmentsystem.setting.api.SystemSettingDto;
import com.hospital.appointmentsystem.doctorleave.impl.DoctorLeaveRepository;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║  ⚙️ APPOINTMENT SERVICE IMPL — En Karmaşık Servis!              ║
 * ╠══════════════════════════════════════════════════════════════════╣
 * ║                                                                  ║
 * ║  Bu serviste 3 repository kullanıyoruz:                         ║
 * ║  - AppointmentRepository → Randevu işlemleri                    ║
 * ║  - PatientRepository     → Hasta bilgisini çekmek için         ║
 * ║  - DoctorRepository      → Doktor bilgisini çekmek için        ║
 * ║                                                                  ║
 * ║  Randevu oluştururken:                                           ║
 * ║  1. İstemci patientId ve doctorId gönderir                      ║
 * ║  2. Biz bu ID'lerle Patient ve Doctor objelerini buluruz        ║
 * ║  3. Appointment'a bağlarız                                      ║
 * ║                                                                  ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */
@Service
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final SystemSettingService systemSettingService;
    private final DoctorLeaveRepository doctorLeaveRepository;

    // ⭐ BEŞ repository/service enjeksiyonu
    public AppointmentServiceImpl(AppointmentRepository appointmentRepository,
                                  PatientRepository patientRepository,
                                  DoctorRepository doctorRepository,
                                  SystemSettingService systemSettingService,
                                  DoctorLeaveRepository doctorLeaveRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.systemSettingService = systemSettingService;
        this.doctorLeaveRepository = doctorLeaveRepository;
    }

    @Override
    @Transactional
    public AppointmentDto createAppointment(AppointmentDto appointmentDto) {

        // 1. Hastayı bul
        Patient patient = patientRepository.findById(appointmentDto.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient", "id", appointmentDto.getPatientId()
                ));

        // 2. Doktoru bul
        Doctor doctor = doctorRepository.findById(appointmentDto.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor", "id", appointmentDto.getDoctorId()
                ));

        // ⭐ Randevu saati uygunluk kontrolü
        LocalDate date = appointmentDto.getAppointmentDate().toLocalDate();
        LocalTime time = appointmentDto.getAppointmentDate().toLocalTime();
        
        List<String> availableSlots = getAvailableSlots(doctor.getId(), date);
        
        // saniyeleri atıp saat:dakika formatında kontrol et (örn: "09:15")
        String timeString = String.format("%02d:%02d", time.getHour(), time.getMinute());
        if (!availableSlots.contains(timeString)) {
            throw new BusinessRuleException("Seçilen randevu saati dolu veya mesai saatleri dışında!");
        }

        // 3. Randevu oluştur ve ilişkileri kur
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);                          // ⭐ Hasta ilişkisi
        appointment.setDoctor(doctor);                            // ⭐ Doktor ilişkisi
        appointment.setAppointmentDate(appointmentDto.getAppointmentDate());
        appointment.setStatus(AppointmentStatus.SCHEDULED);       // ⭐ Varsayılan durum
        appointment.setNotes(appointmentDto.getNotes());

        Appointment savedAppointment = appointmentRepository.save(appointment);
        return mapToDto(savedAppointment);
    }

    @Override
    public AppointmentDto getAppointmentById(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));
        return mapToDto(appointment);
    }

    @Override
    public Page<AppointmentDto> getAllAppointments(Pageable pageable) {
        Page<Appointment> appointments = appointmentRepository.findAll(pageable);
        return appointments.map(this::mapToDto);
    }

    @Override
    public Page<AppointmentDto> getAppointmentsByPatientId(Long patientId, List<AppointmentStatus> statuses, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient", "id", patientId);
        }
        Specification<Appointment> spec = AppointmentSpecification.filterBy(patientId, null, statuses, startDate, endDate);
        return appointmentRepository.findAll(spec, pageable).map(this::mapToDto);
    }

    @Override
    public Page<AppointmentDto> getAppointmentsByDoctorId(Long doctorId, List<AppointmentStatus> statuses, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Doctor", "id", doctorId);
        }
        Specification<Appointment> spec = AppointmentSpecification.filterBy(null, doctorId, statuses, startDate, endDate);
        return appointmentRepository.findAll(spec, pageable).map(this::mapToDto);
    }

    @Override
    public List<String> getAvailableSlots(Long doctorId, LocalDate date) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("Doctor", "id", doctorId);
        }

        // DOKTOR İZİN KONTROLÜ (Enterprise Feature)
        if (doctorLeaveRepository.isDoctorOnLeave(doctorId, date)) {
            return new ArrayList<>(); // Doktor izinliyse slot yok!
        }

        // SİSTEM AYARLARINI ÇEK
        SystemSettingDto settings = systemSettingService.getSettings();
        LocalTime workStart = LocalTime.parse(settings.getWorkStartTime());
        LocalTime workEnd = LocalTime.parse(settings.getWorkEndTime());
        LocalTime lunchStart = LocalTime.parse(settings.getLunchBreakStart());
        LocalTime lunchEnd = LocalTime.parse(settings.getLunchBreakEnd());
        int duration = settings.getAppointmentDuration();

        // Tüm günün randevularını çek
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        
        List<Appointment> existingAppointments = appointmentRepository
            .findByDoctorIdAndStatusAndAppointmentDateBetween(doctorId, AppointmentStatus.SCHEDULED, startOfDay, endOfDay);

        // Dolu saatlerin listesi
        List<LocalTime> bookedTimes = existingAppointments.stream()
            .map(app -> app.getAppointmentDate().toLocalTime())
            .collect(Collectors.toList());

        List<String> availableSlots = new ArrayList<>();
        LocalTime currentTime = workStart;

        while (currentTime.plusMinutes(duration).compareTo(workEnd) <= 0) {
            LocalTime slotEnd = currentTime.plusMinutes(duration);
            
            // Öğle arası kontrolü (Eğer mevcut slot tamamen öğle arası içindeyse veya taşıyorsa)
            // Kural: Slot'un başlangıcı lunchEnd'den önceyse VE Slot'un bitişi lunchStart'tan sonraysa -> Çakışma var
            boolean overlapsLunch = currentTime.isBefore(lunchEnd) && slotEnd.isAfter(lunchStart);

            if (!overlapsLunch) {
                // Eğer bu saatte randevu yoksa ve saat geçmişte değilse ekle
                if (!bookedTimes.contains(currentTime)) {
                    // Eğer bugün seçildiyse, geçmiş saatleri gösterme
                    if (!(date.isEqual(LocalDate.now()) && currentTime.isBefore(LocalTime.now()))) {
                        availableSlots.add(currentTime.toString());
                    }
                }
            }
            currentTime = currentTime.plusMinutes(duration);
        }

        return availableSlots;
    }

    @Override
    @Transactional
    public AppointmentDto updateAppointment(Long id, AppointmentDto appointmentDto) {
        Appointment existingAppointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));

        Patient patient = patientRepository.findById(appointmentDto.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient", "id", appointmentDto.getPatientId()
                ));

        Doctor doctor = doctorRepository.findById(appointmentDto.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor", "id", appointmentDto.getDoctorId()
                ));

        existingAppointment.setPatient(patient);
        existingAppointment.setDoctor(doctor);

        // ⭐ Randevu saati değiştiyse veya doktor değiştiyse uygunluk kontrolü yap
        boolean timeOrDoctorChanged = !existingAppointment.getAppointmentDate().equals(appointmentDto.getAppointmentDate()) || 
                                      !existingAppointment.getDoctor().getId().equals(doctor.getId());
                                      
        if (timeOrDoctorChanged) {
            LocalDate date = appointmentDto.getAppointmentDate().toLocalDate();
            LocalTime time = appointmentDto.getAppointmentDate().toLocalTime();
            
            List<String> availableSlots = getAvailableSlots(doctor.getId(), date);
            String timeString = String.format("%02d:%02d", time.getHour(), time.getMinute());
            
            if (!availableSlots.contains(timeString)) {
                throw new BusinessRuleException("Seçilen randevu saati dolu veya mesai saatleri dışında!");
            }
        }

        existingAppointment.setAppointmentDate(appointmentDto.getAppointmentDate());
        existingAppointment.setNotes(appointmentDto.getNotes());

        // Durum gönderildiyse güncelle
        if (appointmentDto.getStatus() != null) {
            AppointmentStatus newStatus = AppointmentStatus.valueOf(appointmentDto.getStatus());
            if ((newStatus == AppointmentStatus.COMPLETED || newStatus == AppointmentStatus.NO_SHOW) 
                && appointmentDto.getAppointmentDate().isAfter(LocalDateTime.now())) {
                throw new BusinessRuleException("Gelecekteki bir randevu 'Tamamlandı' veya 'Gelmedi' olarak işaretlenemez!");
            }
            existingAppointment.setStatus(newStatus);
        }

        Appointment updatedAppointment = appointmentRepository.save(existingAppointment);
        return mapToDto(updatedAppointment);
    }

    // ──────────────────────────────────────────────────────────
    // ⭐ DURUM GÜNCELLEME — Sadece durumu değiştir
    //
    // Randevuyu iptal etmek veya tamamlandı olarak işaretlemek
    // için tüm bilgileri göndermek yerine sadece durumu
    // güncelleyebilirsin.
    //
    // AppointmentStatus.valueOf("COMPLETED")
    // → String'i Enum'a çevirir
    // → Eğer geçersiz bir değer gelirse hata fırlatır
    // ──────────────────────────────────────────────────────────
    @Override
    @Transactional
    public AppointmentDto updateAppointmentStatus(Long id, String status) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));

        // String → Enum dönüşümü
        // "COMPLETED" → AppointmentStatus.COMPLETED
        try {
            AppointmentStatus newStatus = AppointmentStatus.valueOf(status.toUpperCase());
            
            // ⭐ İŞ KURALI: Gelecekteki bir randevu 'Tamamlandı' veya 'Gelmedi' yapılamaz
            if ((newStatus == AppointmentStatus.COMPLETED || newStatus == AppointmentStatus.NO_SHOW) 
                && appointment.getAppointmentDate().isAfter(LocalDateTime.now())) {
                throw new BusinessRuleException("Gelecekteki bir randevu 'Tamamlandı' veya 'Gelmedi' olarak işaretlenemez!");
            }
            
            appointment.setStatus(newStatus);
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException(
                    "Geçersiz randevu durumu: " + status +
                    ". Geçerli değerler: SCHEDULED, ARRIVED, IN_EXAMINATION, COMPLETED, CANCELLED, NO_SHOW"
            );
        }

        Appointment updatedAppointment = appointmentRepository.save(appointment);
        return mapToDto(updatedAppointment);
    }

    @Override
    @Transactional
    public void deleteAppointment(Long id) {
        appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));
        appointmentRepository.deleteById(id);
    }

    @Override
    public AppointmentDto cancelAppointment(Long id) {
        return updateAppointmentStatus(id, "CANCELLED");
    }

    // ── Dönüşüm Metotları ──

    private AppointmentDto mapToDto(Appointment appointment) {
        AppointmentDto dto = new AppointmentDto();
        dto.setId(appointment.getId());
        dto.setAppointmentDate(appointment.getAppointmentDate());
        dto.setStatus(appointment.getStatus().name()); // Enum → String
        dto.setNotes(appointment.getNotes());

        // ⭐ İlişkili entity'lerden bilgi çekme
        // Hasta bilgisi
        dto.setPatientId(appointment.getPatient().getId());
        dto.setPatientFullName(
                appointment.getPatient().getFirstName() + " " +
                appointment.getPatient().getLastName()
        );

        // Doktor bilgisi
        dto.setDoctorId(appointment.getDoctor().getId());
        dto.setDoctorFullName(
                appointment.getDoctor().getFirstName() + " " +
                appointment.getDoctor().getLastName()
        );

        // Doktorun bölümü
        dto.setDepartmentName(appointment.getDoctor().getDepartment().getName());

        return dto;
    }

    @Override
    public WaitTimeDto getEstimatedWaitTime(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", appointmentId));
        
        // Aynı doktorun o günkü randevularını al
        LocalDate appointmentDate = appointment.getAppointmentDate().toLocalDate();
        LocalDateTime dayStart = appointmentDate.atStartOfDay();
        LocalDateTime dayEnd = appointmentDate.atTime(23, 59);
        
        List<Appointment> sameDayAppointments = appointmentRepository
            .findByDoctorIdAndStatusAndAppointmentDateBetween(
                appointment.getDoctor().getId(), AppointmentStatus.SCHEDULED, dayStart, dayEnd);
        
        // Randevu saatine göre sırala
        sameDayAppointments.sort(java.util.Comparator.comparing(Appointment::getAppointmentDate));
        
        // Bu randevudan önceki randevu sayısını hesapla
        int queuePosition = 0;
        for (Appointment a : sameDayAppointments) {
            if (a.getAppointmentDate().isBefore(appointment.getAppointmentDate())) {
                queuePosition++;
            }
        }
        
        // Ortalama muayene süresi: 15 dakika
        int avgExamMinutes = 15;
        int estimatedMinutes = queuePosition * avgExamMinutes;
        
        // Şu an geçmiş olan süreyi çıkar
        LocalDateTime now = LocalDateTime.now();
        if (now.toLocalDate().equals(appointmentDate) && now.isBefore(appointment.getAppointmentDate())) {
            long minutesUntilAppointment = java.time.Duration.between(now, appointment.getAppointmentDate()).toMinutes();
            estimatedMinutes = (int) Math.max(minutesUntilAppointment, 0);
        }
        
        // Yoğunluk seviyesi
        String busyLevel;
        int totalAppointments = sameDayAppointments.size();
        if (totalAppointments <= 5) busyLevel = "LOW";
        else if (totalAppointments <= 12) busyLevel = "MEDIUM";
        else busyLevel = "HIGH";
        
        return new WaitTimeDto(estimatedMinutes, busyLevel, queuePosition + 1);
    }
}
