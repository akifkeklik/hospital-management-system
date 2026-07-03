package com.hospital.appointmentsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import com.hospital.appointmentsystem.user.api.UserService;

/**
 * ╔══════════════════════════════════════════════════════════════════╗
 * ║  🚀 ANA UYGULAMA SINIFI — Her Şey Buradan Başlar!              ║
 * ╠══════════════════════════════════════════════════════════════════╣
 * ║                                                                  ║
 * ║  Bu sınıf Spring Boot uygulamasının GİRİŞ NOKTASIDIR.          ║
 * ║  Java'daki main() metodu gibi düşün — program buradan başlar.   ║
 * ║                                                                  ║
 * ║  @SpringBootApplication anotasyonu 3 şeyi birleştirir:          ║
 * ║                                                                  ║
 * ║  1. @Configuration                                               ║
 * ║     → "Bu sınıf yapılandırma bilgisi içerir" der               ║
 * ║                                                                  ║
 * ║  2. @EnableAutoConfiguration                                     ║
 * ║     → "pom.xml'deki bağımlılıklara bakarak otomatik             ║
 * ║        yapılandırma yap" der                                     ║
 * ║     → Örneğin: H2 bağımlılığı var → veritabanı bağlantısını    ║
 * ║       otomatik ayarla                                            ║
 * ║                                                                  ║
 * ║  3. @ComponentScan                                               ║
 * ║     → "Bu paketin altındaki tüm alt paketleri tara ve           ║
 * ║        @Controller, @Service, @Repository gibi sınıfları bul"   ║
 * ║     → Yani com.hospital.appointmentsystem altındaki              ║
 * ║       department, patient, doctor, appointment paketlerinin      ║
 * ║       hepsini otomatik bulur!                                    ║
 * ║                                                                  ║
 * ╚══════════════════════════════════════════════════════════════════╝
 */
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class HospitalAppointmentApplication {

    public static void main(String[] args) {
        // SpringApplication.run() → Spring Boot'u başlatır
        // Bu metot:
        //   1. Gömülü Tomcat sunucusunu başlatır (port 8080)
        //   2. Veritabanı bağlantısını kurar
        //   3. Tüm @Controller, @Service, @Repository sınıflarını yükler
        //   4. JPA Entity'lerine bakarak veritabanı tablolarını oluşturur
        SpringApplication.run(HospitalAppointmentApplication.class, args);
    }

    @Bean
    public org.springframework.boot.CommandLineRunner initData(
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
                userService.registerUser("admin", "admin@hospital.com", "admin123", "ROLE_ADMIN", null);
                System.out.println("✅ Varsayılan Sistem Yöneticisi (Admin) oluşturuldu. Kullanıcı: admin | Şifre: admin123");
            }

            // 2. Varsayılan Verilerin Yüklenmesi (SADECE BİR KERE ÇALIŞIR)
            // Kullanıcı bu verileri sonradan silerse, sunucu yeniden başladığında tekrar geri GELMESİN diye flag kullanıyoruz.
            if (!userService.existsByUsername("system_seeded_flag")) {
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
                    boolean exists = departmentRepository.findAll().stream()
                        .anyMatch(d -> d.getName().equals(deptName));
                        
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
                
                // Kurulumun bir daha çalışmaması için flag user oluşturuyoruz
                userService.registerUser("system_seeded_flag", "seeded@system.local", "system_seeded_flag_pass", "ROLE_ADMIN", null);
                System.out.println("✅ İlk Kurulum tamamlandı! Veriler bir daha üzerine yazılmayacak.");
            } else {
                System.out.println("ℹ️ Sistem veritabanı zaten daha önce kurulmuş. Seeder atlandı.");
            }
        };
    }
}
