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
            com.hospital.appointmentsystem.doctor.api.DoctorService doctorService) {
        
        return args -> {
            // 1. Varsayılan Admin Kullanıcısı Oluşturma
            if (!userService.existsByUsername("admin")) {
                userService.registerUser("admin", "admin@hospital.com", "admin123", "ROLE_ADMIN", null);
                System.out.println("✅ Varsayılan Sistem Yöneticisi (Admin) oluşturuldu. Kullanıcı: admin | Şifre: admin123");
            }

            // 2. Varsayılan Bölüm ve Polikliniklerin Eklenmesi
            System.out.println("⏳ Varsayılan Ana Bilim Dalları kontrol ediliyor...");
            
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

            for (int i = 0; i < defaultDepartments.length; i++) {
                String deptName = defaultDepartments[i];
                
                // Bölüm zaten var mı kontrol et
                boolean exists = departmentRepository.findAll().stream()
                    .anyMatch(d -> d.getName().equals(deptName));
                    
                if (!exists) {
                    // Bölümü kaydet
                    com.hospital.appointmentsystem.department.impl.Department dept = 
                        new com.hospital.appointmentsystem.department.impl.Department(deptName, deptName + " Ana Bilim Dalı");
                    departmentRepository.save(dept);
                    
                    // Her bölüme 2 adet Poliklinik (Oda) bağla
                    com.hospital.appointmentsystem.polyclinic.impl.Polyclinic poly1 = 
                        new com.hospital.appointmentsystem.polyclinic.impl.Polyclinic(deptName + " 1. Poliklinik", "Kat " + (i+1) + " - Oda 1", dept.getId());
                    com.hospital.appointmentsystem.polyclinic.impl.Polyclinic poly2 = 
                        new com.hospital.appointmentsystem.polyclinic.impl.Polyclinic(deptName + " 2. Poliklinik", "Kat " + (i+1) + " - Oda 2", dept.getId());
                    
                    polyclinicRepository.save(poly1);
                    polyclinicRepository.save(poly2);
                    System.out.println("   + Eklendi: " + deptName + " (ve Poliklinikleri)");
                }
            }

            // 3. Varsayılan Doktorların Eklenmesi
            System.out.println("⏳ Örnek Doktorlar kontrol ediliyor...");
            String[] maleNames = {"Ahmet", "Mehmet", "Ali", "Can", "Burak", "Emre", "Hakan", "Volkan", "Mustafa", "Kemal"};
            String[] femaleNames = {"Ayşe", "Fatma", "Zeynep", "Elif", "Merve", "Büşra", "Ceren", "Derya", "Esra", "Gamze"};
            String[] surnames = {"Yılmaz", "Kaya", "Demir", "Çelik", "Şahin", "Yıldız", "Öztürk", "Aydın", "Özdemir", "Arslan"};
            java.util.Random rand = new java.util.Random();

            departmentRepository.findAll().forEach(dept -> {
                long doctorCountInDept = doctorRepository.findAll().stream()
                    .filter(d -> d.getDepartment() != null && d.getDepartment().getId().equals(dept.getId()))
                    .count();
                
                if (doctorCountInDept == 0) {
                    java.util.List<com.hospital.appointmentsystem.polyclinic.impl.Polyclinic> polys = polyclinicRepository.findByDepartmentId(dept.getId());
                    if (polys != null && !polys.isEmpty()) {
                        for(int j=0; j<2; j++) { // Her bölüme 2 doktor
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
                            doc.setPolyclinicId(polys.get(j % polys.size()).getId()); 
                            doc.setActive(true);
                            
                            doctorService.createDoctor(doc);
                        }
                        System.out.println("   + " + dept.getName() + " bölümüne 2 doktor atandı.");
                    }
                }
            });
            System.out.println("✅ Sistem veritabanı kurulumu tamamlandı!");
        };
    }
}
