package com.hospital.appointmentsystem.exception;

/**
 * İş kuralı ihlallerinde fırlatılan domain-specific exception.
 *
 * <p>Bu exception, doğrudan bir kaynak bulunamama durumu (404) değil,
 * iş mantığına aykırı bir işlem yapılmaya çalışıldığında kullanılır.</p>
 *
 * <h3>Kullanım Örnekleri:</h3>
 * <pre>{@code
 * // Gelecekteki randevu tamamlandı olarak işaretlenemez
 * throw new BusinessRuleException(
 *     "Gelecekteki bir randevu 'Tamamlandı' olarak işaretlenemez!"
 * );
 *
 * // Randevu saati dolu
 * throw new BusinessRuleException(
 *     "Seçilen randevu saati dolu veya mesai saatleri dışında!"
 * );
 * }</pre>
 *
 * <p>GlobalExceptionHandler bu exception'ı yakalayıp
 * <b>422 Unprocessable Entity</b> döner.</p>
 *
 * @see GlobalExceptionHandler
 */
public class BusinessRuleException extends RuntimeException {

    /**
     * @param message İş kuralı ihlalini açıklayan mesaj
     */
    public BusinessRuleException(String message) {
        super(message);
    }

    /**
     * @param message İş kuralı ihlalini açıklayan mesaj
     * @param cause   Bu hataya neden olan asıl exception
     */
    public BusinessRuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
