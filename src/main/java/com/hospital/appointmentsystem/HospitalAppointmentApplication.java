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
    public org.springframework.boot.CommandLineRunner initData(JdbcTemplate jdbcTemplate, UserService userService) {
        return args -> {
            // Varsayılan Admin Kullanıcısı Oluşturma
            if (!userService.existsByUsername("admin")) {
                userService.registerUser("admin", "admin@hospital.com", "admin123", "ROLE_ADMIN", null);
                System.out.println("✅ Varsayılan Sistem Yöneticisi (Admin) oluşturuldu. Kullanıcı: admin | Şifre: admin123");
            }
        };
    }
}
