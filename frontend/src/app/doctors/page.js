'use client';
import { useState, useEffect, useCallback } from 'react';
import { useSearchParams } from 'next/navigation';
import { DoctorService, DepartmentService, PolyclinicService } from '../../services/api';
import { useApi } from '../../hooks/useApi';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import ConfirmModal from '../../components/ConfirmModal';
import { toast } from '../../components/Toast';
import { useSettings } from '../../context/SettingsContext';
import styles from '../shared.module.css';

export default function DoctorsPage() {
  const { t, tErr } = useSettings();
  const searchParams = useSearchParams();
  const searchTerm = searchParams.get('search') || '';
  const [prevSearchTerm, setPrevSearchTerm] = useState(searchTerm);
  const [page, setPage] = useState(0);

  if (searchTerm !== prevSearchTerm) {
    setPrevSearchTerm(searchTerm);
    setPage(0);
  }

  const [pageSize, setPageSize] = useState(5);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [confirmModal, setConfirmModal] = useState({ isOpen: false, id: null });
  const [formData, setFormData] = useState({ 
    firstName: '', lastName: '', specialization: '', phoneNumber: '', email: '', departmentId: '', polyclinicId: '' 
  });
  const [editingId, setEditingId] = useState(null);
  const fetchDoctorsData = useCallback(async (signal) => {
    const [depts, polys, docs] = await Promise.all([
      DepartmentService.getAll(0, 100, { signal }),
      PolyclinicService.getAll({ signal }),
      DoctorService.getAll(0, 100, { signal })
    ]);
    return {
      departments: depts.content || depts || [],
      polyclinics: polys || [],
      doctors: docs.content || docs || []
    };
  }, []);

  const { data, loading, execute: rawFetchData } = useApi(fetchDoctorsData, {
    departments: [],
    polyclinics: [],
    doctors: []
  });

  const { departments, polyclinics, doctors: allDoctors } = data;

  const fetchData = useCallback(async () => {
    try {
      await rawFetchData();
    } catch (err) {
      console.error('Veriler yüklenirken hata:', err);
      toast.error('Veriler yüklenemedi.');
    }
  }, [rawFetchData]);

  useEffect(() => {
    fetchData();
  }, [fetchData]); // Run safely with useCallback dependencies


  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.departmentId) {
      toast.error(t('select_dept_required'));
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
      toast.success(editingId ? t('doctor_updated') : t('doctor_added'));
    } catch (error) {
      toast.error(`${t('operation_failed')}:\n${error.message}`);
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
      toast.success(t('doctor_deleted'));
    } catch (error) {
      toast.error(t('doctor_delete_failed'));
    } finally {
      setConfirmModal({ isOpen: false, id: null });
    }
  };

  const columns = [
    { header: t('title_desc'), render: (row) => `${row.specialization} ${row.firstName} ${row.lastName}` },
    { header: t('department'), render: (row) => t(row.departmentName) },
    { header: t('polyclinics'), render: (row) => row.polyclinicName || '-' },
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

  const calculatedTotalPages = Math.ceil(filteredDoctors.length / pageSize);
  const displayedDoctors = filteredDoctors.slice(page * pageSize, (page + 1) * pageSize);

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

      <div style={{ marginBottom: '1rem', marginTop: '1rem', display: 'flex', gap: '1rem', alignItems: 'center', justifyContent: 'flex-end', flexWrap: 'wrap' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', backgroundColor: 'var(--surface)', padding: '0.2rem 0.5rem 0.2rem 1rem', borderRadius: '8px', border: '1px solid var(--border)', boxShadow: 'var(--shadow-sm)' }}>
          <span style={{ fontSize: '0.85rem', fontWeight: '500', color: 'var(--text-muted)', whiteSpace: 'nowrap' }}>Kayıt Sayısı:</span>
          <select 
            value={pageSize} 
            onChange={(e) => { setPageSize(Number(e.target.value)); setPage(0); }}
            style={{ width: 'auto', padding: '0.4rem 2rem 0.4rem 0.8rem', border: 'none', backgroundColor: 'transparent', boxShadow: 'none', fontWeight: '600', color: 'var(--primary)' }}
          >
            <option value={3}>3</option>
            <option value={5}>5</option>
            <option value={10}>10</option>
            <option value={20}>20</option>
            <option value={50}>50</option>
          </select>
        </div>
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
        title={editingId ? t('edit_doctor') : t('add_doctor')}
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
            <label>{t('department')}</label>
            <select 
              required 
              value={formData.departmentId} 
              onChange={(e) => setFormData({...formData, departmentId: e.target.value, polyclinicId: ''})}
            >
              <option value="">-- {t('select_department')} --</option>
              {departments.map(dept => (
                <option key={dept.id} value={dept.id}>{dept.name}</option>
              ))}
            </select>
          </div>

          <div className={styles.formGroup}>
            <label>{t('polyclinics')}</label>
            <select 
              value={formData.polyclinicId} 
              onChange={(e) => setFormData({...formData, polyclinicId: e.target.value})}
              disabled={!formData.departmentId}
              title={!formData.departmentId ? t('select_dept_first') : ""}
            >
              <option value="">-- {t('select_polyclinic')} --</option>
              {formData.departmentId && polyclinics.filter(p => p.departmentId === parseInt(formData.departmentId)).map(poly => (
                <option key={poly.id} value={poly.id}>{poly.name} ({poly.roomNumber})</option>
              ))}
            </select>
          </div>
          
          <div className={styles.formGroup}>
            <label>{t('specialization')}</label>
            <input required value={formData.specialization} onChange={(e) => setFormData({...formData, specialization: e.target.value})} />
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
        title={t('delete_confirm_title')}
        message={t('delete_confirm_message')}
        onConfirm={executeDelete}
        onCancel={() => setConfirmModal({ isOpen: false, id: null })}
        confirmText={t('yes_delete')}
        cancelText={t('cancel')}
      />
    </div>
  );
}
