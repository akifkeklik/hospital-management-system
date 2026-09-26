'use client';
import { useState, useEffect, useCallback } from 'react';
import { useSettings } from '../../../context/SettingsContext';
import { fetchAPI } from '../../../services/api';
import { toast } from '../../../components/Toast';
import ConfirmModal from '../../../components/ConfirmModal';
import styles from './page.module.css';

export default function DoctorRequestsPage() {
  const { t, tErr } = useSettings();
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [confirmModal, setConfirmModal] = useState({ show: false, type: '', reqId: null, message: '' });
  const [credentialsModal, setCredentialsModal] = useState({ show: false, message: '' });

  useEffect(() => {
    let ignore = false;
    async function fetchRequests() {
      try {
        const data = await fetchAPI('/admin/doctor-requests');
        if (!ignore) {
          setRequests(data);
          setLoading(false);
        }
      } catch (err) {
        if (!ignore) {
          setError(tErr(err.message));
          setLoading(false);
        }
      }
    }
    fetchRequests();
    return () => { ignore = true; };
  }, [tErr]);

  const handleAction = async (id, action) => {
    try {
      let successMsg = action === 'approve' ? t('approve_success') : t('reject_success');
      
      const data = await fetchAPI(`/admin/doctor-requests/${id}/${action}`, {
        method: 'POST'
      });
      
      if (data && data.message) {
        successMsg = data.message;
      }
      
      setConfirmModal({ show: false, type: '', reqId: null, message: '' });
      
      if (action === 'approve') {
        setCredentialsModal({ show: true, message: successMsg });
      } else {
        toast.success(successMsg);
      }
      
      // Refresh list
      const newData = await fetchAPI('/admin/doctor-requests');
      setRequests(newData);
    } catch (err) {
      setConfirmModal({ show: false, type: '', reqId: null, message: '' });
      toast.error(tErr(err.message));
    }
  };

  const executeConfirm = () => {
    if (confirmModal.reqId && confirmModal.type) {
      handleAction(confirmModal.reqId, confirmModal.type);
    }
  };

  if (loading) return <div>{t('loading')}</div>;
  if (error) return <div style={{ color: 'red' }}>{error}</div>;

  return (
    <div className={styles.container}>
      <h1 className={styles.title}>{t('doctor_requests_title')}</h1>
      
      {requests.length === 0 ? (
        <div className={styles.emptyState}>
          <div className={styles.emptyStateIconWrapper}>
            <i className="fi fi-rr-inbox"></i>
          </div>
          <h2 className={styles.emptyStateTitle}>{t('no_pending_requests')}</h2>
          <p className={styles.emptyStateDesc}>{t('no_pending_requests_desc')}</p>
        </div>
      ) : (
        <div className={styles.tableContainer}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>{t('date')}</th>
                <th>{t('title_desc')}</th>
                <th>{t('tc_no')}</th>
                <th>{t('specialization')}</th>
                <th>{t('email')}</th>
                <th>{t('actions')}</th>
              </tr>
            </thead>
            <tbody>
              {requests.map(req => (
                <tr key={req.id}>
                  <td>{new Date(req.requestDate).toLocaleDateString()}</td>
                  <td>{req.firstName} {req.lastName}</td>
                  <td>{req.tcIdentityNumber}</td>
                  <td>{req.specialization || '-'}</td>
                  <td>{req.email}</td>
                  <td>
                    <button 
                      onClick={() => setConfirmModal({
                        show: true,
                        type: 'approve',
                        reqId: req.id,
                        message: `${req.firstName} ${req.lastName} ${t('approve_confirm')}`
                      })}
                      className={styles.approveBtn}
                    >
                      {t('approve')}
                    </button>
                    <button 
                      onClick={() => setConfirmModal({
                        show: true,
                        type: 'reject',
                        reqId: req.id,
                        message: t('reject_confirm')
                      })}
                      className={styles.rejectBtn}
                    >
                      {t('reject')}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Custom Modern Confirm Modal */}
      <ConfirmModal
        isOpen={confirmModal.show}
        title={t('doctor_requests_title')}
        message={confirmModal.message}
        onConfirm={executeConfirm}
        onCancel={() => setConfirmModal({...confirmModal, show: false})}
        confirmText={confirmModal.type === 'approve' ? t('approve') : t('reject')}
        type={confirmModal.type === 'approve' ? 'approve' : 'danger'}
      />

      {/* Credentials Modal */}
      {credentialsModal.show && (
        <div className={styles.modalOverlay}>
          <div className={styles.modalContent} style={{ maxWidth: '500px', borderTop: '4px solid #10b981' }}>
            <h3 style={{ color: '#10b981', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <i className="fi fi-rr-check-circle"></i> Onay Başarılı
            </h3>
            <p style={{ marginTop: '1rem', fontSize: '1.1rem', lineHeight: '1.5', padding: '1rem', backgroundColor: 'var(--bg-lighter)', borderRadius: '8px', wordBreak: 'break-all' }}>
              {credentialsModal.message}
            </p>
            <div className={styles.modalActions} style={{ marginTop: '1.5rem', justifyContent: 'center' }}>
              <button 
                className={styles.confirmApproveBtn}
                onClick={() => setCredentialsModal({ show: false, message: '' })}
                style={{ width: '100%', padding: '0.75rem' }}
              >
                Anladım, Kapat
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
