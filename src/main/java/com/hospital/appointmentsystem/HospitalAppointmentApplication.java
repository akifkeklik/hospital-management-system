package com.hospital.appointmentsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import com.hospital.appointmentsystem.user.api.UserService;

import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class HospitalAppointmentApplication {

    public static void main(String[] args) {
        SpringApplication.run(HospitalAppointmentApplication.class, args);
    }


    @Bean
    public org.springframework.boot.CommandLineRunner initData(
            @org.springframework.beans.factory.annotation.Value("${admin.password:}") String adminPassword,
            @org.springframework.beans.factory.annotation.Value("${test.user.password:local_test_secret_2026}") String testUserPassword,
            org.springframework.core.env.Environment env,
            JdbcTemplate jdbcTemplate, 
            UserService userService,
            com.hospital.appointmentsystem.department.impl.DepartmentRepository departmentRepository,
            com.hospital.appointmentsystem.polyclinic.impl.PolyclinicRepository polyclinicRepository,
            com.hospital.appointmentsystem.doctor.impl.DoctorRepository doctorRepository,
            com.hospital.appointmentsystem.doctor.api.DoctorService doctorService,
            com.hospital.appointmentsystem.patient.api.PatientService patientService,
            com.hospital.appointmentsystem.appointment.api.AppointmentService appointmentService) {
        
        return args -> {
            // 1. Varsayılan Admin Kullanıcısı Oluşturma
            if (!userService.existsByUsername("admin")) {
                if (adminPassword == null || adminPassword.trim().isEmpty()) {
                    throw new IllegalArgumentException("CRITICAL SECURITY ERROR: ADMIN_PASSWORD is not set! The application cannot start safely without an admin password.");
                }
                userService.registerUser("admin", "admin@hospital.com", adminPassword, "ROLE_ADMIN", null);
                System.out.println("✅ Varsayılan Sistem Yöneticisi (Admin) oluşturuldu.");
            } else {
                if (adminPassword != null && !adminPassword.trim().isEmpty()) {
                    userService.changePassword("admin", adminPassword);
                    System.out.println("✅ Varsayılan Sistem Yöneticisi (Admin) şifresi güncellendi.");
                }
            }

            // 1.5. Hasta şifre sıfırlama (geçici)
            if (userService.existsByUsername("10964562766")) {
                userService.changePassword("10964562766", "123456");
                System.out.println("✅ Hasta (10964562766) şifresi '123456' olarak sıfırlandı.");
            }

            // 2. Varsayılan Verilerin Yüklenmesi (SADECE BİR KERE ÇALIŞIR)
            boolean isDevOrLocal = env.acceptsProfiles(org.springframework.core.env.Profiles.of("dev", "default", "test"));
            if (!isDevOrLocal) {
                System.out.println("ℹ️ Production environment detected. Skipping dummy data seeder and test users.");
                return;
            }

            // Sistemde henüz hiç bölüm yoksa, varsayılan verileri yüklüyoruz.
            if (departmentRepository.count() == 0) {
                System.out.println("⏳ İlk Kurulum: Varsayılan veriler yükleniyor...");
                
                String[] defaultDepartments = {
                    "İç Hastalıkları (Dahiliye)", 
                    "Kulak Burun Boğaz (KBB)", 
                    "Göz Hastalıkları", 
                    "Genel Cerrahi", 
                    "Kardiyoloji", 
                    "Nöroloji", 
                    "Ortopedi ve Travmatoloji",
                    "Çocuk Sağlığı ve Hastalıkları"
                };

                String[] maleNames = {"Ahmet", "Mehmet", "Ali", "Can", "Burak", "Emre", "Hakan", "Volkan", "Mustafa", "Kemal"};
                String[] femaleNames = {"Ayşe", "Fatma", "Zeynep", "Elif", "Merve", "Büşra", "Ceren", "Derya", "Esra", "Gamze"};
                String[] surnames = {"Yılmaz", "Kaya", "Demir", "Çelik", "Şahin", "Yıldız", "Öztürk", "Aydın", "Özdemir", "Arslan"};
                java.util.Random rand = new java.util.Random();

                for (int i = 0; i < defaultDepartments.length; i++) {
                    String deptName = defaultDepartments[i];
                    
                    // Bölüm Zaten Var mı (Admin önceden elle eklemiş olabilir)
                    boolean exists = departmentRepository.existsByName(deptName);
                        
                    if (!exists) {
                        // 1. Bölümü Kaydet
                        com.hospital.appointmentsystem.department.impl.Department dept = 
                            new com.hospital.appointmentsystem.department.impl.Department(deptName, deptName + " Ana Bilim Dalı");
                        departmentRepository.save(dept);
                        
                        // 2. Poliklinikleri Kaydet (Odalar)
                        com.hospital.appointmentsystem.polyclinic.impl.Polyclinic poly1 = 
                            new com.hospital.appointmentsystem.polyclinic.impl.Polyclinic(deptName + " 1. Poliklinik", "Kat " + (i+1) + " - Oda 1", dept.getId());
                        com.hospital.appointmentsystem.polyclinic.impl.Polyclinic poly2 = 
                            new com.hospital.appointmentsystem.polyclinic.impl.Polyclinic(deptName + " 2. Poliklinik", "Kat " + (i+1) + " - Oda 2", dept.getId());
                        
                        poly1 = polyclinicRepository.save(poly1);
                        poly2 = polyclinicRepository.save(poly2);
                        
                        // 3. Doktorları Kaydet
                        for(int j=1; j<=2; j++) {
                            boolean isMale = rand.nextBoolean();
                            String firstName = isMale ? maleNames[rand.nextInt(maleNames.length)] : femaleNames[rand.nextInt(femaleNames.length)];
                            String lastName = surnames[rand.nextInt(surnames.length)];
                            
                            String tc = "1" + String.format("%010d", Math.abs(rand.nextLong() % 10000000000L));
                            String phone = "05" + String.format("%09d", Math.abs(rand.nextInt(1000000000)));
                            
                            com.hospital.appointmentsystem.doctor.api.DoctorDto doc = new com.hospital.appointmentsystem.doctor.api.DoctorDto();
                            doc.setFirstName(firstName);
                            doc.setLastName(lastName);
                            doc.setTcIdentityNumber(tc);
                            doc.setSpecialization("Uzman Doktor");
                            doc.setPhoneNumber(phone);
                            doc.setEmail(firstName.toLowerCase() + "." + lastName.toLowerCase() + rand.nextInt(10000) + "@hospital.com");
                            doc.setDepartmentId(dept.getId());
                            doc.setPolyclinicId(j == 1 ? poly1.getId() : poly2.getId()); 
                            doc.setActive(true);
                            
                            doctorService.createDoctor(doc);
                        }
                        System.out.println("   + Kuruldu: " + deptName + " (Odalar ve Doktorlar dahil)");
                    }
                }
                
                // 4. Örnek Hastaları ve Randevuları Kaydet
                System.out.println("⏳ Örnek hastalar ve randevular oluşturuluyor...");
                java.util.List<com.hospital.appointmentsystem.patient.api.PatientDto> createdPatients = new java.util.ArrayList<>();
                for(int i=1; i<=6; i++) {
                    boolean isMale = rand.nextBoolean();
                    String firstName = isMale ? maleNames[rand.nextInt(maleNames.length)] : femaleNames[rand.nextInt(femaleNames.length)];
                    String lastName = surnames[rand.nextInt(surnames.length)];
                    String tc = "2" + String.format("%010d", Math.abs(rand.nextLong() % 10000000000L));
                    String phone = "05" + String.format("%09d", Math.abs(rand.nextInt(1000000000)));
                    
                    com.hospital.appointmentsystem.patient.api.PatientDto patientDto = new com.hospital.appointmentsystem.patient.api.PatientDto();
                    patientDto.setFirstName(firstName);
                    patientDto.setLastName(lastName);
                    patientDto.setTcIdentityNumber(tc);
                    patientDto.setPhoneNumber(phone);
                    patientDto.setEmail(firstName.toLowerCase() + "." + lastName.toLowerCase() + rand.nextInt(1000) + "@patient.com");
                    
                    try {
                        createdPatients.add(patientService.createPatient(patientDto));
                    } catch (Exception e) {
                        System.out.println("Hasta oluşturulamadı: " + e.getMessage());
                    }
                }
                
                // Doktorları çekip onlara randevu atayalım
                java.util.List<com.hospital.appointmentsystem.doctor.impl.Doctor> allDoctors = doctorRepository.findAll();
                if (!allDoctors.isEmpty() && !createdPatients.isEmpty()) {
                    for(int i=0; i<6; i++) {
                        com.hospital.appointmentsystem.patient.api.PatientDto p = createdPatients.get(i % createdPatients.size());
                        com.hospital.appointmentsystem.doctor.impl.Doctor d = allDoctors.get(i % allDoctors.size());
                        
                        com.hospital.appointmentsystem.appointment.api.AppointmentDto apptDto = new com.hospital.appointmentsystem.appointment.api.AppointmentDto();
                        apptDto.setPatientId(p.getId());
                        apptDto.setDoctorId(d.getId());
                        
                        // İleri bir tarih (yarın ile 10 gün sonrası arası)
                        int daysAhead = rand.nextInt(10) + 1;
                        java.time.LocalDate apptDate = java.time.LocalDate.now().plusDays(daysAhead);
                        
                        // Uygun saatleri çek
                        java.util.List<String> availableSlots = appointmentService.getAvailableSlots(d.getId(), apptDate);
                        if (!availableSlots.isEmpty()) {
                            String slot = availableSlots.get(rand.nextInt(availableSlots.size()));
                            String[] timeParts = slot.split(":");
                            java.time.LocalTime apptTime = java.time.LocalTime.of(Integer.parseInt(timeParts[0]), Integer.parseInt(timeParts[1]));
                            apptDto.setAppointmentDate(java.time.LocalDateTime.of(apptDate, apptTime));
                            apptDto.setNotes("İlk kurulum otomatik randevusu.");
                            
                            try {
                                appointmentService.createAppointment(apptDto);
                            } catch (Exception e) {
                                System.out.println("Randevu oluşturulamadı: " + e.getMessage());
                            }
                        }
                    }
                    System.out.println("   + Kuruldu: Örnek Hastalar ve Randevular");
                }
                
                System.out.println("✅ İlk Kurulum tamamlandı! Veriler bir daha üzerine yazılmayacak.");
            } else {
                System.out.println("ℹ️ Sistem veritabanı zaten daha önce kurulmuş. Seeder atlandı.");
            }

            // 3. E2E Test Kullanıcıları (Idempotent)
            System.out.println("⏳ E2E Test Kullanıcıları kontrol ediliyor...");
            if (!userService.existsByUsername("88888888888")) {
                com.hospital.appointmentsystem.department.impl.Department dept = departmentRepository.findAll().stream().findFirst().orElse(null);
                com.hospital.appointmentsystem.polyclinic.impl.Polyclinic poly = polyclinicRepository.findAll().stream().findFirst().orElse(null);
                if (dept != null && poly != null) {
                    com.hospital.appointmentsystem.doctor.api.DoctorDto doc = new com.hospital.appointmentsystem.doctor.api.DoctorDto();
                    doc.setFirstName("Test");
                    doc.setLastName("Doctor");
                    doc.setTcIdentityNumber("88888888888");
                    doc.setSpecialization("Test Uzmanı");
                    doc.setPhoneNumber("05551111111");
                    doc.setEmail("testdoctor@hospital.com");
                    doc.setDepartmentId(dept.getId());
                    doc.setPolyclinicId(poly.getId());
                    doc.setActive(true);
                    doctorService.createDoctor(doc);
                    // Update password and clear needsPasswordChange since it's a known test user
                    userService.changePassword("88888888888", testUserPassword); 
                    System.out.println("   + Test Doktoru oluşturuldu: 88888888888");
                }
            }
            if (!userService.existsByUsername("99999999999")) {
                com.hospital.appointmentsystem.patient.api.PatientDto pat = new com.hospital.appointmentsystem.patient.api.PatientDto();
                pat.setFirstName("Test");
                pat.setLastName("Patient");
                pat.setTcIdentityNumber("99999999999");
                pat.setPhoneNumber("05552222222");
                pat.setEmail("testpatient@patient.com");
                com.hospital.appointmentsystem.patient.api.PatientDto savedPat = patientService.createPatient(pat);
                userService.registerUser("99999999999", "testpatient@patient.com", testUserPassword, "ROLE_PATIENT", savedPat.getId());
                System.out.println("   + Test Hastası oluşturuldu: 99999999999");
            }
        };
    }
}
