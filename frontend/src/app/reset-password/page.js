'use client';
import { useState, useEffect, Suspense } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import Link from 'next/link';
import { AuthService } from '../../services/api';
import { useSettings } from '../../context/SettingsContext';
import styles from '../login/page.module.css';

function ResetPasswordForm() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const token = searchParams.get('token');
  const { t, tErr } = useSettings();
  
  const [formData, setFormData] = useState({
    newPassword: '',
    confirmPassword: ''
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  // Token validation is derived during render — no effect needed
  const tokenError = !token ? 'Geçersiz veya eksik şifre sıfırlama bağlantısı. Lütfen e-postanızdaki bağlantıya tekrar tıklayın.' : '';

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleReset = async (e) => {
    e.preventDefault();
    if (!token) return;
    
    if (formData.newPassword !== formData.confirmPassword) {
      setError('Şifreler birbiriyle eşleşmiyor.');
      return;
    }

    if (formData.newPassword.length < 6) {
      setError('Şifreniz en az 6 karakter olmalıdır.');
      return;
    }

    setLoading(true);
    setError('');
    setSuccess(false);

    try {
      await AuthService.resetPassword(token, formData.newPassword);
      setSuccess(true);
      setTimeout(() => {
        router.push('/login');
      }, 3000);
    } catch (err) {
      setError(tErr(err.message) || t('forgot_password_error'));
    } finally {
      setLoading(false);
    }
  };

  if (!token) {
    return (
      <div className={styles.container}>
        <div className={styles.loginCard} style={{ maxWidth: '450px' }}>
          <h1 className={styles.title}>Geçersiz Bağlantı</h1>
          <div className={styles.error}>{tokenError}</div>
          <div style={{ marginTop: '15px', textAlign: 'center' }}>
            <Link href="/forgot-password" className={styles.button} style={{ textDecoration: 'none', display: 'inline-block' }}>
              Yeni Şifre Sıfırlama Talebi Oluştur
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <div className={styles.loginCard} style={{ maxWidth: '450px' }}>
        <div className={styles.logo}>
          <svg width="32" height="32" viewBox="0 0 24 24" fill="#ffffff" stroke="rgba(255,255,255,0.4)" strokeWidth="0.5">
            <path d="M 19 3 A 10 10 0 1 0 19 21 A 9.5 9.5 0 1 1 19 3 Z" />
          </svg>
        </div>
        <h1 className={styles.title}>Yeni Şifre Belirle</h1>
        <p className={styles.subtitle}>Lütfen yeni şifrenizi girin.</p>

        {error && <div className={styles.error}>{error}</div>}
        {success && (
          <div style={{ backgroundColor: 'rgba(16, 185, 129, 0.1)', color: '#10b981', padding: '10px 14px', borderRadius: '8px', fontSize: '0.85rem', marginBottom: '10px', border: '1px solid rgba(16, 185, 129, 0.2)', width: '100%', lineHeight: '1.4' }}>
            Şifreniz başarıyla sıfırlandı. Giriş sayfasına yönlendiriliyorsunuz...
          </div>
        )}

        <form className={styles.form} onSubmit={handleReset}>
          <div className={styles.inputGroup}>
            <label className={styles.label}>{t('new_password')}</label>
            <input
              type="password"
              name="newPassword"
              className={styles.input}
              placeholder={t('placeholder_new_password')}
              value={formData.newPassword}
              onChange={handleChange}
              required
            />
          </div>

          <div className={styles.inputGroup}>
            <label className={styles.label}>Yeni Şifre (Tekrar)</label>
            <input
              type="password"
              name="confirmPassword"
              className={styles.input}
              placeholder="Yeni şifrenizi tekrar girin"
              value={formData.confirmPassword}
              onChange={handleChange}
              required
            />
          </div>

          <button type="submit" className={styles.button} disabled={loading || success}>
            {loading ? t('processing') : 'Şifreyi Kaydet'}
          </button>
          
          <div style={{ marginTop: '15px', textAlign: 'center', fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
            İptal etmek için <Link href="/login" style={{ color: 'var(--primary)', fontWeight: '600' }}>{t('login_page_title')}</Link> sayfasına dönün.
          </div>
        </form>
      </div>
    </div>
  );
}

export default function ResetPasswordPage() {
  return (
    <Suspense fallback={
      <div className={styles.container}>
        <div className={styles.loginCard} style={{ maxWidth: '450px', textAlign: 'center', color: 'white' }}>
          Yükleniyor...
        </div>
      </div>
    }>
      <ResetPasswordForm />
    </Suspense>
  );
}
