'use client';
import { useState, useEffect } from 'react';
import { useSearchParams } from 'next/navigation';
import { DoctorService, DepartmentService, PolyclinicService } from '../../services/api';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import ConfirmModal from '../../components/ConfirmModal';
import { toast } from '../../components/Toast';
import { useSettings } from '../../context/SettingsContext';
import styles from '../shared.module.css';

export default function DoctorsPage() {
  const { t } = useSettings();
  const searchParams = useSearchParams();
  const initialSearch = searchParams.get('search') || '';
  const [searchTerm, setSearchTerm] = useState(initialSearch);
  const [allDoctors, setAllDoctors] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [polyclinics, setPolyclinics] = useState([]);
  const [page, setPage] = useState(0);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [confirmModal, setConfirmModal] = useState({ isOpen: false, id: null });
  const [formData, setFormData] = useState({ 
    firstName: '', lastName: '', specialization: '', phoneNumber: '', email: '', departmentId: '', polyclinicId: '' 
  });
  const [editingId, setEditingId] = useState(null);

  const fetchData = async () => {
    try {
      const [docs, depts, polys] = await Promise.all([
        DoctorService.getAll(0, 1000), // Fetch all doctors for local search/pagination
        DepartmentService.getAll(0, 1000),
        PolyclinicService.getAll()
      ]);
      setAllDoctors(docs.content || []);
      setDepartments(depts.content || []);
      setPolyclinics(polys || []);
    } catch (error) {
      toast.error('Veriler yüklenemedi.');
    }
  };

  useEffect(() => {
    fetchData();
  }, []); // Run only once, pagination is local now

  useEffect(() => {
    const currentSearch = searchParams.get('search') || '';
    if (currentSearch !== searchTerm) {
      setSearchTerm(currentSearch);
    }
  }, [searchParams]);

  useEffect(() => {
    setPage(0); // Reset page when search term changes
  }, [searchTerm]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.departmentId) {
      toast.error('Lütfen bir bölüm seçin.');
      return;
    }
    
    try {
      if (editingId) {
        await DoctorService.update(editingId, formData);
      } else {
        await DoctorService.create(formData);
      }
      setIsModalOpen(false);
      setEditingId(null);
      fetchData();
      toast.success(editingId ? 'Doktor başarıyla güncellendi.' : 'Doktor başarıyla eklendi.');
    } catch (error) {
      toast.error(`İşlem başarısız oldu:\n${error.message}`);
    }
  };

  const handleEdit = (doctor) => {
    setFormData({ 
      firstName: doctor.firstName, 
      lastName: doctor.lastName,
      specialization: doctor.specialization,
      phoneNumber: doctor.phoneNumber || '',
      email: doctor.email || '',
      departmentId: doctor.departmentId,
      polyclinicId: doctor.polyclinicId || ''
    });
    setEditingId(doctor.id);
    setIsModalOpen(true);
  };

  const handleDelete = (id) => {
    setConfirmModal({ isOpen: true, id });
  };

  const executeDelete = async () => {
    try {
      await DoctorService.delete(confirmModal.id);
      fetchData();
      toast.success('Doktor başarıyla silindi.');
    } catch (error) {
      toast.error('Silme işlemi başarısız. Doktorun randevuları olabilir.');
    } finally {
      setConfirmModal({ isOpen: false, id: null });
    }
  };

  const columns = [
    { header: t('title_desc') || 'Ünvan/Ad Soyad', render: (row) => `${row.specialization} ${row.firstName} ${row.lastName}` },
    { header: t('department'), render: (row) => t(row.departmentName) },
    { header: t('polyclinics') || 'Poliklinik', render: (row) => row.polyclinicName || '-' },
    { header: t('phone'), accessor: 'phoneNumber' }
  ];

  const filteredDoctors = allDoctors.filter(doc => {
    if (!searchTerm) return true;
    const term = searchTerm.toLowerCase();
    const fullName = `${doc.firstName || ''} ${doc.lastName || ''}`.toLowerCase();
    const deptName = doc.departmentName?.toLowerCase() || '';
    const polyName = doc.polyclinicName?.toLowerCase() || '';
    return fullName.includes(term) || deptName.includes(term) || polyName.includes(term);
  });

  const PAGE_SIZE = 8;
  const calculatedTotalPages = Math.ceil(filteredDoctors.length / PAGE_SIZE);
  const displayedDoctors = filteredDoctors.slice(page * PAGE_SIZE, (page + 1) * PAGE_SIZE);

  return (
    <div className={styles.pageContainer}>
      <div className={styles.pageHeader}>
        <div>
          <h1 className={styles.pageTitle}>{t('doctors')}</h1>
          <p className={styles.pageDesc}>{t('Hastanede görev yapan tüm doktorların listesi ve yönetim paneli.')}</p>
        </div>
        <button 
          className={styles.primaryBtn} 
          onClick={() => {
            setFormData({ firstName: '', lastName: '', specialization: '', phoneNumber: '', email: '', departmentId: '', polyclinicId: '' });
            setEditingId(null);
            setIsModalOpen(true);
          }}
        >
          + {t('add_doctor')}
        </button>
      </div>

      <DataTable 
        columns={columns}
        data={displayedDoctors}
        onEdit={handleEdit}
        onDelete={handleDelete}
        page={page}
        totalPages={calculatedTotalPages}
        onPageChange={setPage}
      />

      <Modal 
        isOpen={isModalOpen} 
        onClose={() => setIsModalOpen(false)} 
        title={editingId ? t('edit_doctor') || 'Doktor Düzenle' : t('add_doctor')}
      >
        <form onSubmit={handleSubmit}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <div className={styles.formGroup}>
              <label>{t('name') || 'Ad'}</label>
              <input required value={formData.firstName} onChange={(e) => setFormData({...formData, firstName: e.target.value})} />
            </div>
            <div className={styles.formGroup}>
              <label>{t('surname') || 'Soyad'}</label>
              <input required value={formData.lastName} onChange={(e) => setFormData({...formData, lastName: e.target.value})} />
            </div>
          </div>
          
          <div className={styles.formGroup}>
            <label>{t('department') || 'Bölüm'}</label>
            <select 
              required 
              value={formData.departmentId} 
              onChange={(e) => setFormData({...formData, departmentId: e.target.value, polyclinicId: ''})}
            >
              <option value="">-- {t('select_department') || 'Bölüm Seçin'} --</option>
              {departments.map(dept => (
                <option key={dept.id} value={dept.id}>{dept.name}</option>
              ))}
            </select>
          </div>

          <div className={styles.formGroup}>
            <label>{t('polyclinics') || 'Poliklinik'}</label>
            <select 
              value={formData.polyclinicId} 
              onChange={(e) => setFormData({...formData, polyclinicId: e.target.value})}
              disabled={!formData.departmentId}
              title={!formData.departmentId ? "Önce bir bölüm seçmelisiniz" : ""}
            >
              <option value="">-- {t('select_polyclinic') || 'Poliklinik Seçin'} --</option>
              {formData.departmentId && polyclinics.filter(p => p.departmentId === parseInt(formData.departmentId)).map(poly => (
                <option key={poly.id} value={poly.id}>{poly.name} ({poly.roomNumber})</option>
              ))}
            </select>
          </div>
          
          <div className={styles.formGroup}>
            <label>{t('specialization') || 'Uzmanlık'}</label>
            <input required value={formData.specialization} onChange={(e) => setFormData({...formData, specialization: e.target.value})} />
          </div>
          
          <div className={styles.formGroup}>
            <label>{t('phone') || 'Telefon Numarası'}</label>
            <input maxLength="15" value={formData.phoneNumber} onChange={(e) => setFormData({...formData, phoneNumber: e.target.value})} />
          </div>
          <div className={styles.formGroup}>
            <label>{t('email') || 'E-Posta'}</label>
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
        title={t('delete_confirm_title') || 'Silme İşlemi Onayı'}
        message={t('delete_confirm_message') || 'Bu doktoru silmek istediğinize emin misiniz?'}
        onConfirm={executeDelete}
        onCancel={() => setConfirmModal({ isOpen: false, id: null })}
        confirmText={t('yes_delete') || 'Evet, Sil'}
        cancelText={t('cancel') || 'İptal'}
      />
    </div>
  );
}
