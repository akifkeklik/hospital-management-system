package com.hospital.appointmentsystem.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 🛡️ GLOBAL EXCEPTION HANDLER
 * 
 * Bu sınıf, uygulamada fırlatılan tüm hataları (Exceptions) merkezi bir 
 * noktadan yakalar ve Frontend'e (veya kullanıcıya) karmaşık Java hataları 
 * yerine temiz, anlaşılır JSON mesajları döner.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * @Valid anotasyonu ile yakalanan doğrulama (Validation) hatalarını işler.
     * Örneğin: Boş bırakılan isim, hatalı TC numarası vb.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Error");
        
        // Sadece hatalı alanları ve mesajları bir map'e doldur
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        
        response.put("details", errors);
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Optimistic Locking hatasını yakalar. (Aynı randevunun aynı anda alınması)
     */
    @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, Object>> handleOptimisticLockingException(org.springframework.orm.ObjectOptimisticLockingFailureException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.CONFLICT.value());
        response.put("error", "Conflict");
        response.put("message", "Sistem çakışması tespit edildi! Bu randevu slotu saniyeler önce başka bir hasta tarafından alınmış veya işlem güncellenmiş olabilir. Lütfen sayfayı yenileyip tekrar deneyin.");
        
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    /**
     * Veritabanı bütünlük hatalarını (ör: Duplicate Key / Unique Constraint) yakalar.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.CONFLICT.value());
        response.put("error", "Conflict");
        
        String message = "Veritabanı kural ihlali (Mükerrer Kayıt). Aynı TC Kimlik No veya E-posta ile zaten bir kayıt bulunuyor olabilir.";
        
        // Postgres mesajını güzelleştir
        if (ex.getCause() != null && ex.getCause().getCause() != null) {
            String dbMsg = ex.getCause().getCause().getMessage();
            if (dbMsg.contains("duplicate key value") || dbMsg.contains("Unique constraint")) {
                message = "Girdiğiniz bilgilere ait sistemde kayıtlı başka bir kullanıcı zaten var (Mükerrer TC veya E-posta). Lütfen kontrol edip tekrar deneyiniz.";
            }
        }
        
        response.put("message", message);
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    /**
     * Diğer tüm beklenmedik hataları yakalar (Fallback).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", "Internal Server Error");
        response.put("message", ex.getMessage());
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
