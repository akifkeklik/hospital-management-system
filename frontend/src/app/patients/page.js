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
  return (
    <Suspense fallback={<div>Yükleniyor...</div>}>
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

  const initialSearch = searchParams.get('search') || '';
  const [searchTerm, setSearchTerm] = useState(initialSearch);

  useEffect(() => {
    const currentSearch = searchParams.get('search') || '';
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
      toast.error('Hastalar yüklenemedi.');
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
        toast.success('Hasta başarıyla güncellendi.');
      } else {
        await PatientService.create(formData);
        toast.success('Hasta başarıyla eklendi.');
      }
      setIsModalOpen(false);
      setEditingId(null);
      fetchPatients();
    } catch (error) {
      toast.error(`İşlem başarısız oldu:\n${error.message}`);
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
      toast.success('Hasta başarıyla silindi.');
    } catch (error) {
      toast.error('Silme işlemi başarısız. Hastanın randevuları olabilir.');
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
          toast.info('Bu kimlik numarasına sahip hasta bulunamadı. Kayıt penceresi açılıyor...');
          setFormData({ tcIdentityNumber: tc, firstName: '', lastName: '', phoneNumber: '', email: '' });
          setEditingId(null);
          setIsModalOpen(true);
        }
      }} />

      <div style={{ marginBottom: '1rem', marginTop: '1rem' }}>
        <input 
          type="text" 
          placeholder="TC Kimlik No veya İsim ile ara..."
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
        title={editingId ? 'Hasta Düzenle' : 'Yeni Hasta Kaydı'}
      >
        <form onSubmit={handleSubmit}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div className={styles.formGroup}>
              <label>Ad</label>
              <input required value={formData.firstName} onChange={(e) => setFormData({...formData, firstName: e.target.value})} />
            </div>
            <div className={styles.formGroup}>
              <label>Soyad</label>
              <input required value={formData.lastName} onChange={(e) => setFormData({...formData, lastName: e.target.value})} />
            </div>
          </div>
          <div className={styles.formGroup}>
            <label>TC Kimlik Numarası (11 Haneli)</label>
            <input required minLength="11" maxLength="11" value={formData.tcIdentityNumber} onChange={(e) => setFormData({...formData, tcIdentityNumber: e.target.value})} />
          </div>
          <div className={styles.formGroup}>
            <label>Telefon Numarası</label>
            <input maxLength="15" value={formData.phoneNumber} onChange={(e) => setFormData({...formData, phoneNumber: e.target.value})} />
          </div>
          <div className={styles.formGroup}>
            <label>E-Posta</label>
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
        title="Silme İşlemi Onayı"
        message="Bu hastayı silmek istediğinize emin misiniz?"
        onConfirm={executeDelete}
        onCancel={() => setConfirmModal({ isOpen: false, id: null })}
        confirmText="Evet, Sil"
        type="danger"
      />
    </div>
  );
}
