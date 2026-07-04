'use client';
import { useState, useEffect } from 'react';
import { AppointmentService, AuthService } from '../services/api';
import { useSettings } from '../context/SettingsContext';
import ConfirmModal from './ConfirmModal';
import EmptyState from './EmptyState';
import LoadingScreen from './LoadingScreen';
import styles from './DoctorDashboard.module.css'; // Özel stil

export default function DoctorDashboard() {
  const { t } = useSettings();
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [profile, setProfile] = useState(null);
  const [timeFilter, setTimeFilter] = useState('all'); // all, today, week, month, 3months, 6months

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        const userProfile = await AuthService.getMe();
        setProfile(userProfile);

        if (userProfile && userProfile.id) {
            // Randevuları doctorId'ye göre çekeceğiz ama API'de getByDoctor() var mı?
            // AppointmentController.java'da GET /api/appointments/doctor/{doctorId} var mı?
            // Var olduğunu farz ediyoruz. Eğer yoksa, AppointmentService.getAll ve filtreleme yapalım.
            // Fakat backend'i kontrol etmemiz lazım.
            const data = await AppointmentService.getAll(0, 100);
            
            // Eğer backend'de role-based veya doctor spesifik endpoint yoksa, manuel filtre:
            // Sadece bu doktorun randevuları
            const myAppointments = data.content ? data.content.filter(a => a.doctorName === (userProfile.firstName + " " + userProfile.lastName) || a.doctorId === userProfile.id) : [];
            setAppointments(myAppointments);
        }
      } catch (err) {
        console.error(err);
        setError(t('error_loading_data'));
      } finally {
        setLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  if (loading) {
    return <LoadingScreen fullScreen={true} />;
  }

  if (error) {
    return (
      <div className={styles.errorState}>
        <div className={styles.errorIcon}>⚠️</div>
        <h3>{error}</h3>
      </div>
    );
  }

  return (
    <div className={styles.dashboardContainer}>
      
      {/* Karşılama Alanı */}
      <div className={styles.welcomeSection}>
        <div>
          <h1 className={styles.welcomeTitle}>{t('welcome')}, Dr. {profile?.firstName} {profile?.lastName}</h1>
          <p className={styles.welcomeSubtitle}>{t('doctor_dashboard_subtitle')}</p>
        </div>
        <div className={styles.dateBadge}>
          {new Date().toLocaleDateString()}
        </div>
      </div>

      <div className={styles.contentGrid}>
        
        {/* Sol Kolon: Randevular */}
        <div className={styles.card}>
          <div className={styles.cardHeader} style={{ padding: '1.5rem', borderBottom: '1px solid var(--border)' }}>
            <h2 className={styles.cardTitle} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', width: '100%', margin: 0 }}>
              <span style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', fontSize: '1.25rem', fontWeight: '700' }}>
                <span className={styles.icon} style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', width: '36px', height: '36px', backgroundColor: 'rgba(99, 102, 241, 0.1)', color: '#6366f1', borderRadius: '10px' }}>
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                </span>
                {t('my_appointments')}
              </span>
              <select 
                value={timeFilter} 
                onChange={(e) => setTimeFilter(e.target.value)}
                style={{ padding: '0.6rem 2rem 0.6rem 1rem', borderRadius: '10px', border: '1px solid var(--border)', backgroundColor: 'var(--surface)', color: 'var(--text-main)', fontSize: '0.9rem', fontWeight: '500', cursor: 'pointer', outline: 'none', appearance: 'none', backgroundImage: 'url("data:image/svg+xml,%3Csvg xmlns=\'http://www.w3.org/2000/svg\' width=\'16\' height=\'16\' viewBox=\'0 0 24 24\' fill=\'none\' stroke=\'%236b7280\' stroke-width=\'2\' stroke-linecap=\'round\' stroke-linejoin=\'round\'%3E%3Cpolyline points=\'6 9 12 15 18 9\'%3E%3C/polyline%3E%3C/svg%3E")', backgroundRepeat: 'no-repeat', backgroundPosition: 'right 0.75rem center', backgroundSize: '16px', minWidth: '160px', width: 'auto', boxShadow: '0 2px 4px rgba(0,0,0,0.02)' }}
              >
                <option value="all">{t('filter_all_time')}</option>
                <option value="today">{t('filter_today')}</option>
                <option value="week">{t('filter_this_week')}</option>
                <option value="month">{t('filter_this_month')}</option>
                <option value="3months">{t('filter_three_months')}</option>
                <option value="6months">{t('filter_six_months')}</option>
              </select>
            </h2>
          </div>
          
          <div className={styles.cardBody} style={{ padding: '1.5rem', flex: 1, overflowY: 'auto' }}>
            {appointments.filter(app => {
              if (timeFilter === 'all') return true;
              const appDate = new Date(app.appointmentDate);
              const now = new Date();
              const diffMs = appDate - now;
              const diffDays = diffMs / (1000 * 60 * 60 * 24);
              
              if (timeFilter === 'today') return diffDays >= 0 && diffDays < 1;
              if (timeFilter === 'week') return diffDays >= 0 && diffDays <= 7;
              if (timeFilter === 'month') return diffDays >= 0 && diffDays <= 30;
              if (timeFilter === '3months') return diffDays >= 0 && diffDays <= 90;
              if (timeFilter === '6months') return diffDays >= 0 && diffDays <= 180;
              return true;
            }).length === 0 ? (
              <EmptyState 
                title={t('empty_state_title')} 
                description={t('empty_state_desc')} 
              />
            ) : (
              <div className={styles.appointmentList} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                {appointments.filter(app => {
                  if (timeFilter === 'all') return true;
                  const appDate = new Date(app.appointmentDate);
                  const now = new Date();
                  const diffMs = appDate - now;
                  const diffDays = diffMs / (1000 * 60 * 60 * 24);
                  
                  if (timeFilter === 'today') return diffDays >= 0 && diffDays < 1;
                  if (timeFilter === 'week') return diffDays >= 0 && diffDays <= 7;
                  if (timeFilter === 'month') return diffDays >= 0 && diffDays <= 30;
                  if (timeFilter === '3months') return diffDays >= 0 && diffDays <= 90;
                  if (timeFilter === '6months') return diffDays >= 0 && diffDays <= 180;
                  return true;
                }).map((app) => {
                  const dateObj = new Date(app.appointmentDate);
                  const formattedDate = dateObj.toLocaleDateString('tr-TR', { day: '2-digit', month: 'long', year: 'numeric' });
                  let formattedTime = app.appointmentTime;
                  if (!formattedTime && app.appointmentDate.includes('T')) {
                    formattedTime = app.appointmentDate.split('T')[1].substring(0, 5);
                  } else if (!formattedTime) {
                    formattedTime = dateObj.toLocaleTimeString('tr-TR', { hour: '2-digit', minute: '2-digit' });
                  }

                  const patName = app.patientName || app.patientFullName || app.patientFirstName + ' ' + app.patientLastName;
                  const validPatName = patName && !patName.includes('undefined') ? patName : t('unknown_patient');

                  return (
                    <div key={app.id} className={styles.appointmentItem} style={{ display: 'flex', alignItems: 'center', padding: '1.25rem', backgroundColor: 'var(--surface)', border: '1px solid var(--border)', borderRadius: '12px', transition: 'transform 0.2s, box-shadow 0.2s', boxShadow: '0 2px 4px rgba(0,0,0,0.02)' }} onMouseEnter={(e) => { e.currentTarget.style.transform = 'translateY(-2px)'; e.currentTarget.style.boxShadow = '0 8px 16px rgba(0,0,0,0.06)'; e.currentTarget.style.borderColor = 'var(--primary)'; }} onMouseLeave={(e) => { e.currentTarget.style.transform = 'none'; e.currentTarget.style.boxShadow = '0 2px 4px rgba(0,0,0,0.02)'; e.currentTarget.style.borderColor = 'var(--border)'; }}>
                      <div className={styles.appointmentTime} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', paddingRight: '1.5rem', borderRight: '1px solid var(--border)', minWidth: '110px' }}>
                        <div className={styles.timeValue} style={{ fontSize: '1.25rem', fontWeight: '700', color: 'var(--primary)', letterSpacing: '0.05em' }}>{formattedTime}</div>
                        <div className={styles.dateValue} style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '0.25rem', fontWeight: '500' }}>{formattedDate}</div>
                      </div>
                      <div className={styles.appointmentDetails} style={{ paddingLeft: '1.5rem', flex: 1, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem' }}>
                          <h4 style={{ margin: 0, fontSize: '1.1rem', fontWeight: '600', color: 'var(--text-main)' }}>
                            {validPatName}
                          </h4>
                          <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0Z"/><circle cx="12" cy="10" r="3"/></svg>
                            {app.departmentName ? t(app.departmentName) : t('appointment')}
                          </span>
                        </div>
                        <span className={styles.statusBadge} style={{ 
                          padding: '0.4rem 1rem', 
                          borderRadius: '20px', 
                          fontSize: '0.75rem', 
                          fontWeight: '700', 
                          backgroundColor: app.status === 'COMPLETED' ? 'rgba(16, 185, 129, 0.1)' : app.status === 'CANCELLED' ? 'rgba(239, 68, 68, 0.1)' : 'rgba(99, 102, 241, 0.1)', 
                          color: app.status === 'COMPLETED' ? '#10b981' : app.status === 'CANCELLED' ? '#ef4444' : '#6366f1',
                          border: app.status === 'COMPLETED' ? '1px solid rgba(16, 185, 129, 0.2)' : app.status === 'CANCELLED' ? '1px solid rgba(239, 68, 68, 0.2)' : '1px solid rgba(99, 102, 241, 0.2)',
                          letterSpacing: '0.03em'
                        }}>
                          {t(app.status ? `status_${app.status.toLowerCase()}` : 'status_scheduled') || app.status}
                        </span>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        </div>

        {/* Sağ Kolon: İstatistikler */}
        <div className={styles.statsCard}>
          <h3>{t('daily_summary')}</h3>
          <div className={styles.statItem}>
            <span>{t('total_appointments')}</span>
            <span className={styles.statNumber}>{appointments.length}</span>
          </div>
          
          <div style={{ marginTop: '2rem', paddingTop: '1.5rem', borderTop: '1px solid var(--border)' }}>
            <h4 style={{ marginBottom: '1rem', color: 'var(--text-main)', fontSize: '0.95rem' }}>{t('quick_actions')}</h4>
            <a 
              href="/doctor/leaves" 
              style={{ 
                display: 'block', 
                width: '100%', 
                padding: '0.75rem 1rem', 
                backgroundColor: 'var(--primary)', 
                color: 'white', 
                textAlign: 'center', 
                borderRadius: '8px', 
                textDecoration: 'none', 
                fontWeight: '600', 
                transition: 'opacity 0.2s' 
              }}
              onMouseOver={(e) => e.target.style.opacity = 0.9}
              onMouseOut={(e) => e.target.style.opacity = 1}
            >
              📅 {t('create_leave_request')}
            </a>
          </div>
        </div>
        
      </div>
    </div>
  );
}
