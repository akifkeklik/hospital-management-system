package com.hospital.appointmentsystem.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.MDC;

/**
 * Enterprise-grade Global Exception Handler.
 *
 * <p>Uygulamadaki tüm exception'ları merkezi bir noktadan yakalayıp
 * tutarlı, izlenebilir ve güvenli {@link ApiErrorResponse} JSON formatında döner.</p>
 *
 * <h3>Mimari Kararlar:</h3>
 * <ul>
 *   <li><b>@RestControllerAdvice:</b> {@code @ControllerAdvice + @ResponseBody}.
 *       Tüm yanıtlar otomatik olarak JSON'a serialize edilir.</li>
 *   <li><b>ResponseEntityExceptionHandler:</b> Spring MVC'nin standart
 *       exception'larını (405, 415, 400 vb.) override edebilmek için extend edilir.</li>
 *   <li><b>traceId:</b> Her hata yanıtında benzersiz UUID üretilir ve log'a yazılır.
 *       Destek ekibi, hastanın gördüğü hata kodunu kullanarak log'larda anında
 *       ilgili satırı bulabilir.</li>
 *   <li><b>Güvenli 500 Yanıtları:</b> Internal Server Error durumlarında gerçek
 *       exception mesajı asla kullanıcıya sızdırılmaz. Sadece traceId ile
 *       referans verilir.</li>
 *   <li><b>Handler Sırası:</b> Exception'lar en spesifikten en genele doğru
 *       sıralanmıştır. Spring, en spesifik handler'ı seçer.</li>
 * </ul>
 *
 * @see ApiErrorResponse
 * @see ValidationError
 * @see ResourceNotFoundException
 * @see BusinessRuleException
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ═══════════════════════════════════════════════════════════════════
    //  SPRING MVC STANDARD EXCEPTIONS (ResponseEntityExceptionHandler overrides)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * {@code @Valid} anotasyonu ile yakalanan doğrulama hatalarını işler.
     *
     * <p>Her hatalı alan için ayrı bir {@link ValidationError} nesnesi oluşturur.
     * Frontend bu listeyi kullanarak ilgili form alanlarının altına
     * hata mesajları gösterebilir.</p>
     *
     * @return 400 Bad Request + validationErrors listesi
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        String traceId = generateTraceId();
        String path = extractPath(request);

        List<ValidationError> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> new ValidationError(
                        fieldError.getField(),
                        fieldError.getRejectedValue(),
                        fieldError.getDefaultMessage()
                ))
                .collect(Collectors.toList());

        log.warn("[TraceId: {}] Validation failed on path: {} — {} field error(s)",
                traceId, path, validationErrors.size());

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Doğrulama hatası. Lütfen girdiğiniz bilgileri kontrol ediniz.",
                path,
                traceId,
                validationErrors
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Desteklenmeyen HTTP metodu ile istek yapıldığında (ör: GET endpoint'ine POST).
     *
     * @return 405 Method Not Allowed
     */
    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        String traceId = generateTraceId();
        String path = extractPath(request);

        log.warn("[TraceId: {}] Method not supported: {} on path: {}",
                traceId, ex.getMethod(), path);

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                HttpStatus.METHOD_NOT_ALLOWED.getReasonPhrase(),
                String.format("'%s' HTTP metodu bu endpoint için desteklenmiyor.", ex.getMethod()),
                path,
                traceId
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Desteklenmeyen Content-Type ile istek yapıldığında.
     *
     * @return 415 Unsupported Media Type
     */
    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        String traceId = generateTraceId();
        String path = extractPath(request);

        log.warn("[TraceId: {}] Unsupported media type: {} on path: {}",
                traceId, ex.getContentType(), path);

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                HttpStatus.UNSUPPORTED_MEDIA_TYPE.getReasonPhrase(),
                String.format("'%s' medya türü desteklenmiyor.", ex.getContentType()),
                path,
                traceId
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    /**
     * Zorunlu bir query parameter eksik olduğunda.
     *
     * @return 400 Bad Request
     */
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        String traceId = generateTraceId();
        String path = extractPath(request);

        log.warn("[TraceId: {}] Missing request parameter: '{}' on path: {}",
                traceId, ex.getParameterName(), path);

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                String.format("Zorunlu parametre eksik: '%s'", ex.getParameterName()),
                path,
                traceId
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  CUSTOM DOMAIN EXCEPTIONS
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Kaynak bulunamadığında (hasta, doktor, randevu vb.) fırlatılır.
     *
     * @return 404 Not Found
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {

        String traceId = generateTraceId();

        log.warn("[TraceId: {}] Resource not found: {} — Path: {}",
                traceId, ex.getMessage(), request.getRequestURI());

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                traceId
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * İş kuralı ihlallerinde (ör: gelecekteki randevu tamamlandı olarak işaretlenemez).
     *
     * @return 422 Unprocessable Entity
     */
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessRule(
            BusinessRuleException ex, HttpServletRequest request) {

        String traceId = generateTraceId();

        log.warn("[TraceId: {}] Business rule violation: {} — Path: {}",
                traceId, ex.getMessage(), request.getRequestURI());

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                traceId
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  SECURITY EXCEPTIONS
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Kimlik doğrulama başarısız olduğunda (geçersiz token, süresi dolmuş token vb.).
     *
     * <p><b>Not:</b> Spring Security filter zincirindeki AuthenticationException'lar
     * bu handler'a ulaşmaz — onlar SecurityConfig'deki authenticationEntryPoint
     * tarafından yakalanır. Bu handler, Controller katmanında fırlatılan
     * AuthenticationException'lar içindir.</p>
     *
     * @return 401 Unauthorized
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthentication(
            AuthenticationException ex, HttpServletRequest request) {

        String traceId = generateTraceId();

        log.warn("[TraceId: {}] Authentication failed: {} — Path: {}",
                traceId, ex.getMessage(), request.getRequestURI());

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "Kimlik doğrulama başarısız. Lütfen geçerli bir token ile giriş yapın.",
                request.getRequestURI(),
                traceId
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Yetki yetersiz olduğunda (doğru token ama izni yok).
     *
     * <p><b>Not:</b> Spring Security filter zincirindeki AccessDeniedException'lar
     * SecurityConfig'deki accessDeniedHandler tarafından yakalanır. Bu handler,
     * {@code @PreAuthorize} gibi method-level security anotasyonları tarafından
     * fırlatılan exception'lar içindir.</p>
     *
     * @return 403 Forbidden
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {

        String traceId = generateTraceId();

        log.warn("[TraceId: {}] Access denied: {} — Path: {} — User: {}",
                traceId, ex.getMessage(), request.getRequestURI(), request.getRemoteUser());

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                "Bu işlem için yetkiniz bulunmamaktadır.",
                request.getRequestURI(),
                traceId
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  DATA / PERSISTENCE EXCEPTIONS
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Veritabanı constraint ihlallerinde (duplicate key, foreign key vb.).
     *
     * <p>Veritabanı hata mesajları doğrudan kullanıcıya gösterilmez.
     * Duplicate key durumları tespit edilip anlaşılır mesaj döndürülür.</p>
     *
     * @return 409 Conflict
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request) {

        String traceId = generateTraceId();

        log.warn("[TraceId: {}] Data integrity violation on path: {} — Cause: {}",
                traceId, request.getRequestURI(), extractRootCauseMessage(ex));

        String message = "Veritabanı kural ihlali. Aynı TC Kimlik No veya E-posta ile "
                + "zaten bir kayıt bulunuyor olabilir.";

        // Duplicate key constraint'lerini daha spesifik mesajla karşıla
        String rootCause = extractRootCauseMessage(ex);
        if (rootCause.contains("active_slot_id")) {
            message = "Seçilen randevu saati az önce başka bir hasta tarafından rezerve edildi. Lütfen farklı bir saat seçiniz.";
        } else if (rootCause.contains("duplicate key value")
                || rootCause.contains("Duplicate entry")
                || rootCause.contains("unique constraint")
                || rootCause.contains("Unique index")) {
            message = "Girdiğiniz bilgilere ait sistemde kayıtlı başka bir kayıt zaten var "
                    + "(Mükerrer TC, E-posta veya Randevu). Lütfen kontrol edip tekrar deneyiniz.";
        }

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                message,
                request.getRequestURI(),
                traceId
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Optimistic locking çakışmalarında (aynı randevunun aynı anda alınması gibi).
     *
     * <p>Bu durum, iki kullanıcının aynı anda aynı kaydı güncellemeye
     * çalıştığında ortaya çıkar. Hastane sistemlerinde kritik bir senaryo:
     * İki hasta aynı randevu slotunu aynı anda alabilir.</p>
     *
     * @return 409 Conflict
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiErrorResponse> handleOptimisticLocking(
            ObjectOptimisticLockingFailureException ex, HttpServletRequest request) {

        String traceId = generateTraceId();

        log.warn("[TraceId: {}] Optimistic locking failure on path: {} — Entity: {}",
                traceId, request.getRequestURI(), ex.getPersistentClassName());

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                "Sistem çakışması tespit edildi! Bu kayıt saniyeler önce başka bir kullanıcı "
                        + "tarafından güncellenmiş olabilir. Lütfen sayfayı yenileyip tekrar deneyin.",
                request.getRequestURI(),
                traceId
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  GENERAL EXCEPTIONS
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Geçersiz argüman hatası.
     *
     * @return 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {

        String traceId = generateTraceId();

        log.warn("[TraceId: {}] Illegal argument on path: {} — Message: {}",
                traceId, request.getRequestURI(), ex.getMessage());

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                traceId
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Tüm beklenmeyen hatalar için fallback handler.
     *
     * <p><b>GÜVENLİK:</b> Gerçek exception mesajı asla kullanıcıya sızdırılmaz.
     * Sadece traceId referansı verilir. Asıl hata detayları server log'larında
     * ERROR seviyesinde saklanır.</p>
     *
     * @return 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAllUncaughtExceptions(
            Exception ex, HttpServletRequest request) {

        String traceId = generateTraceId();

        // Stack trace dahil full exception — sadece server loglarında
        log.error("[TraceId: {}] Unexpected error on path: {} — Type: {}",
                traceId, request.getRequestURI(), ex.getClass().getName(), ex);

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Beklenmeyen bir hata oluştu. Lütfen daha sonra tekrar deneyiniz. "
                        + "(Hata Kodu: " + traceId + ")",
                request.getRequestURI(),
                traceId
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  PRIVATE HELPER METHODS
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Benzersiz trace ID üretir. Her hata yanıtında ve logda kullanılır.
     * Destek ekibi, hastanın gördüğü bu kodu kullanarak logda aramaya yapabilir.
     */
    private String generateTraceId() {
        String traceId = MDC.get("traceId");
        return traceId != null ? traceId : UUID.randomUUID().toString();
    }

    /**
     * WebRequest'ten request URI'ı çıkarır.
     * ResponseEntityExceptionHandler override'larında HttpServletRequest
     * doğrudan erişilemediği için bu helper kullanılır.
     */
    private String extractPath(WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            return servletWebRequest.getRequest().getRequestURI();
        }
        return "unknown";
    }

    /**
     * Exception zincirinin en altındaki (root cause) mesajı çıkarır.
     * DataIntegrityViolationException gibi sarmalanan exception'larda
     * gerçek veritabanı hata mesajına ulaşmak için kullanılır.
     */
    private String extractRootCauseMessage(Throwable throwable) {
        Throwable rootCause = throwable;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        return rootCause.getMessage() != null ? rootCause.getMessage() : "";
    }
}
