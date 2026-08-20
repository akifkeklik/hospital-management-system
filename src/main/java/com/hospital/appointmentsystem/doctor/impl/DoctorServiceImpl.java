package com.hospital.appointmentsystem.doctor.impl;

import com.hospital.appointmentsystem.department.impl.Department;
import com.hospital.appointmentsystem.department.impl.DepartmentRepository;
import com.hospital.appointmentsystem.doctor.api.DoctorDto;
import com.hospital.appointmentsystem.doctor.api.DoctorService;
import com.hospital.appointmentsystem.exception.ResourceNotFoundException;
import com.hospital.appointmentsystem.polyclinic.impl.Polyclinic;
import com.hospital.appointmentsystem.polyclinic.impl.PolyclinicRepository;
import com.hospital.appointmentsystem.user.api.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║  ⚙️ DOCTOR SERVICE IMPL — İlişkili Servis İmplementasyonu       ║
 * ╠══════════════════════════════════════════════════════════════════╣
 * ║                                                                  ║
 * ║  🆕 YENİ KAVRAM: BİRDEN FAZLA REPOSITORY KULLANMAK             ║
 * ║                                                                  ║
 * ║  Doctor, Department'a bağlı olduğu için bu serviste             ║
 * ║  HEM DoctorRepository HEM DepartmentRepository kullanıyoruz.    ║
 * ║                                                                  ║
 * ║  Neden?                                                          ║
 * ║  → Doktor oluşturulurken, bağlı olacağı bölümü veritabanından  ║
 * ║    çekip doktora bağlamamız gerekiyor.                          ║
 * ║  → İstemci bize sadece departmentId gönderiyor (sayı).          ║
 * ║  → Biz o ID ile Department objesini bulup Doctor'a atıyoruz.   ║
 * ║                                                                  ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */
@Service
public class DoctorServiceImpl implements DoctorService {

    // ⭐ İKİ repository — çünkü iki tabloyla işimiz var
    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final PolyclinicRepository polyclinicRepository;
    private final UserService userService;

    public DoctorServiceImpl(DoctorRepository doctorRepository,
                             DepartmentRepository departmentRepository,
                             PolyclinicRepository polyclinicRepository,
                             UserService userService) {
        this.doctorRepository = doctorRepository;
        this.departmentRepository = departmentRepository;
        this.polyclinicRepository = polyclinicRepository;
        this.userService = userService;
    }

    @Override
    @Transactional
    @CacheEvict(value = "doctors", allEntries = true)
    public DoctorDto createDoctor(DoctorDto doctorDto) {

        // ⭐ İLİŞKİ KURMA AŞAMASI
        // 1. İstemci bize departmentId gönderdi (örneğin: 1)
        // 2. Bu ID ile Department objesini veritabanından çekiyoruz
        // 3. Department objesini Doctor'a bağlıyoruz
        Department department = departmentRepository.findById(doctorDto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department", "id", doctorDto.getDepartmentId()
                ));

        Polyclinic polyclinic = null;
        if (doctorDto.getPolyclinicId() != null) {
            polyclinic = polyclinicRepository.findById(doctorDto.getPolyclinicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Polyclinic", "id", doctorDto.getPolyclinicId()));
        }

        // DTO → Entity dönüşümü + Department bağlama
        Doctor doctor = mapToEntity(doctorDto);
        doctor.setDepartment(department); // ⭐ İlişkiyi kuruyoruz!
        doctor.setPolyclinic(polyclinic);

        Doctor savedDoctor = doctorRepository.save(doctor);

        // ⭐ Doktor için otomatik Kullanıcı hesabı oluştur
        // Şifre olarak TC'nin ilk 6 hanesini varsayılan olarak veriyoruz
        String tc = doctorDto.getTcIdentityNumber();
        String defaultPassword = tc != null && tc.length() >= 6 ? tc.substring(0, 6) : "123456";
        userService.registerUser(tc, doctorDto.getEmail(), defaultPassword, "ROLE_DOCTOR", savedDoctor.getId());
        userService.setNeedsPasswordChange(tc, true);

        return mapToDto(savedDoctor);
    }

    @Override
    @Cacheable(value = "doctors", key = "#id")
    public DoctorDto getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", id));
        return mapToDto(doctor);
    }

    @Override
    public Page<DoctorDto> getAllDoctors(Pageable pageable) {
        Page<Doctor> doctors = doctorRepository.findAll(pageable);
        return doctors.map(this::mapToDto);
    }

    @Override
    @Cacheable(value = "doctors", key = "'dept_' + #departmentId")
    public List<DoctorDto> getDoctorsByDepartmentId(Long departmentId) {
        // Önce bölümün var olduğunu kontrol et
        if (!departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Department", "id", departmentId);
        }

        return doctorRepository.findByDepartmentId(departmentId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "doctors", allEntries = true)
    public DoctorDto updateDoctor(Long id, DoctorDto doctorDto) {
        Doctor existingDoctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", id));

        // Bölüm değişmiş olabilir, yeni bölümü çek
        Department department = departmentRepository.findById(doctorDto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department", "id", doctorDto.getDepartmentId()
                ));

        Polyclinic polyclinic = null;
        if (doctorDto.getPolyclinicId() != null) {
            polyclinic = polyclinicRepository.findById(doctorDto.getPolyclinicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Polyclinic", "id", doctorDto.getPolyclinicId()));
        }

        existingDoctor.setFirstName(doctorDto.getFirstName());
        existingDoctor.setLastName(doctorDto.getLastName());
        existingDoctor.setSpecialization(doctorDto.getSpecialization());
        existingDoctor.setPhoneNumber(doctorDto.getPhoneNumber());
        existingDoctor.setEmail(doctorDto.getEmail());
        existingDoctor.setDepartment(department);
        existingDoctor.setPolyclinic(polyclinic);

        Doctor updatedDoctor = doctorRepository.save(existingDoctor);
        return mapToDto(updatedDoctor);
    }

    @Override
    @Transactional
    @CacheEvict(value = "doctors", allEntries = true)
    public void deleteDoctor(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", id));
        doctorRepository.deleteById(id);
    }

    // ── Dönüşüm Metotları ──

    private DoctorDto mapToDto(Doctor doctor) {
        DoctorDto dto = new DoctorDto();
        dto.setId(doctor.getId());
        dto.setFirstName(doctor.getFirstName());
        dto.setLastName(doctor.getLastName());
        dto.setTcIdentityNumber(doctor.getTcIdentityNumber());
        dto.setSpecialization(doctor.getSpecialization());
        dto.setPhoneNumber(doctor.getPhoneNumber());
        dto.setEmail(doctor.getEmail());

        // ⭐ İlişkili entity'den bilgi alma
        // doctor.getDepartment() → Department objesini verir

        if (doctor.getDepartment() != null) {
            dto.setDepartmentId(doctor.getDepartment().getId());
            dto.setDepartmentName(doctor.getDepartment().getName());
        }

        if (doctor.getPolyclinic() != null) {
            dto.setPolyclinicId(doctor.getPolyclinic().getId());
            dto.setPolyclinicName(doctor.getPolyclinic().getName());
        }

        return dto;
    }

    private Doctor mapToEntity(DoctorDto dto) {
        Doctor doctor = new Doctor();
        doctor.setFirstName(dto.getFirstName());
        doctor.setLastName(dto.getLastName());
        doctor.setTcIdentityNumber(dto.getTcIdentityNumber());
        doctor.setSpecialization(dto.getSpecialization());
        doctor.setPhoneNumber(dto.getPhoneNumber());
        doctor.setEmail(dto.getEmail());
        // Department burada SET EDİLMEZ — createDoctor/updateDoctor'da yapılır
        return doctor;
    }
}
