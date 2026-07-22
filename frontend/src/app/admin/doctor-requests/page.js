'use client';
import { useState, useEffect } from 'react';
import { useSettings } from '../../../context/SettingsContext';
import { fetchAPI } from '../../../services/api';
import { toast } from '../../../components/Toast';
import ConfirmModal from '../../../components/ConfirmModal';
import styles from './page.module.css';

export default function DoctorRequestsPage() {
  const { t, tErr } = useSettings();
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [confirmModal, setConfirmModal] = useState({ show: false, type: '', reqId: null, message: '' });

  const fetchRequests = async () => {
    try {
      const data = await fetchAPI('/admin/doctor-requests');
      setRequests(data);
    } catch (err) {
      setError(tErr(err.message));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRequests();
  }, []);

  const handleAction = async (id, action) => {
    try {
      let successMsg = action === 'approve' ? t('approve_success') : t('reject_success');
      
      // We need to use raw fetch here ONLY if fetchAPI doesn't return the raw response object.
      // fetchAPI parses JSON automatically and returns the payload OR throws an Error.
      // But we need the message from the 200 OK response!
      // Let's check how fetchAPI handles 200 OK:
      // If it's a JSON response, fetchAPI returns the parsed JSON object!
      // The backend returns: { message: "Doktor başarıyla onaylandı..." }
      const data = await fetchAPI(`/admin/doctor-requests/${id}/${action}`, {
        method: 'POST'
      });
      
      if (data && data.message) {
        successMsg = data.message;
      }
      
      setConfirmModal({ show: false, type: '', reqId: null, message: '' });
      toast.success(successMsg, { autoClose: action === 'approve' ? 10000 : 3000 });
      fetchRequests(); // Refresh list
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
    </div>
  );
}
