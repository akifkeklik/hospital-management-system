'use client';
import { useEffect, useState } from 'react';
import { DoctorLeaveService, DoctorService } from '../../../services/api';
import { toast } from '../../../components/Toast';
import { useSettings } from '../../../context/SettingsContext';
import styles from '../../shared.module.css';

export default function DoctorLeavesPage() {
  const { t } = useSettings();
  const [leaves, setLeaves] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [mounted, setMounted] = useState(false);
  const [loading, setLoading] = useState(true);

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

  const handleUpdateStatus = async (id, status) => {
    const confirmMsg = status === 'APPROVED' ? t('confirm_approve_leave') : t('confirm_reject_leave');
    if (confirm(confirmMsg)) {
      try {
        await DoctorLeaveService.updateStatus(id, status);
        toast.success(status === 'APPROVED' ? t('leave_approved') : t('leave_rejected'));
        fetchData();
      } catch (error) {
        toast.error(t('operation_error'));
      }
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
        <div style={{ overflowX: 'auto' }}>
          {loading ? (
            <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>{t('loading')}</div>
          ) : leaves.length === 0 ? (
            <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>{t('no_leave_requests')}</div>
          ) : (
            <table className={styles.table}>
              <thead>
                <tr>
                  <th>{t('doctor')}</th>
                  <th>{t('start_date')}</th>
                  <th>{t('end_date')}</th>
                  <th>{t('reason')}</th>
                  <th>{t('status')}</th>
                  <th style={{ textAlign: 'right' }}>{t('actions')}</th>
                </tr>
              </thead>
              <tbody>
                {leaves.map(leave => {
                  const doc = doctors.find(d => d.id === leave.doctorId);
                  const statusColors = getStatusColors(leave.status);
                  return (
                    <tr key={leave.id}>
                      <td>
                        <div style={{ fontWeight: '500', color: 'var(--text-main)' }}>
                          {doc ? `Dr. ${doc.firstName} ${doc.lastName}` : t('unknown_doctor')}
                        </div>
                      </td>
                      <td style={{ color: 'var(--text-main)' }}>{leave.startDate}</td>
                      <td style={{ color: 'var(--text-main)' }}>{leave.endDate}</td>
                      <td style={{ color: 'var(--text-muted)' }}>{leave.reason}</td>
                      <td>
                        <span style={{
                          display: 'inline-flex',
                          alignItems: 'center',
                          padding: '0.25rem 0.75rem',
                          borderRadius: '9999px',
                          fontSize: '0.875rem',
                          fontWeight: '500',
                          backgroundColor: statusColors.bg,
                          color: statusColors.color
                        }}>
                          {getStatusLabel(leave.status)}
                        </span>
                      </td>
                      <td style={{ textAlign: 'right' }}>
                        {leave.status === 'PENDING' && (
                          <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end' }}>
                            <button
                              onClick={() => handleUpdateStatus(leave.id, 'APPROVED')}
                              style={{
                                padding: '0.375rem 0.75rem',
                                backgroundColor: '#10b981',
                                color: 'white',
                                border: 'none',
                                borderRadius: '0.375rem',
                                cursor: 'pointer',
                                fontSize: '0.875rem',
                                fontWeight: '500'
                              }}
                            >
                              {t('approve')}
                            </button>
                            <button
                              onClick={() => handleUpdateStatus(leave.id, 'REJECTED')}
                              style={{
                                padding: '0.375rem 0.75rem',
                                backgroundColor: '#ef4444',
                                color: 'white',
                                border: 'none',
                                borderRadius: '0.375rem',
                                cursor: 'pointer',
                                fontSize: '0.875rem',
                                fontWeight: '500'
                              }}
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
          )}
        </div>
      </div>
    </div>
  );
}
