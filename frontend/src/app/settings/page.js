'use client';
import { useSettings } from '../../context/SettingsContext';
import styles from './page.module.css';
import { useEffect, useState } from 'react';
import { AuthService, SystemSettingService } from '../../services/api';
import { toast } from '../../components/Toast';

export default function SettingsPage() {
  const { language, changeLanguage, themeColor, applyThemeColor, t, THEMES, LANGUAGES } = useSettings();
  const [mounted, setMounted] = useState(false);
  const [userProfile, setUserProfile] = useState(null);
  
  // Real System Settings State
  const [apptDuration, setApptDuration] = useState('15');
  const [startTime, setStartTime] = useState('09:00');
  const [endTime, setEndTime] = useState('17:00');
  const [lunchBreakStart, setLunchBreakStart] = useState('12:00');
  const [lunchBreakEnd, setLunchBreakEnd] = useState('13:00');
  const [maintenanceMode, setMaintenanceMode] = useState(false);

  useEffect(() => {
    setMounted(true);
    AuthService.getMe().then(data => {
      setUserProfile(data);
      if (data && data.role === 'ROLE_ADMIN') {
        SystemSettingService.getSettings().then(settings => {
          setApptDuration(settings.appointmentDuration.toString());
          setStartTime(settings.workStartTime);
          setEndTime(settings.workEndTime);
          setLunchBreakStart(settings.lunchBreakStart || '12:00');
          setLunchBreakEnd(settings.lunchBreakEnd || '13:00');
          setMaintenanceMode(settings.maintenanceMode);
        }).catch(err => console.error("Error fetching settings:", err));
      }
    }).catch(() => {});
  }, []);

  const handleSaveSystemSettings = async () => {
    try {
      await SystemSettingService.updateSettings({
        appointmentDuration: parseInt(apptDuration),
        workStartTime: startTime,
        workEndTime: endTime,
        lunchBreakStart: lunchBreakStart,
        lunchBreakEnd: lunchBreakEnd,
        maintenanceMode: maintenanceMode
      });
      toast.success(t('system_settings_updated'));
    } catch (err) {
      toast.error(t('system_settings_error'));
      console.error(err);
    }
  };

  if (!mounted) return null;

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h1 className={styles.title}>{t('settings')}</h1>
      </div>

      <div className={styles.settingsGrid}>
        
        {/* Sistem Ayarları (Sadece Admin Görür) */}
        {userProfile?.role === 'ROLE_ADMIN' && (
          <section className={styles.section} style={{ gridColumn: '1 / -1' }}>
            <div className={styles.sectionHeader} style={{ marginBottom: '0.5rem' }}>
              <div className={styles.sectionIcon}>⚙️</div>
              <div>
                <h2 className={styles.sectionTitle} style={{ fontSize: '1.1rem' }}>{t('hospital_system_settings')}</h2>
                <p className={styles.sectionDesc} style={{ fontSize: '0.8rem' }}>{t('hospital_system_settings_desc')}</p>
              </div>
            </div>
            
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '0.75rem', marginTop: '0.5rem' }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem' }}>
                <label style={{ fontSize: '0.8rem', fontWeight: '600', color: 'var(--text-main)' }}>{t('standard_appointment_duration')}</label>
                <select 
                  value={apptDuration} 
                  onChange={(e) => setApptDuration(e.target.value)}
                  style={{ padding: '0.5rem', borderRadius: '6px', border: '1px solid var(--border)', backgroundColor: 'var(--background)', color: 'var(--text-main)', fontSize: '0.85rem' }}
                >
                  <option value="10">10 {t('minutes')}</option>
                  <option value="15">15 {t('minutes')}</option>
                  <option value="20">20 {t('minutes')}</option>
                  <option value="30">30 {t('minutes')}</option>
                </select>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem' }}>
                <label style={{ fontSize: '0.8rem', fontWeight: '600', color: 'var(--text-main)' }}>{t('work_start_time')}</label>
                <input 
                  type="time" 
                  value={startTime}
                  onChange={(e) => setStartTime(e.target.value)}
                  style={{ padding: '0.5rem', borderRadius: '6px', border: '1px solid var(--border)', backgroundColor: 'var(--background)', color: 'var(--text-main)', fontSize: '0.85rem' }}
                />
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem' }}>
                <label style={{ fontSize: '0.8rem', fontWeight: '600', color: 'var(--text-main)' }}>{t('work_end_time')}</label>
                <input 
                  type="time" 
                  value={endTime}
                  onChange={(e) => setEndTime(e.target.value)}
                  style={{ padding: '0.5rem', borderRadius: '6px', border: '1px solid var(--border)', backgroundColor: 'var(--background)', color: 'var(--text-main)', fontSize: '0.85rem' }}
                />
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem' }}>
                <label style={{ fontSize: '0.8rem', fontWeight: '600', color: 'var(--text-main)' }}>{t('lunch_break_start')}</label>
                <input 
                  type="time" 
                  value={lunchBreakStart}
                  onChange={(e) => setLunchBreakStart(e.target.value)}
                  style={{ padding: '0.5rem', borderRadius: '6px', border: '1px solid var(--border)', backgroundColor: 'var(--background)', color: 'var(--text-main)', fontSize: '0.85rem' }}
                />
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem' }}>
                <label style={{ fontSize: '0.8rem', fontWeight: '600', color: 'var(--text-main)' }}>{t('lunch_break_end')}</label>
                <input 
                  type="time" 
                  value={lunchBreakEnd}
                  onChange={(e) => setLunchBreakEnd(e.target.value)}
                  style={{ padding: '0.5rem', borderRadius: '6px', border: '1px solid var(--border)', backgroundColor: 'var(--background)', color: 'var(--text-main)', fontSize: '0.85rem' }}
                />
              </div>

              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0.75rem', backgroundColor: 'var(--background)', borderRadius: '6px', border: '1px solid var(--border)', gridColumn: '1 / -1' }}>
                <div>
                  <div style={{ fontWeight: '600', color: 'var(--text-main)', fontSize: '0.85rem' }}>{t('maintenance_mode')}</div>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{t('maintenance_mode_desc')}</div>
                </div>
                <button 
                  onClick={() => setMaintenanceMode(!maintenanceMode)}
                  style={{ padding: '0.4rem 0.8rem', borderRadius: '15px', border: 'none', cursor: 'pointer', fontWeight: 'bold', fontSize: '0.8rem', backgroundColor: maintenanceMode ? '#ef4444' : '#e2e8f0', color: maintenanceMode ? '#fff' : '#64748b' }}
                >
                  {maintenanceMode ? t('ON') : t('OFF')}
                </button>
              </div>

              <div style={{ gridColumn: '1 / -1', textAlign: 'right' }}>
                <button 
                  onClick={handleSaveSystemSettings}
                  style={{ padding: '0.6rem 1.5rem', backgroundColor: 'var(--primary)', color: 'white', border: 'none', borderRadius: '6px', cursor: 'pointer', fontWeight: '600', fontSize: '0.9rem' }}
                >
                  {t('save_system_settings')}
                </button>
              </div>
            </div>
          </section>
        )}
        {/* Dil Ayarı */}
        <section className={styles.section}>
          <div className={styles.sectionHeader}>
            <div className={styles.sectionIcon}>🌐</div>
            <div>
              <h2 className={styles.sectionTitle}>{t('language_title')}</h2>
              <p className={styles.sectionDesc}>{t('language_desc')}</p>
            </div>
          </div>
          
          <div className={styles.langGrid}>
            {LANGUAGES.map((lang) => (
              <button
                key={lang.id}
                className={`${styles.langBtn} ${language === lang.id ? styles.activeLang : ''}`}
                onClick={() => changeLanguage(lang.id)}
              >
                <span className={styles.flag}>{lang.flag}</span>
                <span className={styles.langName}>{lang.name}</span>
                {language === lang.id && (
                  <svg className={styles.checkIcon} viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                  </svg>
                )}
              </button>
            ))}
          </div>
        </section>

        {/* Tema Ayarı */}
        <section className={styles.section}>
          <div className={styles.sectionHeader}>
            <div className={styles.sectionIcon}>🎨</div>
            <div>
              <h2 className={styles.sectionTitle}>{t('theme_title')}</h2>
              <p className={styles.sectionDesc}>{t('theme_desc')}</p>
            </div>
          </div>
          
          <div className={styles.themeGrid}>
            {THEMES.map((theme) => (
              <button
                key={theme.id}
                className={`${styles.themeBtn} ${themeColor === theme.id ? styles.activeTheme : ''}`}
                onClick={() => applyThemeColor(theme.id)}
                title={t(`theme_${theme.id}`)}
              >
                <div 
                  className={styles.colorSwatch} 
                  style={{ background: `linear-gradient(135deg, ${theme.hex}, ${theme.hover})` }}
                />
                <span className={styles.themeName}>{t(`theme_${theme.id}`)}</span>
              </button>
            ))}
          </div>
        </section>

      </div>
    </div>
  );
}
