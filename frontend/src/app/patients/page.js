'use client';
import { useState, useEffect, Suspense } from 'react';
import { useSearchParams } from 'next/navigation';
import { PatientService } from '../../services/api';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import ConfirmModal from '../../components/ConfirmModal';
import Scanner from '../../components/Scanner';
import { toast } from '../../components/Toast';
import { useSettings } from '../../context/SettingsContext';
import styles from '../shared.module.css';

export default function PatientsPage() {
  const { t } = useSettings();
  return (
    <Suspense fallback={<div>{t('loading')}</div>}>
      <PatientsContent />
    </Suspense>
  );
}

function PatientsContent() {
  const { t } = useSettings();
  const searchParams = useSearchParams();
  const [patients, setPatients] = useState([]);
  const [allPatients, setAllPatients] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [confirmModal, setConfirmModal] = useState({ isOpen: false, id: null });
  const [formData, setFormData] = useState({ 
    firstName: '', lastName: '', tcIdentityNumber: '', phoneNumber: '', email: '' 
  });
  const [editingId, setEditingId] = useState(null);

  const initialSearch = searchParams.get('search');
  const [searchTerm, setSearchTerm] = useState(initialSearch);

  useEffect(() => {
    const currentSearch = searchParams.get('search');
    if (currentSearch !== searchTerm) {
      setSearchTerm(currentSearch);
    }
  }, [searchParams]);

  useEffect(() => {
    setPage(0);
  }, [searchTerm]);

  const fetchPatients = async () => {
    try {
      const data = await PatientService.getAll(0, 1000);
      setAllPatients(data.content || data);
    } catch (error) {
      toast.error(t('error_loading_patients'));
    }
  };

  useEffect(() => {
    fetchPatients();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingId) {
        await PatientService.update(editingId, formData);
        toast.success(t('patient_updated'));
      } else {
        await PatientService.create(formData);
        toast.success(t('patient_added'));
      }
      setIsModalOpen(false);
      setEditingId(null);
      fetchPatients();
    } catch (error) {
      toast.error(`${t('operation_failed')}:\n${error.message}`);
    }
  };

  const handleEdit = (patient) => {
    setFormData({ 
      firstName: patient.firstName, 
      lastName: patient.lastName,
      tcIdentityNumber: patient.tcIdentityNumber,
      phoneNumber: patient.phoneNumber || '',
      email: patient.email || ''
    });
    setEditingId(patient.id);
    setIsModalOpen(true);
  };

  const handleDelete = (id) => {
    setConfirmModal({ isOpen: true, id });
  };

  const executeDelete = async () => {
    try {
      await PatientService.delete(confirmModal.id);
      fetchPatients();
      toast.success(t('patient_deleted'));
    } catch (error) {
      toast.error(t('delete_failed_has_appointments'));
    } finally {
      setConfirmModal({ isOpen: false, id: null });
    }
  };

  const columns = [
    { header: t('name'), render: (row) => `${row.firstName} ${row.lastName}` },
    { header: t('tc_id'), accessor: 'tcIdentityNumber' },
    { header: t('phone'), accessor: 'phoneNumber' },
    { header: t('email'), accessor: 'email' }
  ];

  const filteredPatients = allPatients.filter(p => {
    if (!searchTerm) return true;
    const term = searchTerm.toLowerCase();
    const fullName = `${p.firstName || ''} ${p.lastName || ''}`.toLowerCase();
    const tc = p.tcIdentityNumber || '';
    return fullName.includes(term) || tc.includes(term);
  });

  const PAGE_SIZE = 8;
  const calculatedTotalPages = Math.ceil(filteredPatients.length / PAGE_SIZE);
  const displayedPatients = filteredPatients.slice(page * PAGE_SIZE, (page + 1) * PAGE_SIZE);

  return (
    <div>
      <div className={styles.pageHeader}>
        <h1 className={styles.pageTitle}>{t('patients')}</h1>
        <button 
          className={styles.primaryBtn} 
          onClick={() => {
            setFormData({ tcIdentityNumber: '', firstName: '', lastName: '', phoneNumber: '', email: '' });
            setEditingId(null);
            setIsModalOpen(true);
          }}
        >
          + {t('add_patient')}
        </button>
      </div>

      <Scanner onScan={(tc) => {
        setSearchTerm(tc);
        const exists = allPatients.some(p => p.tcIdentityNumber === tc);
        if (!exists) {
          toast.info(t('patient_not_found_opening_register'));
          setFormData({ tcIdentityNumber: tc, firstName: '', lastName: '', phoneNumber: '', email: '' });
          setEditingId(null);
          setIsModalOpen(true);
        }
      }} />

      <div style={{ marginBottom: '1rem', marginTop: '1rem' }}>
        <input 
          type="text" 
          placeholder={t('search_patient_placeholder')}
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          style={{ width: '100%', maxWidth: '400px', padding: '0.6rem 1rem', borderRadius: '8px', border: '1px solid var(--border)', backgroundColor: 'var(--background)', color: 'var(--text-main)', fontSize: '0.9rem' }}
        />
      </div>

      <DataTable 
        columns={columns} 
        data={displayedPatients} 
        onEdit={handleEdit} 
        onDelete={handleDelete} 
        page={page}
        totalPages={calculatedTotalPages}
        onPageChange={setPage}
      />

      <Modal 
        isOpen={isModalOpen} 
        onClose={() => setIsModalOpen(false)} 
        title={editingId ? (t('edit_patient')) : (t('add_patient'))}
      >
        <form onSubmit={handleSubmit}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div className={styles.formGroup}>
              <label>{t('name')}</label>
              <input required value={formData.firstName} onChange={(e) => setFormData({...formData, firstName: e.target.value})} />
            </div>
            <div className={styles.formGroup}>
              <label>{t('surname')}</label>
              <input required value={formData.lastName} onChange={(e) => setFormData({...formData, lastName: e.target.value})} />
            </div>
          </div>
          <div className={styles.formGroup}>
            <label>{t('tc_id')}</label>
            <input required minLength="11" maxLength="11" value={formData.tcIdentityNumber} onChange={(e) => setFormData({...formData, tcIdentityNumber: e.target.value})} />
          </div>
          <div className={styles.formGroup}>
            <label>{t('phone')}</label>
            <input maxLength="15" value={formData.phoneNumber} onChange={(e) => setFormData({...formData, phoneNumber: e.target.value})} />
          </div>
          <div className={styles.formGroup}>
            <label>{t('email')}</label>
            <input type="email" value={formData.email} onChange={(e) => setFormData({...formData, email: e.target.value})} />
          </div>
          <div className={styles.formActions}>
            <button type="button" className={styles.cancelBtn} onClick={() => setIsModalOpen(false)}>{t('cancel')}</button>
            <button type="submit" className={styles.primaryBtn}>{t('save')}</button>
          </div>
        </form>
      </Modal>

      <ConfirmModal
        isOpen={confirmModal.isOpen}
        title={t('confirm_deletion_title')}
        message={t('confirm_delete_patient')}
        onConfirm={executeDelete}
        onCancel={() => setConfirmModal({ isOpen: false, id: null })}
        confirmText={t('yes_delete')}
        type="danger"
      />
    </div>
  );
}
