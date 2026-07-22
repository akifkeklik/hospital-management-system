'use client';
import { useState, useEffect, useRef } from 'react';
import { useRouter } from 'next/navigation';
import { useSettings } from '../context/SettingsContext';
import { AuthService } from '../services/api';
import { toast } from '../components/Toast';
import styles from './Header.module.css';

export default function Header() {
  const router = useRouter();
  const [theme, setTheme] = useState('dark');
  const [userProfile, setUserProfile] = useState(null);
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [isNotifOpen, setIsNotifOpen] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [searchQuery, setSearchQuery] = useState('');
  const [allDoctors, setAllDoctors] = useState([]);
  const [allDepartments, setAllDepartments] = useState([]);
  const [allPatients, setAllPatients] = useState([]);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const dropdownRef = useRef(null);
  const notifRef = useRef(null);
  const searchRef = useRef(null);
  const { t } = useSettings();

  useEffect(() => {
    // Tema ayarını yükle
    const savedTheme = localStorage.getItem('theme') || 'dark';
    // eslint-disable-next-line
    
    document.documentElement.setAttribute('data-theme', savedTheme);

    // Profil bilgisini çek
    const fetchProfile = async () => {
      try {
        const data = await AuthService.getMe();
        setUserProfile(data);
        if (data && data.id) {
          let notifs = [];
          if (data.role === 'DOCTOR' || data.role === 'ROLE_DOCTOR' || data.role === 'HEKIM' || data.role === 'ROLE_HEKIM') {
            notifs = await import('../services/api').then(m => m.NotificationService.getByDoctor(data.id));
          } else {
            notifs = await import('../services/api').then(m => m.NotificationService.getByPatient(data.id));
          }
          const hiddenNotifs = JSON.parse(localStorage.getItem('hiddenNotifs') || '[]');
          notifs = notifs.filter(n => !hiddenNotifs.includes(n.id));
          setNotifications(notifs.slice(0, 5));
          setUnreadCount(notifs.filter(n => !n.read).length);
        }
      } catch (error) {
        console.error("Profil bilgisi alınamadı:", error);
      }
    };
    fetchProfile();

    // Arama verilerini (doktorlar, bölümler, hastalar) çek
    const fetchSearchData = async () => {
      try {
        const { DoctorService, DepartmentService, PatientService } = await import('../services/api');
        const [docs, depts, patients] = await Promise.all([
          DoctorService.getAll(0, 500),
          DepartmentService.getAll(0, 100),
          PatientService.getAll(0, 500)
        ]);
        setAllDoctors(docs.content || []);
        setAllDepartments(depts.content || []);
        setAllPatients(patients.content || []);
      } catch (error) {
        console.error("Arama verisi alınamadı:", error);
      }
    };
    fetchSearchData();

    // Dışarı tıklayınca dropdown kapansın
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsDropdownOpen(false);
      }
      if (notifRef.current && !notifRef.current.contains(event.target)) {
        setIsNotifOpen(false);
      }
      if (searchRef.current && !searchRef.current.contains(event.target)) {
        setShowSuggestions(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    window.addEventListener("hiddenNotifsUpdate", fetchProfile);

    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
      window.removeEventListener("hiddenNotifsUpdate", fetchProfile);
    };
  }, []);

  const toggleTheme = () => {
    const newTheme = theme === 'light' ? 'dark' : 'light';
    setTheme(newTheme);
    localStorage.setItem('theme', newTheme);
    document.documentElement.setAttribute('data-theme', newTheme);
  };

  const filteredSuggestions = searchQuery.trim().length >= 2 ? (() => {
    const term = searchQuery.toLowerCase().trim();
    const docSuggestions = allDoctors.filter(d => 
      `${d.firstName} ${d.lastName}`.toLowerCase().includes(term) || 
      (d.specialization && d.specialization.toLowerCase().includes(term))
    ).slice(0, 5).map(d => ({ type: 'doctor', text: `${d.firstName} ${d.lastName} (${t('doctor')}) - ${d.specialization || t('no_data')}`, url: `/doctors?search=${encodeURIComponent(d.firstName)}` }));

    const deptSuggestions = allDepartments.filter(d => 
      d.name.toLowerCase().includes(term)
    ).slice(0, 3).map(d => ({ type: 'department', text: `${d.name} (${t('department')})`, url: `/doctors?search=${encodeURIComponent(d.name)}` }));

    const patSuggestions = allPatients.filter(p => 
      `${p.firstName} ${p.lastName}`.toLowerCase().includes(term) || 
      (p.tcIdentityNumber && p.tcIdentityNumber.includes(term))
    ).slice(0, 3).map(p => ({ type: 'patient', text: `${p.firstName} ${p.lastName} (${t('patient')}) - ${p.tcIdentityNumber}`, url: `/patients?search=${encodeURIComponent(p.tcIdentityNumber)}` }));

    return [...docSuggestions, ...deptSuggestions, ...patSuggestions];
  })() : [];

  const handleGlobalSearch = (e) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      if (filteredSuggestions && filteredSuggestions.length > 0) {
        router.push(filteredSuggestions[0].url);
        setShowSuggestions(false);
      } else {
        toast.error(t('no_results_found'));
      }
    }
  };

  const handleLogout = async () => {
    await AuthService.logout();
  };

  const roleText = (userProfile?.role === 'ROLE_PATIENT' || userProfile?.role === 'PATIENT' || userProfile?.role === 'HASTA') ? t('patient') : 
                   (userProfile?.role === 'ROLE_DOCTOR' || userProfile?.role === 'DOCTOR' || userProfile?.role === 'HEKIM' || userProfile?.role === 'HEKİM') ? t('doctor') : t('admin_role');

  const initial = userProfile?.firstName && userProfile?.lastName 
    ? `${userProfile.firstName.charAt(0)}${userProfile.lastName.charAt(0)}`.toUpperCase() 
    : userProfile?.firstName ? userProfile.firstName.charAt(0).toUpperCase() : '';

  return (
    <header className={styles.header}>
      <div style={{ position: 'relative', width: '100%', maxWidth: '400px' }} ref={searchRef}>
        <form className={styles.search} onSubmit={handleGlobalSearch} style={{ width: '100%' }}>
          <div 
            className={styles.searchIcon} 
            onClick={handleGlobalSearch}
            style={{ cursor: 'pointer' }}
            title={t('search')}
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <circle cx="11" cy="11" r="8"></circle>
              <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
            </svg>
          </div>
          <input 
            type="text" 
            placeholder={t('global_search_placeholder')}
            className={styles.searchInput}
            value={searchQuery}
            onChange={(e) => {
              setSearchQuery(e.target.value);
              setShowSuggestions(true);
            }}
            onFocus={() => setShowSuggestions(true)}
            style={{ width: '100%' }}
          />
        </form>

        {/* Live Search Suggestions Dropdown */}
        {showSuggestions && searchQuery.trim().length >= 2 && (
          <div style={{
            position: 'absolute',
            top: 'calc(100% + 8px)',
            left: 0,
            right: 0,
            backgroundColor: 'var(--surface)',
            border: '1px solid var(--border)',
            borderRadius: '12px',
            boxShadow: '0 10px 25px rgba(0,0,0,0.1)',
            zIndex: 1000,
            overflow: 'hidden'
          }}>
            {filteredSuggestions.length > 0 ? (
              <div style={{ display: 'flex', flexDirection: 'column' }}>
                {filteredSuggestions.map((sug, idx) => (
                  <div 
                    key={idx}
                    onClick={() => {
                      setSearchQuery(sug.type === 'department' ? sug.text : sug.text.split(' - ')[0]);
                      setShowSuggestions(false);
                      router.push(sug.url);
                    }}
                    style={{
                      padding: '0.8rem 1rem',
                      borderBottom: idx < filteredSuggestions.length - 1 ? '1px solid var(--border)' : 'none',
                      cursor: 'pointer',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '0.75rem',
                      transition: 'background-color 0.2s'
                    }}
                    onMouseOver={(e) => e.currentTarget.style.backgroundColor = 'var(--surface-hover)'}
                    onMouseOut={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                  >
                    <div style={{ color: 'var(--primary)', opacity: 0.8 }}>
                      {sug.type === 'doctor' ? (
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle></svg>
                      ) : sug.type === 'patient' ? (
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
                      ) : (
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path><polyline points="9 22 9 12 15 12 15 22"></polyline></svg>
                      )}
                    </div>
                    <div style={{ color: 'var(--text-main)', fontSize: '0.9rem', fontWeight: '500' }}>
                      {sug.text}
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div style={{ padding: '1rem', textAlign: 'center', color: 'var(--text-muted)', fontSize: '0.9rem' }}>
                {t('no_results_found')}
              </div>
            )}
          </div>
        )}
      </div>
      <div className={styles.actions}>
        <button className={styles.themeToggle} onClick={toggleTheme} title={t('toggle_theme')}>
          {theme === 'light' ? (
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path>
            </svg>
          ) : (
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <circle cx="12" cy="12" r="5"></circle>
              <line x1="12" y1="1" x2="12" y2="3"></line>
              <line x1="12" y1="21" x2="12" y2="23"></line>
              <line x1="4.22" y1="4.22" x2="5.64" y2="5.64"></line>
              <line x1="18.36" y1="18.36" x2="19.78" y2="19.78"></line>
              <line x1="1" y1="12" x2="3" y2="12"></line>
              <line x1="21" y1="12" x2="23" y2="12"></line>
              <line x1="4.22" y1="19.78" x2="5.64" y2="18.36"></line>
              <line x1="18.36" y1="5.64" x2="19.78" y2="4.22"></line>
            </svg>
          )}
        </button>

        <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }} ref={notifRef}>
          <button 
            onClick={() => setIsNotifOpen(!isNotifOpen)} 
            className={styles.themeToggle}
            title={t('notifications')}
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path><path d="M13.73 21a2 2 0 0 1-3.46 0"></path></svg>
            {unreadCount > 0 && (
              <span style={{ position: 'absolute', top: '8px', right: '8px', width: '8px', height: '8px', backgroundColor: '#ef4444', borderRadius: '50%' }}></span>
            )}
          </button>
          
          {isNotifOpen && (
            <div style={{
              position: 'absolute', top: 'calc(100% + 10px)', right: '0', width: '320px', 
              backgroundColor: 'var(--surface)', border: '1px solid var(--border)', 
              borderRadius: '16px', boxShadow: '0 10px 40px -10px rgba(0,0,0,0.2)', 
              zIndex: 100, overflow: 'hidden'
            }}>
              <div style={{ padding: '1.2rem', background: 'linear-gradient(135deg, rgba(var(--primary-rgb), 0.1), transparent)', borderBottom: '1px solid var(--border)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <strong style={{ color: 'var(--text-main)', fontSize: '1.05rem', display: 'block' }}>{t('notifications')}</strong>
                {notifications.length > 0 && (
                  <button 
                    onClick={(e) => {
                      e.stopPropagation();
                      const hiddenNotifs = JSON.parse(localStorage.getItem('hiddenNotifs') || '[]');
                      notifications.forEach(n => { if (!hiddenNotifs.includes(n.id)) hiddenNotifs.push(n.id); });
                      localStorage.setItem('hiddenNotifs', JSON.stringify(hiddenNotifs));
                      setNotifications([]);
                      setUnreadCount(0);
                      window.dispatchEvent(new Event('hiddenNotifsUpdate'));
                    }}
                    style={{ background: 'none', border: 'none', color: '#ef4444', fontSize: '0.8rem', cursor: 'pointer', padding: '0', fontWeight: '500' }}
                    onMouseOver={(e) => e.currentTarget.style.textDecoration = 'underline'}
                    onMouseOut={(e) => e.currentTarget.style.textDecoration = 'none'}
                  >
                    {t('delete')}
                  </button>
                )}
              </div>
              <div style={{ maxHeight: '300px', overflowY: 'auto', padding: '0' }}>
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
                        cursor: 'pointer',
                        position: 'relative'
                      }}
                      onClick={() => {
                        setIsNotifOpen(false);
                        router.push('/patient-notifications');
                      }}
                    >
                      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.25rem' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                          {!notif.read && <div style={{ width: '6px', height: '6px', borderRadius: '50%', backgroundColor: '#ef4444' }}></div>}
                          <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                            {new Date(notif.createdAt).toLocaleString('tr-TR', { dateStyle: 'short', timeStyle: 'short' })}
                          </span>
                        </div>
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            const hiddenNotifs = JSON.parse(localStorage.getItem('hiddenNotifs') || '[]');
                            if (!hiddenNotifs.includes(notif.id)) hiddenNotifs.push(notif.id);
                            localStorage.setItem('hiddenNotifs', JSON.stringify(hiddenNotifs));
                            const newNotifs = notifications.filter(n => n.id !== notif.id);
                            setNotifications(newNotifs);
                            setUnreadCount(newNotifs.filter(n => !n.read).length);
                            window.dispatchEvent(new Event('hiddenNotifsUpdate'));
                          }}
                          style={{
                            background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer', padding: '2px', display: 'flex', alignItems: 'center', justifyContent: 'center'
                          }}
                          onMouseOver={(e) => e.currentTarget.style.color = '#ef4444'}
                          onMouseOut={(e) => e.currentTarget.style.color = 'var(--text-muted)'}
                          title={t('delete')}
                        >
                          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
                        </button>
                      </div>
                      <p style={{ color: notif.read ? 'var(--text-muted)' : 'var(--text-main)', fontSize: '0.85rem', margin: 0, fontWeight: notif.read ? '400' : '500', lineHeight: '1.4' }}>
                        {notif.message}
                      </p>
                    </div>
                  ))
                )}
              </div>
              <div style={{ padding: '1rem', borderTop: '1px solid var(--border)', backgroundColor: 'rgba(var(--background-rgb), 0.5)' }}>
                <button 
                  onClick={() => { setIsNotifOpen(false); router.push('/patient-notifications'); }} 
                  style={{ width: '100%', padding: '0.5rem', backgroundColor: 'transparent', color: 'var(--primary)', border: 'none', textAlign: 'center', cursor: 'pointer', fontWeight: '600'}}
                  onMouseOver={(e) => { e.currentTarget.style.textDecoration = 'underline'; }}
                  onMouseOut={(e) => { e.currentTarget.style.textDecoration = 'none'; }}
                >
                  {t('see_all_notifications')}
                </button>
              </div>
            </div>
          )}
        </div>
        
        <div className={styles.profileContainer} ref={dropdownRef}>
          <div className={styles.profile} onClick={() => setIsDropdownOpen(!isDropdownOpen)}>
            <div className={styles.avatar}>{initial}</div>
            <div className={styles.info}>
              <span className={styles.name}>{userProfile ? `${userProfile.firstName} ${userProfile.lastName}` : '...'}</span>
              <span className={styles.role}>{userProfile ? roleText : '...'}</span>
            </div>
          </div>

          {isDropdownOpen && (
            <div className={styles.dropdownMenu}>
              <div className={styles.dropdownHeader}>
                <strong>{userProfile ? `${userProfile.firstName} ${userProfile.lastName}` : t('unknown_patient')}</strong>
                <span>{userProfile?.email || '-'}</span>
              </div>
              <div className={styles.dropdownBody}>
                <div className={styles.dropdownItem}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-muted)' }}>
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path><circle cx="12" cy="7" r="4"></circle></svg>
                    <span className={styles.itemLabel}>{t('role')}</span>
                  </div>
                  <span className={styles.itemValue} style={{ color: 'var(--primary)', fontWeight: '700' }}>{roleText}</span>
                </div>
                <div className={styles.dropdownItem}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-muted)' }}>
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
                    <span className={styles.itemLabel}>{t('tc_no')}</span>
                  </div>
                  <span className={styles.itemValue}>{userProfile?.username || '-'}</span>
                </div>
                <div className={styles.dropdownItem}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-muted)' }}>
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path></svg>
                    <span className={styles.itemLabel}>{t('phone')}</span>
                  </div>
                  <span className={styles.itemValue}>{userProfile?.phoneNumber || '-'}</span>
                </div>
                <div 
                  className={styles.dropdownItem} 
                  style={{ cursor: 'pointer', color: 'var(--primary)', marginTop: '0.5rem', fontWeight: '500' }}
                  onClick={() => router.push('/settings')}
                >
                  <span className={styles.itemLabel}>⚙️</span>
                  <span className={styles.itemValue}>{t('settings')}</span>
                </div>
              </div>
              <div className={styles.dropdownFooter}>
                <button onClick={handleLogout} className={styles.logoutBtn}>
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
