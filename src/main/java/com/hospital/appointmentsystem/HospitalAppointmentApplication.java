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
            com.hospital.appointmentsystem.polyclinic.impl.PolyclinicRepository polyclinicRepository) {
        
        return args -> {
            // 1. Varsayılan Admin Kullanıcısı Oluşturma
            if (!userService.existsByUsername("admin")) {
                userService.registerUser("admin", "admin@hospital.com", "admin123", "ROLE_ADMIN", null);
                System.out.println("✅ Varsayılan Sistem Yöneticisi (Admin) oluşturuldu. Kullanıcı: admin | Şifre: admin123");
            }

            // 2. Varsayılan Bölüm ve Polikliniklerin Eklenmesi (Eğer boşsa)
            if (departmentRepository.count() == 0) {
                System.out.println("⏳ Veritabanı boş! Varsayılan Ana Bilim Dalları ve Poliklinikler oluşturuluyor...");
                
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
                    
                    // Bölümü kaydet
                    com.hospital.appointmentsystem.department.impl.Department dept = 
                        new com.hospital.appointmentsystem.department.impl.Department(deptName, deptName + " Ana Bilim Dalı");
                    dept = departmentRepository.save(dept);
                    
                    // Her bölüme 2 adet Poliklinik (Oda) bağla
                    com.hospital.appointmentsystem.polyclinic.impl.Polyclinic poly1 = 
                        new com.hospital.appointmentsystem.polyclinic.impl.Polyclinic(deptName + " 1. Poliklinik", "Kat " + (i+1) + " - Oda 1", dept.getId());
                    com.hospital.appointmentsystem.polyclinic.impl.Polyclinic poly2 = 
                        new com.hospital.appointmentsystem.polyclinic.impl.Polyclinic(deptName + " 2. Poliklinik", "Kat " + (i+1) + " - Oda 2", dept.getId());
                    
                    polyclinicRepository.save(poly1);
                    polyclinicRepository.save(poly2);
                }
                
                System.out.println("✅ Varsayılan Bölümler ve Poliklinikler başarıyla sisteme yüklendi!");
            }
        };
    }
}
