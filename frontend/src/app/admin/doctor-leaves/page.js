'use client';
import { useEffect, useState } from 'react';
import { DoctorLeaveService, DoctorService } from '../../../services/api';
import { toast } from '../../../components/Toast';
import { useSettings } from '../../../context/SettingsContext';
import ConfirmModal from '../../../components/ConfirmModal';
import styles from '../../shared.module.css';

export default function DoctorLeavesPage() {
  const { t } = useSettings();
  const [leaves, setLeaves] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [mounted, setMounted] = useState(false);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalConfig, setModalConfig] = useState({ id: null, status: null, message: '' });

  useEffect(() => {
    setMounted(true);
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [leavesData, doctorsData] = await Promise.all([
        DoctorLeaveService.getAll(),
        DoctorService.getAll(0, 100)
      ]);
      setLeaves(leavesData);
      setDoctors(doctorsData.content || []);
    } catch (error) {
      console.error("Error fetching data:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateStatusClick = (id, status) => {
    const confirmMsg = status === 'APPROVED' ? t('confirm_approve_leave') : t('confirm_reject_leave');
    setModalConfig({ id, status, message: confirmMsg });
    setIsModalOpen(true);
  };

  const handleConfirmUpdate = async () => {
    const { id, status } = modalConfig;
    try {
      await DoctorLeaveService.updateStatus(id, status);
      toast.success(status === 'APPROVED' ? t('leave_approved') : t('leave_rejected'));
      fetchData();
    } catch (error) {
      toast.error(t('operation_error'));
    } finally {
      setIsModalOpen(false);
    }
  };

  const getStatusLabel = (status) => {
    if (status === 'APPROVED') return t('status_approved');
    if (status === 'REJECTED') return t('status_rejected');
    return t('status_pending');
  };

  const getStatusColors = (status) => {
    if (status === 'APPROVED') return { bg: '#dcfce7', color: '#166534' };
    if (status === 'REJECTED') return { bg: '#fee2e2', color: '#991b1b' };
    return { bg: '#fef3c7', color: '#92400e' };
  };

  if (!mounted) return null;

  return (
    <div style={{ padding: '2rem', maxWidth: '1200px', margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '2rem', color: 'var(--primary)', marginBottom: '0.5rem' }}>
            {t('leave_requests')}
          </h1>
          <p style={{ color: 'var(--text-muted)' }}>
            {t('leave_requests_desc')}
          </p>
        </div>
      </div>

      <div className={styles.card}>
        <div style={{ padding: '1.5rem', borderBottom: '1px solid var(--border)' }}>
          <h2 style={{ fontSize: '1.25rem', fontWeight: '600', color: 'var(--text-main)' }}>{t('leave_requests')}</h2>
        </div>
          <div style={{ overflowX: 'auto', padding: '1rem' }}>
            <table style={{ width: '100%', borderCollapse: 'separate', borderSpacing: '0 0.5rem', textAlign: 'left' }}>
              <thead>
                <tr>
                  <th style={{ padding: '1rem 1.5rem', color: 'var(--text-muted)', fontWeight: '600', fontSize: '0.85rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>{t('doctor')}</th>
                  <th style={{ padding: '1rem 1.5rem', color: 'var(--text-muted)', fontWeight: '600', fontSize: '0.85rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>{t('start_date')}</th>
                  <th style={{ padding: '1rem 1.5rem', color: 'var(--text-muted)', fontWeight: '600', fontSize: '0.85rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>{t('end_date')}</th>
                  <th style={{ padding: '1rem 1.5rem', color: 'var(--text-muted)', fontWeight: '600', fontSize: '0.85rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>{t('reason')}</th>
                  <th style={{ padding: '1rem 1.5rem', color: 'var(--text-muted)', fontWeight: '600', fontSize: '0.85rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>{t('status')}</th>
                  <th style={{ padding: '1rem 1.5rem', textAlign: 'right', color: 'var(--text-muted)', fontWeight: '600', fontSize: '0.85rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>{t('actions')}</th>
                </tr>
              </thead>
              <tbody>
                {leaves.map(leave => {
                  const doc = doctors.find(d => d.id === leave.doctorId);
                  const statusColors = getStatusColors(leave.status);
                  return (
                    <tr key={leave.id} style={{ backgroundColor: 'var(--surface)', boxShadow: '0 2px 4px rgba(0,0,0,0.02)', transition: 'transform 0.2s, box-shadow 0.2s', borderRadius: '12px' }} onMouseEnter={(e) => { e.currentTarget.style.transform = 'translateY(-2px)'; e.currentTarget.style.boxShadow = '0 8px 12px rgba(0,0,0,0.05)'; }} onMouseLeave={(e) => { e.currentTarget.style.transform = 'none'; e.currentTarget.style.boxShadow = '0 2px 4px rgba(0,0,0,0.02)'; }}>
                      <td style={{ padding: '1.25rem 1.5rem', borderTopLeftRadius: '12px', borderBottomLeftRadius: '12px' }}>
                        <div style={{ fontWeight: '600', color: 'var(--text-main)' }}>
                          {doc ? `Dr. ${doc.firstName} ${doc.lastName}` : t('unknown_doctor')}
                        </div>
                      </td>
                      <td style={{ padding: '1.25rem 1.5rem', color: 'var(--text-main)', fontWeight: '500' }}>{new Date(leave.startDate).toLocaleDateString('tr-TR')}</td>
                      <td style={{ padding: '1.25rem 1.5rem', color: 'var(--text-main)', fontWeight: '500' }}>{new Date(leave.endDate).toLocaleDateString('tr-TR')}</td>
                      <td style={{ padding: '1.25rem 1.5rem', color: 'var(--text-muted)' }}>{leave.reason}</td>
                      <td style={{ padding: '1.25rem 1.5rem' }}>
                        <span style={{
                          display: 'inline-flex',
                          alignItems: 'center',
                          padding: '0.4rem 1rem',
                          borderRadius: '20px',
                          fontSize: '0.75rem',
                          fontWeight: '700',
                          backgroundColor: statusColors.bg,
                          color: statusColors.color,
                          border: `1px solid ${statusColors.color}40`,
                          letterSpacing: '0.03em'
                        }}>
                          {getStatusLabel(leave.status)}
                        </span>
                      </td>
                      <td style={{ padding: '1.25rem 1.5rem', textAlign: 'right', borderTopRightRadius: '12px', borderBottomRightRadius: '12px' }}>
                        {leave.status === 'PENDING' && (
                          <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end' }}>
                            <button
                              onClick={() => handleUpdateStatusClick(leave.id, 'APPROVED')}
                              style={{
                                padding: '0.5rem 1rem',
                                backgroundColor: 'rgba(16, 185, 129, 0.1)',
                                color: '#10b981',
                                border: '1px solid rgba(16, 185, 129, 0.3)',
                                borderRadius: '8px',
                                cursor: 'pointer',
                                fontSize: '0.85rem',
                                fontWeight: '600',
                                transition: 'all 0.2s',
                              }}
                              onMouseEnter={(e) => { e.currentTarget.style.backgroundColor = '#10b981'; e.currentTarget.style.color = '#fff'; }}
                              onMouseLeave={(e) => { e.currentTarget.style.backgroundColor = 'rgba(16, 185, 129, 0.1)'; e.currentTarget.style.color = '#10b981'; }}
                            >
                              {t('approve')}
                            </button>
                            <button
                              onClick={() => handleUpdateStatus(leave.id, 'REJECTED')}
                              style={{
                                padding: '0.5rem 1rem',
                                backgroundColor: 'rgba(239, 68, 68, 0.1)',
                                color: '#ef4444',
                                border: '1px solid rgba(239, 68, 68, 0.3)',
                                borderRadius: '8px',
                                cursor: 'pointer',
                                fontSize: '0.85rem',
                                fontWeight: '600',
                                transition: 'all 0.2s',
                              }}
                              onMouseEnter={(e) => { e.currentTarget.style.backgroundColor = '#ef4444'; e.currentTarget.style.color = '#fff'; }}
                              onMouseLeave={(e) => { e.currentTarget.style.backgroundColor = 'rgba(239, 68, 68, 0.1)'; e.currentTarget.style.color = '#ef4444'; }}
                            >
                              {t('reject')}
                            </button>
                          </div>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
      </div>
      
      <ConfirmModal 
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onConfirm={handleConfirmUpdate}
        title={modalConfig.status === 'APPROVED' ? t('approve_leave') : t('reject_leave')}
        message={modalConfig.message}
        type={modalConfig.status === 'APPROVED' ? 'success' : 'danger'}
        confirmText={t('confirm') || 'Onayla'}
        cancelText={t('cancel') || 'İptal'}
      />
    </div>
  );
}
