'use client';
import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { AppointmentService, AuthService } from '../services/api';
import { useSettings } from '../context/SettingsContext';
import ConfirmModal from './ConfirmModal';
import EmptyState from './EmptyState';
import LoadingScreen from './LoadingScreen';
import { useSpeech } from '../hooks/useSpeech';
import HospitalMap from './HospitalMap';
import Pagination from './Pagination';
import { getTimeFilterParams } from '../utils/dateFilters';
import { useApi } from '../hooks/useApi';
import { useAuth } from '../context/AuthContext';
import styles from './PatientDashboard.module.css';

export default function PatientDashboard() {
  const { t, tErr } = useSettings();
  const { speak, isSpeaking, stop } = useSpeech();
  const router = useRouter();
  const [page, setPage] = useState(0);
  const [confirmModal, setConfirmModal] = useState({ isOpen: false, id: null });
  const [quickSearch, setQuickSearch] = useState('');
  const [timeFilter, setTimeFilter] = useState('all');
  const [mapDept, setMapDept] = useState(null);
  const { user: userProfile } = useAuth();

  const fetchDashboardData = useCallback(async (signal, currentPage, currentTimeFilter) => {
    const attempt = async (retryCount = 0) => {
      try {
        // Hastanın kendi randevularını çek (Filtreler backend'e gidiyor)
        const filters = { status: 'SCHEDULED', ...getTimeFilterParams(currentTimeFilter) };
        const userAppointmentsResponse = await AppointmentService.getByPatient(userProfile.id, currentPage, 100, filters, { signal });
        const userAppointments = userAppointmentsResponse.items || [];
        const totalPages = userAppointmentsResponse.totalPages || 0;
        
        // Tarihe göre sırala (yaklaşan en üstte)
        userAppointments.sort((a, b) => new Date(a.appointmentDate) - new Date(b.appointmentDate));
        
        // Sadece bugünkü randevular için bekleme süresini çek
        const now = new Date();
        const newWaitTimes = {};
        for (const app of userAppointments) {
          const appDate = new Date(app.appointmentDate);
          if (appDate.getDate() === now.getDate() && appDate.getMonth() === now.getMonth() && appDate.getFullYear() === now.getFullYear()) {
            try {
              const wt = await AppointmentService.getWaitEstimate(app.id, { signal });
              newWaitTimes[app.id] = wt;
            } catch (e) {
              if (e.name === 'AbortError') throw e;
              console.error("Wait time fetch error:", e);
            }
          }
        }
        return { userAppointments, totalPages, waitTimes: newWaitTimes };
      } catch (error) {
        if (error.name === 'AbortError') throw error;
        console.error("Hasta verileri alınamadı:", error);
        if (retryCount < 2) {
          await new Promise(r => setTimeout(r, 3000));
          return attempt(retryCount + 1);
        }
        throw error;
      }
    };
    return attempt(0);
  }, [userProfile]);

  const { data, loading, execute } = useApi(fetchDashboardData, { userAppointments: [], totalPages: 0, waitTimes: {} });

  const appointments = data?.userAppointments || [];
  const totalPages = data?.totalPages || 0;
  const waitTimes = data?.waitTimes || {};

  useEffect(() => {
    execute(page, timeFilter).catch(() => {});
  }, [page, timeFilter, execute]);

  const handleCancelClick = (id) => {
    setConfirmModal({ isOpen: true, id });
  };

  const executeCancel = async () => {
    try {
      await AppointmentService.updateStatus(confirmModal.id, 'CANCELLED');
      toast.success(t('appointment_cancelled'));
      execute(page, timeFilter);
    } catch (error) {
      toast.error(t('cancel_failed') + ': ' + error.message);
    } finally {
      setConfirmModal({ isOpen: false, id: null });
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('tr-TR', { 
      year: 'numeric', month: 'long', day: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  };

  const handleQuickSearch = (e) => {
    e.preventDefault();
    if (quickSearch.trim()) {
      router.push(`/book-appointment?search=${encodeURIComponent(quickSearch.trim())}`);
    }
  };

  return (
    <div className={styles.container}>
      <div className={styles.heroBanner}>
        <div className={styles.heroContent}>
          <h1 className={styles.heroTitle}>
            {t('hero_welcome')} <br/>
            <span>{userProfile?.firstName ? userProfile.firstName : t('patient')}</span>
          </h1>
          <p className={styles.heroSubtitle}>
            {t('patient_dashboard_subtitle')}
          </p>
          
          <form onSubmit={handleQuickSearch} className={styles.heroSearchForm}>
            <div className={styles.heroSearchIcon}>
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line>
              </svg>
            </div>
            <input 
              type="text"
              placeholder={t('hero_search_placeholder')}
              value={quickSearch}
              onChange={(e) => setQuickSearch(e.target.value)}
              className={styles.heroSearchInput}
            />
            <button type="submit" className={styles.heroSearchBtn}>{t('search')}</button>
          </form>
        </div>
        
        <div className={styles.heroAction}>
          <Link href="/book-appointment" className={styles.bigBookBtn}>
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{marginRight: '8px'}}>
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
              <line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line>
              <line x1="3" y1="10" x2="21" y2="10"></line>
              <line x1="12" y1="14" x2="12" y2="18"></line><line x1="8" y1="16" x2="16" y2="16"></line>
            </svg>
            {t('book_appointment')}
          </Link>
        </div>
      </div>

      <div className={styles.upcomingSection}>
        <div className={styles.sectionHeader}>
          <h2 className={styles.sectionTitle}>{t('upcoming_appointments')}</h2>
          <select 
            value={timeFilter} 
            onChange={(e) => {
              setTimeFilter(e.target.value);
              setPage(0);
            }}
            className={styles.timeFilter}
          >
            <option value="all">{t('filter_all_time')}</option>
            <option value="today">{t('filter_today')}</option>
            <option value="week">{t('filter_this_week')}</option>
            <option value="month">{t('filter_this_month')}</option>
            <option value="3months">{t('filter_three_months')}</option>
            <option value="6months">{t('filter_six_months')}</option>
          </select>
        </div>
        
        {loading ? (
          <LoadingScreen />
        ) : appointments.length === 0 ? (
          <EmptyState 
            title={t('empty_state_title')} 
            description={t('empty_state_desc')} 
            customSvg={<span/>}
          />
        ) : (
          <div className={styles.appointmentsGrid}>
            {appointments.map(app => (
              <div key={app.id} className={styles.appointmentCard}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <div>
                    <div className={styles.appDate}>{formatDate(app.appointmentDate)}</div>
                    <div className={styles.appDoctor}>Dr. {app.doctorFullName}</div>
                    <div className={styles.appDept}>{t(app.departmentName)}</div>
                    {app.notes && <div className={styles.appNotes}>{t('appointment_notes')}: {app.notes}</div>}
                    {waitTimes[app.id] && (
                      <div className={`${styles.waitBadge} ${styles['wait' + waitTimes[app.id].busyLevel]}`}>
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <circle cx="12" cy="12" r="10"></circle><polyline points="12 6 12 12 16 14"></polyline>
                        </svg>
                        {t('est_wait')}: {waitTimes[app.id].estimatedMinutes} {t('minutes')} ({t('queue_pos')}: {waitTimes[app.id].queuePosition})
                      </div>
                    )}
                    <div style={{ marginTop: '0.75rem' }}>
                      <button onClick={() => setMapDept(app.departmentName)} className={styles.mapBtn}>
                        📍 {t('view_on_map')}
                      </button>
                    </div>
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                    <button 
                      onClick={() => speak(`${t('appointment')}: ${formatDate(app.appointmentDate)}, Doktor ${app.doctorFullName}, ${t(app.departmentName)} ${t('department')}. ${waitTimes[app.id] ? t('est_wait') + ' ' + waitTimes[app.id].estimatedMinutes + ' ' + t('minutes') : ''}`)}
                      className={styles.speechBtn}
                      title={t('read_aloud') || "Sesli Oku"}
                    >
                      🔊
                    </button>
                    <button 
                      onClick={() => handleCancelClick(app.id)} 
                      className={styles.cancelBtn}
                      title={t('cancel_appointment')}
                    >
                      {t('cancel_appointment_short')}
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
        
        <Pagination 
          page={page} 
          totalPages={totalPages} 
          onPageChange={setPage} 
        />
      </div>

      <ConfirmModal
        isOpen={confirmModal.isOpen}
        title={t('cancel_appointment_title')}
        message={t('cancel_appointment_confirm_msg')}
        onConfirm={executeCancel}
        onCancel={() => setConfirmModal({ isOpen: false, id: null })}
        confirmText={t('yes_cancel')}
        type="danger"
      />

      <HospitalMap
        departmentName={mapDept}
        isOpen={!!mapDept}
        onClose={() => setMapDept(null)}
      />
    </div>
  );
}
