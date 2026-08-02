package com.hospital.appointmentsystem.exception;

/**
 * Kaynak bulunamadığında fırlatılan domain-specific exception.
 *
 * <p>Bu exception, JPA'nın {@code EntityNotFoundException}'ı yerine
 * kullanılır. Böylece persistence katmanına olan bağımlılık kırılır
 * ve exception handling daha granüler hale gelir.</p>
 *
 * <h3>Kullanım Örnekleri:</h3>
 * <pre>{@code
 * // Kaynak adı + alan adı + alan değeri ile (önerilen)
 * throw new ResourceNotFoundException("Patient", "id", 42);
 * // Mesaj: "Patient not found with id: 42"
 *
 * // Doğrudan mesaj ile
 * throw new ResourceNotFoundException("Randevu bulunamadı! ID: " + id);
 * }</pre>
 *
 * <p>GlobalExceptionHandler bu exception'ı yakalayıp <b>404 Not Found</b> döner.</p>
 *
 * @see GlobalExceptionHandler
 */
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;

    /**
     * Structured constructor — Kaynak adı, alan adı ve alan değeri ile.
     * Otomatik olarak "{resourceName} not found with {fieldName}: {fieldValue}" mesajı üretir.
     *
     * @param resourceName Kaynak adı (ör: "Patient", "Doctor", "Appointment")
     * @param fieldName    Arama yapılan alan adı (ör: "id", "tcKimlikNo")
     * @param fieldValue   Arama yapılan değer (ör: 42, "12345678901")
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: %s", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    /**
     * Simple message constructor — Doğrudan hata mesajı ile.
     *
     * @param message Hata mesajı
     */
    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceName = null;
        this.fieldName = null;
        this.fieldValue = null;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }
}
