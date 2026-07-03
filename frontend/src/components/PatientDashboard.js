'use client';
import { useState, useEffect } from 'react';
import Link from 'next/link';
import { AppointmentService, AuthService } from '../services/api';
import { useSettings } from '../context/SettingsContext';
import ConfirmModal from './ConfirmModal';
import EmptyState from './EmptyState';
import { toast } from './Toast';

export default function PatientDashboard() {
  const { t } = useSettings();
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [userProfile, setUserProfile] = useState(null);
  const [confirmModal, setConfirmModal] = useState({ isOpen: false, id: null });

  const fetchData = async () => {
    try {
      const profile = await AuthService.getMe();
      setUserProfile(profile);

      // Hastanın kendi randevularını çek
      const userAppointments = await AppointmentService.getByPatient(profile.id);
      
      // Sadece gelecek olanları ve "SCHEDULED" olanları filtrele
      const scheduled = userAppointments.filter(app => app.status === 'SCHEDULED');
      
      // Tarihe göre sırala (yaklaşan en üstte)
      scheduled.sort((a, b) => new Date(a.appointmentDate) - new Date(b.appointmentDate));
      
      setAppointments(scheduled);
    } catch (error) {
      console.error("Hasta verileri alınamadı:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleCancelClick = (id) => {
    setConfirmModal({ isOpen: true, id });
  };

  const executeCancel = async () => {
    try {
      await AppointmentService.updateStatus(confirmModal.id, 'CANCELLED');
      toast.success('Randevunuz başarıyla iptal edildi.');
      fetchData(); // Listeyi yenile
    } catch (error) {
      toast.error('İptal işlemi başarısız: ' + error.message);
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

  return (
    <div style={containerStyle}>
      <div style={welcomeSectionStyle}>
        <div style={welcomeTextContainer}>
          <h1 style={titleStyle}>
            {t('patient_welcome')} {userProfile?.firstName ? userProfile.firstName : t('patient')} 👋
          </h1>
          <p style={subtitleStyle}>
            {t('patient_dashboard_subtitle')}
          </p>
        </div>
        <Link href="/book-appointment" style={bigBookBtnStyle}>
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{marginRight: '8px'}}>
            <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
            <line x1="16" y1="2" x2="16" y2="6"></line>
            <line x1="8" y1="2" x2="8" y2="6"></line>
            <line x1="3" y1="10" x2="21" y2="10"></line>
            <line x1="12" y1="14" x2="12" y2="18"></line>
            <line x1="8" y1="16" x2="16" y2="16"></line>
          </svg>
          {t('book_appointment')}
        </Link>
      </div>

      <div style={upcomingSectionStyle}>
        <h2 style={sectionTitleStyle}>{t('upcoming_appointments')}</h2>
        
        {loading ? (
          <p style={emptyStateStyle}>{t('loading')}</p>
        ) : appointments.length === 0 ? (
          <EmptyState 
            icon="📅" 
            title={t('no_upcoming_appointments')} 
            description={t('no_appointments_desc')} 
          />
        ) : (
          <div style={appointmentsGridStyle}>
            {appointments.map(app => (
              <div key={app.id} style={appointmentCardStyle}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <div>
                    <div style={appDateStyle}>{formatDate(app.appointmentDate)}</div>
                    <div style={appDoctorStyle}>Dr. {app.doctorFullName}</div>
                    <div style={appDeptStyle}>{t(app.departmentName)}</div>
                    {app.notes && <div style={appNotesStyle}>{t('appointment_notes')}: {app.notes}</div>}
                  </div>
                  <button 
                    onClick={() => handleCancelClick(app.id)} 
                    style={cancelBtnStyle}
                    title="Randevuyu İptal Et"
                  >
                    İptal Et
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      <ConfirmModal
        isOpen={confirmModal.isOpen}
        title="Randevu İptali"
        message="Bu randevuyu iptal etmek istediğinize emin misiniz? Bu işlem geri alınamaz."
        onConfirm={executeCancel}
        onCancel={() => setConfirmModal({ isOpen: false, id: null })}
        confirmText="Evet, İptal Et"
        type="danger"
      />
    </div>
  );
}

// Styles
const containerStyle = {
  display: 'flex',
  flexDirection: 'column',
  gap: '1rem',
  flex: 1,
  minHeight: 0
};

const welcomeSectionStyle = {
  backgroundColor: 'var(--surface)',
  borderRadius: '16px',
  padding: '1.25rem',
  textAlign: 'center',
  border: '1px solid var(--border)',
  boxShadow: 'var(--shadow-md)',
  display: 'flex',
  flexDirection: 'row',
  justifyContent: 'space-between',
  alignItems: 'center',
  backgroundImage: 'linear-gradient(to right bottom, var(--surface), var(--background))',
  flexShrink: 0
};

const welcomeTextContainer = {
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'flex-start',
  textAlign: 'left'
};

const titleStyle = {
  fontSize: '1.5rem',
  fontWeight: '800',
  color: 'var(--text-main)',
  margin: 0
};

const subtitleStyle = {
  fontSize: '0.9rem',
  color: 'var(--text-muted)',
  maxWidth: '600px',
  lineHeight: '1.2',
  margin: '0.25rem 0 0 0'
};

const bigBookBtnStyle = {
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  backgroundColor: 'var(--primary)',
  color: '#ffffff',
  padding: '0.75rem 1.5rem',
  fontSize: '1rem',
  fontWeight: '700',
  borderRadius: '12px',
  textDecoration: 'none',
  boxShadow: '0 4px 6px -1px rgba(var(--primary-rgb), 0.3)',
  transition: 'transform 0.2s, box-shadow 0.2s',
  flexShrink: 0
};

const upcomingSectionStyle = {
  display: 'flex',
  flexDirection: 'column',
  flex: 1,
  minHeight: 0,
  backgroundColor: 'var(--surface)',
  borderRadius: '16px',
  border: '1px solid var(--border)',
  padding: '1.5rem',
  overflow: 'hidden'
};

const sectionTitleStyle = {
  fontSize: '1.25rem',
  fontWeight: '700',
  color: 'var(--text-main)',
  borderBottom: '1px solid var(--border)',
  paddingBottom: '0.5rem',
  marginBottom: '1rem',
  flexShrink: 0
};

const emptyStateStyle = {
  color: 'var(--text-muted)',
  fontStyle: 'italic'
};

const emptyCardStyle = {
  backgroundColor: 'var(--surface)',
  border: '1px dashed var(--border)',
  borderRadius: '12px',
  padding: '4rem 2rem',
  textAlign: 'center',
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  gap: '1rem',
  color: 'var(--text-muted)'
};

const emptyIconStyle = {
  fontSize: '3rem',
  opacity: '0.5'
};

const appointmentsGridStyle = {
  display: 'grid',
  gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))',
  gap: '1.25rem',
  overflowY: 'auto',
  flex: 1
};

const appointmentCardStyle = {
  backgroundColor: 'var(--surface)',
  border: '1px solid var(--border)',
  borderLeft: '4px solid var(--primary)',
  borderRadius: '8px',
  padding: '1.5rem',
  boxShadow: 'var(--shadow-sm)',
  display: 'flex',
  flexDirection: 'column',
  gap: '0.5rem',
  transition: 'transform 0.2s'
};

const appDateStyle = {
  fontSize: '1.1rem',
  fontWeight: '700',
  color: 'var(--text-main)'
};

const appDoctorStyle = {
  fontSize: '1rem',
  fontWeight: '600',
  marginBottom: '0.25rem',
  color: 'var(--text-main)'
};

const appDeptStyle = {
  fontSize: '0.9rem',
  color: 'var(--text-muted)',
  marginBottom: '0.5rem',
  display: 'inline-block',
  backgroundColor: 'var(--background)',
  padding: '0.2rem 0.5rem',
  borderRadius: '4px'
};

const appNotesStyle = {
  fontSize: '0.85rem',
  fontStyle: 'italic',
  color: 'var(--text-muted)',
  marginTop: '0.5rem',
  borderTop: '1px dashed var(--border)',
  paddingTop: '0.5rem'
};

const cancelBtnStyle = {
  backgroundColor: '#fef2f2',
  color: '#ef4444',
  border: '1px solid #fca5a5',
  padding: '0.4rem 0.8rem',
  borderRadius: '6px',
  fontSize: '0.85rem',
  fontWeight: '600',
  cursor: 'pointer',
  transition: 'all 0.2s'
};
