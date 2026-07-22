'use client';
import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { AuthService } from '../../services/api';
import { useSettings } from '../../context/SettingsContext';
import styles from '../login/page.module.css';

export default function ForgotPasswordPage() {
  const router = useRouter();
  const { t, tErr } = useSettings();
  
  const [formData, setFormData] = useState({
    tcIdentityNumber: '',
    email: '',
    newPassword: ''
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleReset = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccess(false);

    try {
      await AuthService.resetPassword(formData.tcIdentityNumber, formData.email, formData.newPassword);
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

  return (
    <div className={styles.container}>
      <div className={styles.loginCard} style={{ maxWidth: '450px' }}>
        <div className={styles.logo}>
          <svg width="32" height="32" viewBox="0 0 24 24" fill="#ffffff" stroke="rgba(255,255,255,0.4)" strokeWidth="0.5">
            <path d="M 19 3 A 10 10 0 1 0 19 21 A 9.5 9.5 0 1 1 19 3 Z" />
          </svg>
        </div>
        <h1 className={styles.title}>{t('forgot_password')}</h1>
        <p className={styles.subtitle}>{t('forgot_password_subtitle')}</p>

        {error && <div className={styles.error}>{error}</div>}
        {success && (
          <div style={{ backgroundColor: 'rgba(16, 185, 129, 0.1)', color: '#10b981', padding: '10px 14px', borderRadius: '8px', fontSize: '0.85rem', marginBottom: '10px', border: '1px solid rgba(16, 185, 129, 0.2)', width: '100%' }}>
            {t('forgot_password_success')}
          </div>
        )}

        <form className={styles.form} onSubmit={handleReset}>
          <div className={styles.inputGroup}>
            <label className={styles.label}>{t('placeholder_tc')}</label>
            <input
              type="text"
              name="tcIdentityNumber"
              className={styles.input}
              placeholder={t('placeholder_tc')}
              value={formData.tcIdentityNumber}
              onChange={(e) => setFormData({...formData, tcIdentityNumber: e.target.value.replace(/[^0-9]/g, '')})}
              maxLength={11}
              required
            />
          </div>

          <div className={styles.inputGroup}>
            <label className={styles.label}>{t('email')}</label>
            <input
              type="email"
              name="email"
              className={styles.input}
              placeholder={t('placeholder_registered_email')}
              value={formData.email}
              onChange={handleChange}
              required
            />
          </div>

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

          <button type="submit" className={styles.button} disabled={loading || success}>
            {loading ? t('processing') : t('reset_password')}
          </button>
          
          <div style={{ marginTop: '15px', textAlign: 'center', fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
            {t('remember_password')} <Link href="/login" style={{ color: 'var(--primary)', fontWeight: '600' }}>{t('login_page_title')}</Link>
          </div>
        </form>
      </div>
    </div>
  );
}
