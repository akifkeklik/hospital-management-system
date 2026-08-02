package com.hospital.appointmentsystem.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Enterprise-grade API Error Response DTO.
 *
 * <p>Tüm hata yanıtları için tek, tutarlı bir yapı sağlar. Bu DTO sayesinde
 * frontend tarafı her zaman aynı JSON formatını bekleyebilir ve hata
 * yönetimini standartlaştırabilir.</p>
 *
 * <h3>Mimari Kararlar:</h3>
 * <ul>
 *   <li><b>Immutable Design:</b> Tüm alanlar final'dır, setter yoktur.
 *       Thread-safety ve veri bütünlüğü garanti altındadır.</li>
 *   <li><b>Instant vs LocalDateTime:</b> Instant timezone-agnostic'tir ve
 *       ISO-8601 formatında serialize edilir. Dağıtık sistemlerde timezone
 *       karışıklığını önler.</li>
 *   <li><b>traceId:</b> Her hata yanıtında benzersiz bir UUID üretilir.
 *       Bu ID log'lara da yazılır, böylece destek ekibi bir hastanın
 *       gördüğü hatayı log'larda anında bulabilir.</li>
 *   <li><b>@JsonInclude(NON_NULL):</b> validationErrors gibi null alanlar
 *       JSON çıktısına dahil edilmez — temiz response.</li>
 * </ul>
 *
 * @see ValidationError
 * @see GlobalExceptionHandler
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ApiErrorResponse {

    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final String traceId;
    private final List<ValidationError> validationErrors;

    /**
     * Primary constructor — GlobalExceptionHandler tarafından kullanılır.
     *
     * @param status           HTTP status kodu (ör: 400, 404, 500)
     * @param error            HTTP status reason phrase (ör: "Not Found")
     * @param message          İnsan tarafından okunabilir hata mesajı
     * @param path             İsteğin yapıldığı URI
     * @param traceId          Benzersiz hata izleme kimliği (UUID)
     * @param validationErrors Alan bazlı doğrulama hataları (nullable)
     */
    public ApiErrorResponse(int status, String error, String message,
                            String path, String traceId,
                            List<ValidationError> validationErrors) {
        this.timestamp = Instant.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.traceId = traceId;
        this.validationErrors = validationErrors;
    }

    /**
     * Convenience constructor — validation hataları olmayan yanıtlar için.
     *
     * @param status  HTTP status kodu
     * @param error   HTTP status reason phrase
     * @param message Hata mesajı
     * @param path    İstek URI
     * @param traceId Hata izleme kimliği
     */
    public ApiErrorResponse(int status, String error, String message,
                            String path, String traceId) {
        this(status, error, message, path, traceId, null);
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public String getTraceId() {
        return traceId;
    }

    public List<ValidationError> getValidationErrors() {
        return validationErrors;
    }

    @Override
    public String toString() {
        return "ApiErrorResponse{" +
                "timestamp=" + timestamp +
                ", status=" + status +
                ", error='" + error + '\'' +
                ", message='" + message + '\'' +
                ", path='" + path + '\'' +
                ", traceId='" + traceId + '\'' +
                ", validationErrors=" + validationErrors +
                '}';
    }
}
