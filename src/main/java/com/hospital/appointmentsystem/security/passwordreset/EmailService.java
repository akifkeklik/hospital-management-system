package com.hospital.appointmentsystem.security.passwordreset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Email servisi — sifre sifirlama emaili gonderir.
 *
 * GUVENLIK:
 * - Token plaintext URL'de bulunur (bu tasarim geregi); bu metod disinda loglanmaz.
 * - EMAIL_ENABLED=false oldugunda sadece INFO log yazilir (development modu).
 * - Hata durumunda exception firlatir; caller karar verir (biz kullaniciya hata sizmayiz).
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${password.reset.email.from:noreply@hospital.com}")
    private String fromAddress;

    @Value("${password.reset.email.enabled:false}")
    private boolean emailEnabled;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Sifre sifirlama emaili gonderir.
     *
     * @param toEmail    alici email adresi
     * @param resetLink  frontend reset URL'si (token iceriyor — loglanmaz)
     * @throws MailException email gonderilemezse
     */
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        if (!emailEnabled) {
            // Development modunda email gondermiyoruz; sadece INFO log
            log.info("[DEV] Password reset email sending disabled. Recipient: [REDACTED]");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("Sifre Sifirlama Talebi");

            String htmlContent = buildHtmlEmail(resetLink);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Password reset email sent successfully.");
        } catch (MessagingException e) {
            log.error("Failed to build password reset email message", e);
            throw new RuntimeException("Email message could not be created", e);
        }
        // MailException (send hatasi) caller'a propagate edilir
    }

    private String buildHtmlEmail(String resetLink) {
        return """
                <!DOCTYPE html>
                <html lang="tr">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="margin:0;padding:0;background-color:#0f172a;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background:#0f172a;padding:40px 20px;">
                    <tr>
                      <td align="center">
                        <table width="560" cellpadding="0" cellspacing="0"
                               style="background:rgba(30,41,59,0.95);border-radius:16px;border:1px solid rgba(255,255,255,0.1);overflow:hidden;">
                          <!-- Header -->
                          <tr>
                            <td style="background:linear-gradient(135deg,#3b82f6,#1d4ed8);padding:32px 40px;text-align:center;">
                              <div style="width:56px;height:56px;background:rgba(255,255,255,0.15);border-radius:14px;
                                          display:inline-flex;align-items:center;justify-content:center;margin-bottom:16px;">
                                <span style="font-size:28px;">🔐</span>
                              </div>
                              <h1 style="color:#ffffff;font-size:22px;font-weight:700;margin:0;letter-spacing:-0.5px;">
                                Sifre Sifirlama Talebi
                              </h1>
                            </td>
                          </tr>
                          <!-- Body -->
                          <tr>
                            <td style="padding:36px 40px;color:#e2e8f0;">
                              <p style="font-size:15px;line-height:1.6;margin:0 0 20px 0;">Merhaba,</p>
                              <p style="font-size:15px;line-height:1.6;margin:0 0 24px 0;">
                                Hesabiniz icin bir sifre sifirlama talebi alindi.<br>
                                Sifrenizi yenilemek icin asagidaki butona tiklayin:
                              </p>
                              <div style="text-align:center;margin:32px 0;">
                                <a href="%s"
                                   style="display:inline-block;background:linear-gradient(135deg,#3b82f6,#2563eb);
                                          color:#ffffff;text-decoration:none;padding:14px 36px;
                                          border-radius:10px;font-weight:600;font-size:16px;
                                          box-shadow:0 4px 14px rgba(37,99,235,0.4);">
                                  Sifreyi Sifirla
                                </a>
                              </div>
                              <p style="font-size:13px;color:#94a3b8;line-height:1.6;margin:0 0 12px 0;">
                                Veya su baglantıyı tarayiciniza kopyalayin:
                              </p>
                              <p style="font-size:12px;color:#64748b;word-break:break-all;margin:0 0 24px 0;">%s</p>
                              <div style="background:rgba(245,158,11,0.1);border:1px solid rgba(245,158,11,0.2);
                                          border-radius:8px;padding:14px 16px;margin-bottom:24px;">
                                <p style="font-size:13px;color:#fbbf24;margin:0;">
                                  ⏱ Bu baglanti <strong>30 dakika</strong> gecerlidir ve yalnizca <strong>bir kez</strong> kullanilabilir.
                                </p>
                              </div>
                              <p style="font-size:14px;color:#94a3b8;margin:0;">
                                Bu talebi siz olusturmadıysaniz bu e-postayı dikkate almayabilirsiniz.
                                Hesabiniz guvendedir.
                              </p>
                            </td>
                          </tr>
                          <!-- Footer -->
                          <tr>
                            <td style="padding:20px 40px;border-top:1px solid rgba(255,255,255,0.08);text-align:center;">
                              <p style="font-size:12px;color:#475569;margin:0;">
                                Hastane Randevu Sistemi &mdash; Bu email otomatik olarak gonderilmistir.
                              </p>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(resetLink, resetLink);
    }
}
