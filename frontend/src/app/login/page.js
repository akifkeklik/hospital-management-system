'use client';
import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { AuthService, DepartmentService } from '../../services/api';
import { useSettings } from '../../context/SettingsContext';
import LanguageSelector from '../../components/LanguageSelector';
import styles from './page.module.css';

function parseJwt(token) {
  try {
    return JSON.parse(atob(token.split('.')[1]));
  } catch (e) {
    return null;
  }
}

export default function LoginPage() {
  const router = useRouter();
  const { t, tErr, language, changeLanguage } = useSettings();
  const [loginType, setLoginType] = useState(null); // 'PATIENT', 'DOCTOR', veya 'ADMIN'
  const [showDoctorRegister, setShowDoctorRegister] = useState(false);
  const [showForcePasswordChange, setShowForcePasswordChange] = useState(false);
  
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [passwordConfirm, setPasswordConfirm] = useState('');
  const [tempToken, setTempToken] = useState('');
  
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');
  const [departments, setDepartments] = useState([]);
  const [rememberMe, setRememberMe] = useState(false);

  // Doctor Registration Form State
  const [regData, setRegData] = useState({
    tcIdentityNumber: '', firstName: '', lastName: '', email: '', phoneNumber: '', specialization: '', departmentId: ''
  });

  useEffect(() => {
    if (loginType) {
      const savedUsername = localStorage.getItem(`remembered_username_${loginType}`);
      if (savedUsername) {
        setUsername(savedUsername);
        setRememberMe(true);
      } else {
        setUsername('');
        setRememberMe(false);
      }
    }
  }, [loginType]);

  useEffect(() => {
    if (showDoctorRegister) {
      DepartmentService.getAll(0, 100)
        .then(data => {
            if (data.content) {
                setDepartments(data.content);
            } else if (Array.isArray(data)) {
                setDepartments(data);
            }
        })
        .catch(err => console.error("Bölümler yüklenemedi", err));
    }
  }, [showDoctorRegister]);

  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      setSuccessMsg('');
      const response = await AuthService.login(username.trim(), password);
      if (response && response.token) {
        const decoded = parseJwt(response.token);
        const actualRole = decoded?.role;

        let valid = false;
        if (loginType === 'PATIENT' && actualRole === 'ROLE_PATIENT') valid = true;
        else if (loginType === 'DOCTOR' && actualRole === 'ROLE_DOCTOR') valid = true;
        else if (loginType === 'ADMIN' && actualRole === 'ROLE_ADMIN') valid = true;

        if (!valid) {
          setError(t('err_role_mismatch'));
          setLoading(false);
          return;
        }

        if (response.needsPasswordChange) {
          setTempToken(response.token);
          setPassword('');
          setShowForcePasswordChange(true);
          return;
        }

        if (rememberMe) {
          localStorage.setItem('token', response.token);
          localStorage.setItem(`remembered_username_${loginType}`, username.trim());
          sessionStorage.removeItem('token');
        } else {
          sessionStorage.setItem('token', response.token);
          localStorage.removeItem('token');
          localStorage.removeItem(`remembered_username_${loginType}`);
        }
        router.push('/');
      } else {
        setError(t('err_no_token'));
      }
    } catch (err) {
      if (err.message && (err.message.includes('Failed to fetch') || err.message.includes('NetworkError'))) {
        setError('Sunucuya ulaşılamıyor. Lütfen Vercel ayarlarında NEXT_PUBLIC_API_URL değişkeninin doğru (Render API linkiniz olarak) ayarlandığından emin olun.');
      } else {
        setError(t('err_invalid_credentials'));
      }
    } finally {
      setLoading(false);
    }
  };

  const handleDoctorRegister = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setSuccessMsg('');

    try {
      const data = await AuthService.doctorRegister({
        ...regData,
        departmentId: regData.departmentId ? Number(regData.departmentId) : null
      });
      setSuccessMsg(data.message || t('success_registration'));
      setShowDoctorRegister(false);
      setRegData({tcIdentityNumber: '', firstName: '', lastName: '', email: '', phoneNumber: '', specialization: '', departmentId: ''});
    } catch (err) {
      setError(tErr(err.message));
    } finally {
      setLoading(false);
    }
  };

  const handleForceChangeSubmit = async (e) => {
    e.preventDefault();
    if (password !== passwordConfirm) {
      setError(t('err_passwords_not_match'));
      return;
    }
    setLoading(true);
    setError('');
    try {
      await AuthService.forceChangePassword(username.trim(), password);
      localStorage.setItem('token', tempToken);
      router.push('/');
    } catch (err) {
      setError(tErr(err.message) || t('err_password_update'));
    } finally {
      setLoading(false);
    }
  };

  const renderLanguageSelector = () => (
    <LanguageSelector style={{ position: 'absolute', top: '1.5rem', right: '1.5rem', zIndex: 100 }} />
  );

  const renderInitialSelection = () => (
    <div className={styles.loginCard} style={{ maxWidth: '600px' }}>
      <div className={styles.logo}>
        <svg width="32" height="32" viewBox="0 0 24 24" fill="#ffffff" stroke="rgba(255,255,255,0.4)" strokeWidth="0.5">
          <path d="M 19 3 A 10 10 0 1 0 19 21 A 9.5 9.5 0 1 1 19 3 Z" />
        </svg>
      </div>
      <h1 className={styles.title}>{t('login_page_title')}</h1>
      <p className={styles.subtitle}>{t('login_subtitle')}</p>
      
      <div className={styles.roleSelection}>
        <button onClick={() => setLoginType('PATIENT')} className={styles.roleButton}>
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
            <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
            <circle cx="9" cy="7" r="4" />
            <path d="M22 21v-2a4 4 0 0 0-3-3.87" />
            <path d="M16 3.13a4 4 0 0 1 0 7.75" />
          </svg>
          <span className={styles.roleText}>{t('patient_login')}</span>
        </button>

        <button onClick={() => setLoginType('DOCTOR')} className={styles.roleButton}>
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
            <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
            <path d="M12 8v4" />
            <path d="M10 10h4" />
          </svg>
          <span className={styles.roleText}>{t('doctor_login')}</span>
        </button>

        <button onClick={() => setLoginType('ADMIN')} className={styles.roleButton}>
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
            <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
            <path d="M7 11V7a5 5 0 0 1 10 0v4" />
          </svg>
          <span className={styles.roleText}>{t('admin_login')}</span>
        </button>
      </div>
    </div>
  );

  const renderLoginForm = () => {
    let title = '';
    let placeholder = '';
    
    switch(loginType) {
      case 'PATIENT':
        title = t('patient_login');
        placeholder = t('placeholder_tc');
        break;
      case 'DOCTOR':
        title = t('doctor_login');
        placeholder = t('placeholder_tc');
        break;
      case 'ADMIN':
        title = t('admin_login');
        placeholder = t('label_username');
        break;
    }

    return (
      <div className={styles.loginCard}>
        <div className={styles.logo}>
          <svg width="32" height="32" viewBox="0 0 24 24" fill="#ffffff" stroke="rgba(255,255,255,0.4)" strokeWidth="0.5">
            <path d="M 19 3 A 10 10 0 1 0 19 21 A 9.5 9.5 0 1 1 19 3 Z" />
          </svg>
        </div>
        <h1 className={styles.title}>{title}</h1>
        <p className={styles.subtitle}>{t('verify_credentials')}</p>

        {error && (
          <div className={styles.error}>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="12" cy="12" r="10" />
              <line x1="12" y1="8" x2="12" y2="12" />
              <line x1="12" y1="16" x2="12.01" y2="16" />
            </svg>
            {error}
          </div>
        )}

        <form className={styles.form} onSubmit={handleLogin}>
          <div className={styles.inputGroup}>
            <label className={styles.label}>
              {loginType === 'PATIENT' || loginType === 'DOCTOR' ? t('placeholder_tc') : t('label_username')}
            </label>
            <input
              type="text"
              className={styles.input}
              placeholder={placeholder}
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
          </div>
          <div className={styles.inputGroup}>
            <label className={styles.label}>{t('label_password')}</label>
            <input
              type="password"
              className={styles.input}
              placeholder="••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>

          {/* Remember Me */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginTop: '-0.5rem' }}>
            <input
              type="checkbox"
              id="rememberMe"
              checked={rememberMe}
              onChange={(e) => setRememberMe(e.target.checked)}
              style={{ width: '16px', height: '16px', accentColor: 'var(--primary)', cursor: 'pointer' }}
            />
            <label htmlFor="rememberMe" style={{ color: 'rgba(255,255,255,0.7)', fontSize: '0.9rem', cursor: 'pointer' }}>
              {t('remember_me')}
            </label>
          </div>
          
          <button type="submit" className={styles.button} disabled={loading}>
            {loading ? t('logging_in') : t('login_page_title')}
          </button>
          
          <div className={styles.footerLinks}>
            <span 
              className={styles.backLink}
              onClick={() => { setLoginType(null); setError(''); setUsername(''); setPassword(''); }} 
            >
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <line x1="19" y1="12" x2="5" y2="12" />
                <polyline points="12 19 5 12 12 5" />
              </svg>
              {t('go_back')}
            </span>
            
            <div className={styles.footerActions}>
              <Link href="/forgot-password" className={styles.link}>{t('forgot_password')}</Link>
              {loginType === 'PATIENT' && (
                <>
                  <span style={{color: 'rgba(255,255,255,0.3)'}}>|</span>
                  <Link href="/register" className={styles.link}>{t('register')}</Link>
                </>
              )}
            </div>
          </div>
        </form>

        {successMsg && <div className={styles.success}>{successMsg}</div>}

        {loginType === 'DOCTOR' && !showDoctorRegister && (
          <div style={{textAlign: 'center', marginTop: '1rem'}}>
             <button type="button" onClick={() => { setShowDoctorRegister(true); setError(''); setSuccessMsg(''); }} className={styles.linkButton} style={{marginTop: '1rem'}}>
               {t('how_to_register_doctor')}
             </button>
          </div>
        )}

      </div>
    );
  };

  const renderDoctorRegisterForm = () => {
    return (
      <div className={styles.loginCard} style={{ maxWidth: '600px' }}>
        <h1 className={styles.title}>{t('doctor_registration_title')}</h1>
        <p className={styles.subtitle}>{t('doctor_registration_subtitle')}</p>

        {departments.length === 0 ? (
          <div style={{ marginTop: '1rem', padding: '1.5rem', background: 'rgba(239, 68, 68, 0.1)', border: '1px solid rgba(239, 68, 68, 0.2)', borderRadius: '8px', color: '#fca5a5', textAlign: 'center', lineHeight: '1.5' }}>
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ marginBottom: '1rem', margin: '0 auto', display: 'block' }}>
              <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path>
              <line x1="12" y1="9" x2="12" y2="13"></line>
              <line x1="12" y1="17" x2="12.01" y2="17"></line>
            </svg>
            <strong>{t('no_departments_warning_title') || 'Kayıt Yapılamıyor'}</strong>
            <div style={{ fontSize: '0.9rem', marginTop: '0.5rem', opacity: 0.9 }}>
              {t('no_departments_warning_desc') || 'Sistemde henüz kayıtlı hiçbir bölüm (poliklinik) bulunmadığı için doktor kaydı oluşturulamamaktadır. Lütfen sistem yöneticisi ile iletişime geçin.'}
            </div>
          </div>
        ) : (
          <form onSubmit={handleDoctorRegister} className={styles.form}>
            <div style={{display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem'}}>
            <div className={styles.inputGroup}>
                <label>{t('placeholder_tc')}</label>
                <input type="text" pattern="[0-9]{11}" maxLength="11" required value={regData.tcIdentityNumber} onChange={(e) => setRegData({...regData, tcIdentityNumber: e.target.value.replace(/[^0-9]/g, '')})} className={styles.input} placeholder="11 Haneli" />
            </div>
            <div className={styles.inputGroup}>
                <label>{t('department')}</label>
                <select 
                  value={regData.departmentId} 
                  onChange={(e) => setRegData({...regData, departmentId: e.target.value})} 
                  className={styles.input}
                  required
                >
                  <option value="">{t('select_department_option')}</option>
                  {departments.map(dept => (
                    <option key={dept.id} value={dept.id}>{dept.name}</option>
                  ))}
                </select>
            </div>
            <div className={styles.inputGroup}>
                <label>{t('first_name')}</label>
                <input type="text" required value={regData.firstName} onChange={(e) => setRegData({...regData, firstName: e.target.value})} className={styles.input} placeholder={t('placeholder_first_name')} />
            </div>
            <div className={styles.inputGroup}>
                <label>{t('last_name')}</label>
                <input type="text" required value={regData.lastName} onChange={(e) => setRegData({...regData, lastName: e.target.value})} className={styles.input} placeholder={t('placeholder_last_name')} />
            </div>
            <div className={styles.inputGroup}>
                <label>{t('email')}</label>
                <input type="email" required value={regData.email} onChange={(e) => setRegData({...regData, email: e.target.value})} className={styles.input} placeholder={t('placeholder_email')} />
            </div>
            <div className={styles.inputGroup}>
                <label>{t('phone')}</label>
                <input type="tel" pattern="[0-9]{10,11}" maxLength="11" value={regData.phoneNumber} onChange={(e) => setRegData({...regData, phoneNumber: e.target.value.replace(/[^0-9]/g, '')})} className={styles.input} placeholder={t('placeholder_phone')} />
            </div>
            <div className={styles.inputGroup}>
                <label>{t('title_specialization')}</label>
                <select 
                  value={regData.specialization} 
                  onChange={(e) => setRegData({...regData, specialization: e.target.value})} 
                  className={styles.input}
                  required
                >
                  <option value="">{t('select_title')}</option>
                  <option value="Pratisyen Hekim">{t('gp')}</option>
                  <option value="Uzm. Dr.">{t('specialist_doctor')}</option>
                  <option value="Op. Dr.">{t('operator_doctor')}</option>
                  <option value="Yrd. Doç. Dr.">{t('assist_prof')}</option>
                  <option value="Doç. Dr.">{t('assoc_prof')}</option>
                  <option value="Prof. Dr.">{t('prof_doctor')}</option>
                  <option value="Asistan Dr.">{t('assist_doctor')}</option>
                </select>
            </div>
          </div>

          <div style={{ marginTop: '0.75rem', padding: '0.75rem', background: 'rgba(255, 255, 255, 0.05)', borderRadius: '8px', fontSize: '0.8rem', color: '#9ca3af', lineHeight: '1.4' }}>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ verticalAlign: 'middle', marginRight: '6px', marginTop: '-2px' }}>
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="16" x2="12" y2="12"></line>
                <line x1="12" y1="8" x2="12.01" y2="8"></line>
            </svg>
            {t('security_notice')}
          </div>

          {error && <div className={styles.error} style={{marginTop: '0.5rem', marginBottom: '0'}}>{error}</div>}

          <button type="submit" className={styles.submitBtn} disabled={loading} style={{ marginTop: '0.75rem' }}>
            {loading ? t('submitting') : t('submit_request')}
          </button>
        </form>
        )}

        <div className={styles.backLink} style={{marginTop: '0.75rem', justifyContent: 'center', width: '100%'}}>
            <button type="button" onClick={() => { setShowDoctorRegister(false); setError(''); }} className={styles.linkButton}>
              {t('back_to_login')}
            </button>
        </div>
      </div>
    );
  };

  const renderForcePasswordChangeForm = () => (
    <div className={styles.loginCard} style={{ maxWidth: '480px' }}>
      <h1 className={styles.title}>{t('welcome_title')}</h1>
      <p className={styles.subtitle} style={{color: '#fca5a5'}}>{t('force_password_change_subtitle')}</p>
      
      {error && (
        <div className={styles.error}>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="8" x2="12" y2="12" />
            <line x1="12" y1="16" x2="12.01" y2="16" />
          </svg>
          {error}
        </div>
      )}

      <form className={styles.form} onSubmit={handleForceChangeSubmit}>
        <div className={styles.inputGroup}>
          <label className={styles.label}>{t('new_password')}</label>
          <input
            type="password"
            className={styles.input}
            placeholder={t('placeholder_new_password')}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>
        <div className={styles.inputGroup}>
          <label className={styles.label}>{t('new_password_confirm')}</label>
          <input
            type="password"
            className={styles.input}
            placeholder={t('placeholder_new_password_confirm')}
            value={passwordConfirm}
            onChange={(e) => setPasswordConfirm(e.target.value)}
            required
          />
        </div>
        
        <button type="submit" className={styles.button} disabled={loading} style={{background: 'linear-gradient(135deg, #10b981, #059669)', boxShadow: '0 4px 14px 0 rgba(16, 185, 129, 0.39)', marginTop: '1.5rem'}}>
          {loading ? t('updating') : t('set_password_and_login')}
        </button>
      </form>
    </div>
  );

  return (
    <div className={styles.container}>
      {renderLanguageSelector()}
      {showForcePasswordChange 
        ? renderForcePasswordChangeForm() 
        : (loginType === null 
            ? renderInitialSelection() 
            : (showDoctorRegister ? renderDoctorRegisterForm() : renderLoginForm()))
      }
    </div>
  );
}
