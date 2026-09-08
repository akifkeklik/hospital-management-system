'use client';
import { useState, useCallback, useRef, useEffect } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useSettings } from '../context/SettingsContext';
import { DepartmentService } from '../services/api';
import { useAuth } from '../context/AuthContext';
import styles from './Sidebar.module.css';

export default function Sidebar() {
  const pathname = usePathname();
  const { t } = useSettings();
  const [isExpanded, setIsExpanded] = useState(false);

  const [departments, setDepartments] = useState([]);
  const [polyclinics, setPolyclinics] = useState([]);
  const [hoveredMenu, setHoveredMenu] = useState(null);
  const [hoveredDepartment, setHoveredDepartment] = useState(null);
  const collapseTimer = useRef(null);

  const { user, role } = useAuth();
  
  // Format the role for the sidebar logic
  let formattedRole = null;
  if (role) {
    formattedRole = role.startsWith('ROLE_') ? role.replace('ROLE_', '') : role;
    if (formattedRole === 'HEKIM' || formattedRole === 'HEKİM') formattedRole = 'DOCTOR';
    if (formattedRole === 'HASTA') formattedRole = 'PATIENT';
  }

  useEffect(() => {
    const fetchPolyclinics = async () => {
      if (formattedRole === 'ADMIN') {
         try {
            const polyData = await import('../services/api').then(m => m.PolyclinicService.getAll());
            setPolyclinics(polyData || []);
         } catch (err) {
            console.error("Could not fetch polyclinics for sidebar", err);
         }
      }
    };
    fetchPolyclinics();
  }, [formattedRole]);

  useEffect(() => {
    const fetchDepartments = async () => {
      try {
        const deptData = await DepartmentService.getAll(0, 100);
        setDepartments(deptData.content || deptData || []);
      } catch (err) {
        console.error("Could not fetch departments for sidebar", err);
      }
    };
    fetchDepartments();
  }, []);

  const handleMouseEnter = useCallback(() => {
    if (collapseTimer.current) clearTimeout(collapseTimer.current);
    setIsExpanded(true);
  }, []);

  const handleMouseLeave = useCallback(() => {
    collapseTimer.current = setTimeout(() => setIsExpanded(false), 200);
  }, []);

  const allNavItems = [
    { 
      name: t('dashboard'), path: '/', color: '#3b82f6', roles: ['ADMIN', 'DOCTOR', 'PATIENT'],
      icon: <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg> 
    },
    { 
      name: t('departments'), path: '/departments', color: '#10b981', roles: ['ADMIN'],
      icon: <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="4" y="2" width="16" height="20" rx="2" ry="2"/><path d="M9 22v-4h6v4"/><path d="M8 6h.01"/><path d="M16 6h.01"/><path d="M12 6h.01"/><path d="M12 10h.01"/><path d="M12 14h.01"/><path d="M16 10h.01"/><path d="M16 14h.01"/><path d="M8 10h.01"/><path d="M8 14h.01"/></svg> 
    },
    { 
      name: t('patients'), path: '/patients', color: '#f59e0b', roles: ['ADMIN'],
      icon: <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg> 
    },
    { 
      name: t('doctors'), path: '/doctors', color: '#8b5cf6', roles: ['ADMIN', 'PATIENT'],
      icon: <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg> 
    },
    { 
      name: t('doctor_requests'), path: '/admin/doctor-requests', color: '#ec4899', roles: ['ADMIN'],
      icon: <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16c0 1.1.9 2 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg> 
    },
    { 
      name: t('admin_leaves'), path: '/admin/doctor-leaves', color: '#0ea5e9', roles: ['ADMIN'],
      icon: <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M19 4H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6a2 2 0 0 0-2-2z"/><path d="M16 2v4"/><path d="M8 2v4"/><path d="M3 10h18"/></svg> 
    },
    { 
      name: t('polyclinics'), path: '/admin/polyclinics', color: '#f97316', roles: ['ADMIN'],
      icon: <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M3 21h18"/><path d="M5 21V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2v16"/><path d="M9 21v-4a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2v4"/></svg> 
    },
    {
      name: t('doctor_queue'), path: '/doctor/queue', color: '#8b5cf6', roles: ['DOCTOR'],
      icon: <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg> 
    },
    {
      name: t('doctor_leaves'), path: '/doctor/leaves', color: '#14b8a6', roles: ['DOCTOR'],
      icon: <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg> 
    },
    { 
      name: t('appointments'), path: '/appointments', color: '#ef4444', roles: ['ADMIN', 'DOCTOR', 'PATIENT'],
      icon: <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg> 
    },
    {
      name: t('patient_records'), path: '/patient-records', color: '#10b981', roles: ['PATIENT'],
      icon: <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg>
    },
    {
      name: t('notifications'), path: '/patient-notifications', color: '#f59e0b', roles: ['ADMIN', 'DOCTOR', 'PATIENT'],
      icon: <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>
    },
    { 
      name: t('settings'), path: '/settings', color: '#6b7280', roles: ['ADMIN', 'DOCTOR', 'PATIENT'],
      icon: <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg> 
    }
  ];

  const navItems = formattedRole ? allNavItems.filter(item => item.roles.includes(formattedRole)) : [];

  return (
    <aside 
      className={`${styles.sidebar} ${isExpanded ? styles.expanded : ''}`}
      onMouseEnter={handleMouseEnter}
      onMouseLeave={handleMouseLeave}
    >
      {/* Logo */}
      <Link href="/" className={styles.logo}>
        <div className={styles.logoIcon}>
          <svg viewBox="0 0 24 24" fill="#ffffff" stroke="rgba(255,255,255,0.4)" strokeWidth="0.5">
            <path d="M 19 3 A 10 10 0 1 0 19 21 A 9.5 9.5 0 1 1 19 3 Z" />
          </svg>
        </div>
        <div className={styles.logoText}>
          <h2>{t('logo_title')}</h2>
          <p>{t('logo_desc')}</p>
        </div>
      </Link>

      {/* Nav */}
      <nav className={styles.nav}>
        {navItems.map((item) => {
          const isActive = pathname === item.path || (item.path !== '/' && pathname.startsWith(item.path));
          
          return (
            <div 
              key={item.path} 
              style={{ position: 'relative' }}
              onMouseEnter={() => setHoveredMenu(item.name)}
              onMouseLeave={() => setHoveredMenu(null)}
            >
              <Link 
                href={item.path}
                className={`${styles.navItem} ${isActive ? styles.active : ''}`}
                title={!isExpanded ? item.name : ''}
              >
                {isActive && <span className={styles.activeBar} />}
                <span 
                  className={styles.icon}
                  style={{ 
                    color: isActive ? '#fff' : item.color,
                    backgroundColor: isActive ? undefined : `${item.color}12`,
                    borderColor: isActive ? 'transparent' : `${item.color}25`
                  }}
                >
                  {item.icon}
                </span>
                <span className={styles.navName}>{item.name}</span>
              </Link>
              
              {/* Flyout Menu for Polyclinics - LEVEL 1 (Departments) */}
              {item.name === (t('polyclinics')) && hoveredMenu === item.name && departments.length > 0 && (
                <div 
                  className={styles.flyoutMenu} 
                  style={{
                    position: 'absolute',
                    left: '100%',
                    top: '0',
                    marginLeft: '10px',
                    backgroundColor: 'var(--surface)',
                    border: '1px solid var(--border)',
                    borderRadius: '12px',
                    boxShadow: '0 10px 25px rgba(0,0,0,0.1)',
                    width: '240px',
                    zIndex: 1000,
                    padding: '0.5rem 0'
                  }}
                  onMouseLeave={() => setHoveredDepartment(null)}
                >
                  <div style={{ padding: '0.5rem 1rem', borderBottom: '1px solid var(--border)', marginBottom: '0.5rem' }}>
                    <strong style={{ color: 'var(--primary)', fontSize: '0.85rem', textTransform: 'uppercase' }}>{t('departments')}</strong>
                  </div>
                  <div style={{ maxHeight: '400px', overflowY: 'auto' }}>
                    {departments.map(dept => {
                      const deptPolyclinics = polyclinics.filter(p => p.departmentId === dept.id);
                      return (
                        <div 
                          key={dept.id} 
                          style={{ position: 'relative' }}
                          onMouseEnter={() => setHoveredDepartment(dept.id)}
                        >
                          <Link 
                            href={`/admin/polyclinics?departmentId=${dept.id}`} 
                            style={{ 
                              display: 'flex', 
                              alignItems: 'center',
                              justifyContent: 'space-between',
                              padding: '0.5rem 1rem', 
                              color: hoveredDepartment === dept.id ? 'var(--primary)' : 'var(--text-main)', 
                              textDecoration: 'none', 
                              fontSize: '0.9rem',
                              backgroundColor: hoveredDepartment === dept.id ? 'var(--background)' : 'transparent',
                              transition: 'all 0.2s'
                            }}
                          >
                            <span>{dept.name}</span>
                            {deptPolyclinics.length > 0 && (
                              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ opacity: 0.5 }}>
                                <polyline points="9 18 15 12 9 6"></polyline>
                              </svg>
                            )}
                          </Link>

                          {/* Flyout Menu for Polyclinics - LEVEL 2 (Actual Polyclinics/Rooms) */}
                          {hoveredDepartment === dept.id && deptPolyclinics.length > 0 && (
                            <div 
                              style={{
                                position: 'absolute',
                                left: '100%',
                                top: '0',
                                backgroundColor: 'var(--surface)',
                                border: '1px solid var(--border)',
                                borderRadius: '12px',
                                boxShadow: '0 10px 25px rgba(0,0,0,0.1)',
                                width: '220px',
                                zIndex: 1001,
                                padding: '0.5rem 0',
                                maxHeight: '300px',
                                overflowY: 'auto'
                              }}
                            >
                              <div style={{ padding: '0.5rem 1rem', borderBottom: '1px solid var(--border)', marginBottom: '0.5rem' }}>
                                <strong style={{ color: 'var(--primary)', fontSize: '0.8rem', opacity: 0.8 }}>{dept.name} - {t('polyclinics')}</strong>
                              </div>
                              {deptPolyclinics.map(poly => (
                                <Link 
                                  key={poly.id} 
                                  href={`/admin/polyclinics?departmentId=${dept.id}&highlight=${poly.id}`} 
                                  style={{ 
                                    display: 'block', 
                                    padding: '0.5rem 1rem', 
                                    color: 'var(--text-main)', 
                                    textDecoration: 'none', 
                                    fontSize: '0.85rem' 
                                  }}
                                  onMouseOver={(e) => e.currentTarget.style.backgroundColor = 'var(--background)'}
                                  onMouseOut={(e) => e.currentTarget.style.backgroundColor = 'transparent'}
                                >
                                  • {poly.name}
                                </Link>
                              ))}
                            </div>
                          )}
                        </div>
                      );
                    })}
                  </div>
                </div>
              )}
            </div>
          );
        })}
      </nav>
      
      <div className={styles.footer}>
        <p className={styles.footerVersion}>v1.0</p>
        <span className={styles.footerText}>© 2026 HRS</span>
      </div>
    </aside>
  );
}
