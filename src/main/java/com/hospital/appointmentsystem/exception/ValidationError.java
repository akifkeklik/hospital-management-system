package com.hospital.appointmentsystem.exception;

/**
 * Alan bazlı doğrulama hatası detaylarını taşıyan immutable DTO.
 *
 * <p>Bu sınıf, {@code @Valid} anotasyonu ile tetiklenen doğrulama hatalarının
 * her bir alanını ayrı ayrı temsil eder. Frontend tarafı bu bilgileri
 * kullanarak ilgili form alanlarının altına özel hata mesajları gösterebilir.</p>
 *
 * <h3>Örnek JSON Çıktısı:</h3>
 * <pre>{@code
 * {
 *   "field": "tcKimlikNo",
 *   "rejectedValue": null,
 *   "message": "TC Kimlik No 11 haneli olmalıdır"
 * }
 * }</pre>
 *
 * @see ApiErrorResponse#getValidationErrors()
 */
public final class ValidationError {

    private final String field;
    private final Object rejectedValue;
    private final String message;

    /**
     * @param field         Doğrulama hatası olan alan adı (ör: "email", "tcKimlikNo")
     * @param rejectedValue Intentionally null so sensitive or personal input is not echoed
     * @param message       Kullanıcıya gösterilecek hata mesajı
     */
    public ValidationError(String field, Object rejectedValue, String message) {
        this.field = field;
        this.rejectedValue = rejectedValue;
        this.message = message;
    }

    public String getField() {
        return field;
    }

    public Object getRejectedValue() {
        return rejectedValue;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ValidationError{" +
                "field='" + field + '\'' +
                ", rejectedValue=" + rejectedValue +
                ", message='" + message + '\'' +
                '}';
    }
}
