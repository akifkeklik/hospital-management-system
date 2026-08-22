'use client';
import { useState, useEffect, useRef } from 'react';
import { useRouter } from 'next/navigation';
import { AuthService } from '../services/api';
import { useSettings } from '../context/SettingsContext';
import Link from 'next/link';
import { usePwaInstall } from '../hooks/usePwaInstall';

export default function PatientHeader() {
  const { isInstallable, installApp } = usePwaInstall();
  const router = useRouter();
  const [theme, setTheme] = useState('dark');
  const [userProfile, setUserProfile] = useState(null);
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [isNotifOpen, setIsNotifOpen] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const dropdownRef = useRef(null);
  const notifRef = useRef(null);
  const { t } = useSettings();

  useEffect(() => {
    const savedTheme = localStorage.getItem('theme') || 'dark';
    // eslint-disable-next-line
    
    document.documentElement.setAttribute('data-theme', savedTheme);

    const fetchProfile = async (retryCount = 0) => {
      try {
        const data = await AuthService.getMe();
        setUserProfile(data);
        if (data && data.id) {
          const notifs = await import('../services/api').then(m => m.NotificationService.getByPatient(data.id));
          setNotifications(notifs.slice(0, 5));
          setUnreadCount(notifs.filter(n => !n.read).length);
        }
      } catch (error) {
        console.error("Profil bilgisi alınamadı:", error);
        // Render cold start timeout olabilir, 3 saniye sonra tekrar dene (max 2 retry)
        if (retryCount < 2) {
          setTimeout(() => fetchProfile(retryCount + 1), 3000);
        }
      }
    };
    fetchProfile();

    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsDropdownOpen(false);
      }
      if (notifRef.current && !notifRef.current.contains(event.target)) {
        setIsNotifOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const toggleTheme = () => {
    const themeOrder = ['dark', 'light', 'high-contrast'];
    const currentIndex = themeOrder.indexOf(theme);
    const newTheme = themeOrder[(currentIndex + 1) % themeOrder.length];
    setTheme(newTheme);
    localStorage.setItem('theme', newTheme);
    document.documentElement.setAttribute('data-theme', newTheme);
  };

  const handleLogout = () => {
    AuthService.logout();
  };

  const initial = userProfile?.firstName && userProfile?.lastName 
    ? `${userProfile.firstName.charAt(0)}${userProfile.lastName.charAt(0)}`.toUpperCase() 
    : userProfile?.firstName ? userProfile.firstName.charAt(0).toUpperCase() : '';

  return (
    <header style={headerStyle}>
      <div style={logoContainerStyle}>
        <Link href="/" style={logoStyle}>
          <div style={iconStyle}>
            <svg width="24" height="24" viewBox="0 0 24 24" fill="#ffffff" stroke="rgba(255,255,255,0.4)" strokeWidth="0.5">
              <path d="M 19 3 A 10 10 0 1 0 19 21 A 9.5 9.5 0 1 1 19 3 Z" />
            </svg>
          </div>
          <div>
            <span style={titleStyle}>{t('logo_title')}</span>
            <span style={subtitleStyle}>{t('logo_desc')}</span>
          </div>
        </Link>
      </div>
      
      <div style={actionsStyle}>
        {isInstallable && (
          <button style={installBtnStyle} onClick={installApp} title={t('pwa_install') || "Uygulamayı Kur"}>
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
              <polyline points="7 10 12 15 17 10"></polyline>
              <line x1="12" y1="15" x2="12" y2="3"></line>
            </svg>
          </button>
        )}
        <button style={themeToggleStyle} onClick={toggleTheme} title={theme === 'dark' ? t('light_mode') : theme === 'light' ? t('high_contrast_mode') : t('dark_mode')}>
          {theme === 'dark' ? (
            <span style={{ fontSize: '1.2rem' }}>🌙</span>
          ) : theme === 'light' ? (
            <span style={{ fontSize: '1.2rem' }}>☀️</span>
          ) : (
            <span style={{ fontSize: '1.2rem' }}>👁️</span>
          )}
        </button>
        
        <div style={{ position: 'relative' }} ref={notifRef}>
          <button 
            onClick={() => setIsNotifOpen(!isNotifOpen)} 
            style={themeToggleStyle}
            onMouseOver={(e) => { e.currentTarget.style.backgroundColor = 'var(--background)'; e.currentTarget.style.color = 'var(--text-main)'; }}
            onMouseOut={(e) => { e.currentTarget.style.backgroundColor = 'transparent'; e.currentTarget.style.color = 'var(--text-muted)'; }}
            title={t('notifications')}
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path><path d="M13.73 21a2 2 0 0 1-3.46 0"></path></svg>
            {unreadCount > 0 && (
              <span style={{ position: 'absolute', top: '8px', right: '8px', width: '8px', height: '8px', backgroundColor: '#ef4444', borderRadius: '50%' }}></span>
            )}
          </button>
          
          {isNotifOpen && (
            <div style={{...dropdownMenuStyle, right: '-50px', width: '320px'}}>
              <div style={dropdownHeaderStyle}>
                <strong style={{ color: 'var(--text-main)', fontSize: '1.05rem', display: 'block' }}>{t('notifications')}</strong>
              </div>
              <div style={{...dropdownBodyStyle, maxHeight: '300px', overflowY: 'auto', padding: '0'}}>
                {notifications.length === 0 ? (
                  <div style={{ padding: '1.5rem', textAlign: 'center', color: 'var(--text-muted)', fontSize: '0.9rem' }}>{t('no_notifications')}</div>
                ) : (
                  notifications.map(notif => (
                    <div 
                      key={notif.id} 
                      style={{ 
                        padding: '1rem', 
                        borderBottom: '1px solid var(--border)',
                        backgroundColor: notif.read ? 'transparent' : 'rgba(16, 185, 129, 0.05)',
                        cursor: 'pointer'
                      }}
                      onClick={() => {
                        setIsNotifOpen(false);
                        router.push('/patient-notifications');
                      }}
                    >
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
                        {!notif.read && <div style={{ width: '6px', height: '6px', borderRadius: '50%', backgroundColor: '#ef4444' }}></div>}
                        <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                          {new Date(notif.createdAt).toLocaleString('tr-TR', { dateStyle: 'short', timeStyle: 'short' })}
                        </span>
                      </div>
                      <p style={{ color: notif.read ? 'var(--text-muted)' : 'var(--text-main)', fontSize: '0.85rem', margin: 0, fontWeight: notif.read ? '400' : '500', lineHeight: '1.4' }}>
                        {notif.message}
                      </p>
                    </div>
                  ))
                )}
              </div>
              <div style={dropdownFooterStyle}>
                <button 
                  onClick={() => { setIsNotifOpen(false); router.push('/patient-notifications'); }} 
                  style={{...logoutBtnStyle, backgroundColor: 'transparent', color: 'var(--primary)', border: 'none', textAlign: 'center', width: '100%', cursor: 'pointer', fontWeight: '600'}}
                  onMouseOver={(e) => { e.currentTarget.style.textDecoration = 'underline'; }}
                  onMouseOut={(e) => { e.currentTarget.style.textDecoration = 'none'; }}
                >
                  {t('see_all_notifications')}
                </button>
              </div>
            </div>
          )}
        </div>

        <button 
          onClick={() => router.push('/settings')} 
          style={themeToggleStyle}
          onMouseOver={(e) => { e.currentTarget.style.backgroundColor = 'var(--background)'; e.currentTarget.style.color = 'var(--text-main)'; }}
          onMouseOut={(e) => { e.currentTarget.style.backgroundColor = 'transparent'; e.currentTarget.style.color = 'var(--text-muted)'; }}
          title={t('settings')}
        >
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="3"></circle><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path></svg>
        </button>

        <div style={profileContainerStyle} ref={dropdownRef}>
          <div style={profileStyle} onClick={() => setIsDropdownOpen(!isDropdownOpen)}>
            <div style={avatarStyle}>{initial}</div>
            <div style={infoStyle}>
              <span style={nameStyle}>{userProfile ? `${userProfile.firstName} ${userProfile.lastName}` : '...'}</span>
              <span style={roleStyle}>{t('patient')}</span>
            </div>
          </div>

          {isDropdownOpen && (
            <div style={dropdownMenuStyle}>
              <div style={dropdownHeaderStyle}>
                <strong style={{ color: 'var(--text-main)', fontSize: '1.05rem', marginBottom: '0.2rem', display: 'block' }}>
                  {userProfile ? `${userProfile.firstName} ${userProfile.lastName}` : t('unknown_patient')}
                </strong>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                  {userProfile?.email || '-'}
                </span>
              </div>
              <div style={dropdownBodyStyle}>
                <div style={dropdownItemStyle}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-muted)' }}>
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle></svg>
                    <span style={itemLabelStyle}>{t('role')}</span>
                  </div>
                  <span style={{ ...itemValueStyle, color: 'var(--primary)', fontWeight: '700' }}>{t('patient')}</span>
                </div>
                <div style={dropdownItemStyle}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-muted)' }}>
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
                    <span style={itemLabelStyle}>{t('tc_no')}</span>
                  </div>
                  <span style={itemValueStyle}>{userProfile?.username || '-'}</span>
                </div>
                <div style={dropdownItemStyle}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-muted)' }}>
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path></svg>
                    <span style={itemLabelStyle}>{t('phone')}</span>
                  </div>
                  <span style={itemValueStyle}>{userProfile?.phoneNumber || '-'}</span>
                </div>
              </div>
              <div style={dropdownFooterStyle}>
                <button 
                  onClick={handleLogout} 
                  style={logoutBtnStyle}
                  onMouseOver={(e) => { e.currentTarget.style.backgroundColor = '#ef4444'; e.currentTarget.style.color = 'white'; }}
                  onMouseOut={(e) => { e.currentTarget.style.backgroundColor = 'rgba(239, 68, 68, 0.1)'; e.currentTarget.style.color = '#ef4444'; }}
                >
                  {t('logout')}
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}

// Inline styles for PatientHeader (using CSS variables)
const headerStyle = {
  height: '70px',
  display: 'flex',
  justifyContent: 'space-between',
  alignItems: 'center',
  padding: '0 2rem',
  backgroundColor: 'var(--surface)',
  borderBottom: '1px solid var(--border)',
  position: 'sticky',
  top: 0,
  zIndex: 100,
  boxShadow: 'var(--shadow-sm)'
};

const logoContainerStyle = {
  display: 'flex',
  alignItems: 'center',
};

const logoStyle = {
  display: 'flex',
  alignItems: 'center',
  gap: '12px',
  textDecoration: 'none',
  color: 'var(--text-main)'
};

const iconStyle = {
  background: 'linear-gradient(135deg, var(--primary), var(--primary-hover))',
  color: '#ffffff',
  width: '38px',
  height: '38px',
  minWidth: '38px',
  borderRadius: '10px',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
};

const titleStyle = {
  fontWeight: '800',
  fontSize: '1.2rem',
  letterSpacing: '1px',
  display: 'block',
  lineHeight: '1.2'
};

const subtitleStyle = {
  fontSize: '0.7rem',
  color: 'var(--text-muted)',
  textTransform: 'uppercase',
  letterSpacing: '1px',
  display: 'block'
};

const actionsStyle = {
  display: 'flex',
  alignItems: 'center',
  gap: '1.5rem'
};

const installBtnStyle = {
  background: 'linear-gradient(135deg, #8b5cf6, #6366f1)',
  border: 'none',
  color: 'white',
  padding: '0.5rem',
  borderRadius: '8px',
  cursor: 'pointer',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  boxShadow: '0 2px 8px rgba(139, 92, 246, 0.3)'
};

const bookBtnStyle = {
  backgroundColor: 'var(--primary)',
  color: '#fff',
  padding: '0.5rem 1.25rem',
  borderRadius: '6px',
  fontWeight: '600',
  fontSize: '0.9rem',
  transition: 'background-color 0.2s',
  textDecoration: 'none'
};

const themeToggleStyle = {
  background: 'none',
  border: 'none',
  color: 'var(--text-muted)',
  cursor: 'pointer',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  padding: '8px',
  borderRadius: '50%',
  transition: 'color 0.2s, background-color 0.2s'
};

const profileContainerStyle = {
  position: 'relative'
};

const profileStyle = {
  display: 'flex',
  alignItems: 'center',
  gap: '0.75rem',
  cursor: 'pointer',
  padding: '0.5rem',
  borderRadius: '8px',
  transition: 'background-color 0.2s'
};

const avatarStyle = {
  width: '36px',
  height: '36px',
  borderRadius: '50%',
  backgroundColor: 'var(--primary)',
  color: '#ffffff',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  fontWeight: '600',
  fontSize: '1rem'
};

const infoStyle = {
  display: 'flex',
  flexDirection: 'column'
};

const nameStyle = {
  fontSize: '0.9rem',
  fontWeight: '600',
  color: 'var(--text-main)',
  lineHeight: '1.2'
};

const roleStyle = {
  fontSize: '0.75rem',
  color: 'var(--text-muted)',
  lineHeight: '1.2'
};

const dropdownMenuStyle = {
  position: 'absolute',
  top: 'calc(100% + 10px)',
  right: '0',
  width: '260px',
  backgroundColor: 'var(--surface)',
  border: '1px solid var(--border)',
  borderRadius: '16px',
  boxShadow: '0 10px 40px -10px rgba(0,0,0,0.2)',
  zIndex: 100,
  overflow: 'hidden'
};

const dropdownHeaderStyle = {
  padding: '1.2rem',
  background: 'linear-gradient(135deg, rgba(var(--primary-rgb), 0.1), transparent)',
  borderBottom: '1px solid var(--border)',
  display: 'flex',
  flexDirection: 'column'
};

const dropdownBodyStyle = {
  padding: '1rem',
  display: 'flex',
  flexDirection: 'column',
  gap: '0.8rem'
};

const dropdownItemStyle = {
  display: 'flex',
  justifyContent: 'space-between',
  alignItems: 'center',
  fontSize: '0.85rem'
};

const itemLabelStyle = {
  color: 'var(--text-muted)',
  fontWeight: '500'
};

const itemValueStyle = {
  color: 'var(--text-main)',
  fontWeight: '600'
};

const dropdownFooterStyle = {
  padding: '1rem',
  borderTop: '1px solid var(--border)',
  backgroundColor: 'rgba(var(--background-rgb), 0.5)'
};

const logoutBtnStyle = {
  width: '100%',
  padding: '0.8rem',
  backgroundColor: 'rgba(239, 68, 68, 0.1)',
  color: '#ef4444',
  border: '1px solid rgba(239, 68, 68, 0.2)',
  borderRadius: '8px',
  fontWeight: '600',
  textAlign: 'center',
  cursor: 'pointer',
  transition: 'all 0.2s ease'
};
