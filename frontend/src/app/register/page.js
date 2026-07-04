'use client';
import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { AuthService } from '../../services/api';
import { useSettings } from '../../context/SettingsContext';
import styles from '../login/page.module.css';

export default function RegisterPage() {
  const router = useRouter();
  const { t } = useSettings();
  
  const [formData, setFormData] = useState({
    tcIdentityNumber: '',
    firstName: '',
    lastName: '',
    email: '',
    phoneNumber: '',
    password: ''
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

  const handleRegister = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccess(false);

    try {
      await AuthService.register(formData);
      setSuccess(true);
      setTimeout(() => {
        router.push('/login');
      }, 2000);
    } catch (err) {
      setError(err.message || t('registration_error'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.container}>
      <div className={styles.loginCard} style={{ maxWidth: '500px' }}>
        <div className={styles.logo}>
          <svg width="32" height="32" viewBox="0 0 24 24" fill="#ffffff" stroke="rgba(255,255,255,0.4)" strokeWidth="0.5">
            <path d="M 19 3 A 10 10 0 1 0 19 21 A 9.5 9.5 0 1 1 19 3 Z" />
          </svg>
        </div>
        <h1 className={styles.title}>{t('patient_registration_title')}</h1>
        <p className={styles.subtitle}>{t('patient_registration_subtitle')}</p>

        {error && <div className={styles.error}>{error}</div>}
        {success && (
          <div style={{ backgroundColor: 'rgba(16, 185, 129, 0.1)', color: '#10b981', padding: '10px 14px', borderRadius: '8px', fontSize: '0.85rem', marginBottom: '10px', border: '1px solid rgba(16, 185, 129, 0.2)', width: '100%' }}>
            {t('registration_success_redirecting')}
          </div>
        )}

        <form className={styles.form} onSubmit={handleRegister}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
            <div className={styles.inputGroup}>
              <label className={styles.label}>{t('placeholder_tc')}</label>
              <input
                type="text"
                name="tcIdentityNumber"
                className={styles.input}
                placeholder="11"
                value={formData.tcIdentityNumber}
                onChange={(e) => setFormData({...formData, tcIdentityNumber: e.target.value.replace(/[^0-9]/g, '')})}
                maxLength={11}
                required
              />
            </div>
            
            <div className={styles.inputGroup}>
              <label className={styles.label}>{t('phone')}</label>
              <input
                type="tel"
                name="phoneNumber"
                className={styles.input}
                placeholder="05554443322"
                pattern="[0-9]{10,11}"
                maxLength="11"
                value={formData.phoneNumber}
                onChange={(e) => setFormData({...formData, phoneNumber: e.target.value.replace(/[^0-9]/g, '')})}
                required
              />
            </div>

            <div className={styles.inputGroup}>
              <label className={styles.label}>{t('first_name')}</label>
              <input
                type="text"
                name="firstName"
                className={styles.input}
                placeholder={t('placeholder_first_name')}
                value={formData.firstName}
                onChange={handleChange}
                required
              />
            </div>

            <div className={styles.inputGroup}>
              <label className={styles.label}>{t('last_name')}</label>
              <input
                type="text"
                name="lastName"
                className={styles.input}
                placeholder={t('placeholder_last_name')}
                value={formData.lastName}
                onChange={handleChange}
                required
              />
            </div>

            <div className={styles.inputGroup}>
              <label className={styles.label}>{t('email')}</label>
              <input
                type="email"
                name="email"
                className={styles.input}
                placeholder="ornek@mail.com"
                value={formData.email}
                onChange={handleChange}
                required
              />
            </div>

            <div className={styles.inputGroup}>
              <label className={styles.label}>{t('label_password')}</label>
              <input
                type="password"
                name="password"
                className={styles.input}
                placeholder={t('placeholder_new_password')}
                value={formData.password}
                onChange={handleChange}
                required
              />
            </div>
          </div>

          <button type="submit" className={styles.button} disabled={loading || success} style={{ marginTop: '10px' }}>
            {loading ? t('saving') : t('register')}
          </button>
          
          <div style={{ marginTop: '15px', textAlign: 'center', fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
            {t('already_have_account')} <Link href="/login" style={{ color: 'var(--primary)', fontWeight: '600' }}>{t('login_page_title')}</Link>
          </div>
        </form>
      </div>
    </div>
  );
}
