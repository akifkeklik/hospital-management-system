package com.hospital.appointmentsystem.appointment;

import com.hospital.appointmentsystem.appointment.api.AppointmentDto;
import com.hospital.appointmentsystem.appointment.api.AppointmentService;
import com.hospital.appointmentsystem.doctor.impl.Doctor;
import com.hospital.appointmentsystem.doctor.impl.DoctorRepository;
import com.hospital.appointmentsystem.patient.impl.Patient;
import com.hospital.appointmentsystem.patient.impl.PatientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class AppointmentConcurrencyTest {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Test
    public void testDoubleBookingConcurrency() throws InterruptedException {
        // Setup existing data (assuming seeded DB has Doctor 1 and Patient 1, 2, 3, 4, 5)
        // If not, we will just use 1 and catch whatever happens.
        Long doctorId = 1L;
        Long[] patientIds = {1L, 2L, 3L, 4L, 5L}; // 5 different patients competing
        
        // Generate a random hour/day to ensure a fresh slot every time the test runs
        int randomDay = 5 + (int)(Math.random() * 100);
        int fixedHour = 10; // Guaranteed valid hour (not lunch break, within working hours)
        LocalDateTime targetTime = LocalDateTime.now().plusDays(randomDay).withHour(fixedHour).withMinute(0).withSecond(0).withNano(0);
        
        int numThreads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numThreads);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);
        
        List<Future<Void>> futures = new ArrayList<>();
        
        for (int i = 0; i < numThreads; i++) {
            final Long pId = patientIds[i];
            futures.add(executor.submit(() -> {
                try {
                    latch.await(); // Wait for all threads to be ready
                    
                    AppointmentDto dto = new AppointmentDto();
                    dto.setDoctorId(doctorId);
                    dto.setPatientId(pId);
                    dto.setAppointmentDate(targetTime);
                    dto.setNotes("Concurrency Test");
                    
                    appointmentService.createAppointment(dto);
                    successCount.incrementAndGet();
                } catch (DataIntegrityViolationException e) {
                    // Veritabanı unique constraint yakaladı!
                    conflictCount.incrementAndGet();
                } catch (Exception e) {
                    if (e.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
                        conflictCount.incrementAndGet();
                    } else if (e.getMessage() != null && e.getMessage().contains("dolu")) {
                        // Eğer SELECT bazlı validasyon yakalarsa o da conflict sayılır ama amacımız DB'nin yakalaması
                        conflictCount.incrementAndGet();
                    } else {
                        e.printStackTrace();
                        conflictCount.incrementAndGet();
                    }
                } finally {
                    doneLatch.countDown();
                }
                return null;
            }));
        }
        
        // Start all threads simultaneously
        latch.countDown();
        doneLatch.await();
        
        executor.shutdown();
        
        System.out.println("==================================================");
        System.out.println("SUCCESS COUNT: " + successCount.get());
        System.out.println("CONFLICT COUNT: " + conflictCount.get());
        System.out.println("==================================================");
        
        // Sadece 1 tanesi başarılı olmalı
        assertEquals(1, successCount.get(), "Only 1 appointment should succeed in double-booking scenario");
        assertEquals(4, conflictCount.get(), "Exactly 4 appointments should be rejected due to conflict");
    }
}
