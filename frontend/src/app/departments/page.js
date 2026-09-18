'use client';
import { useState, useEffect, useCallback } from 'react';
import { useApi } from '../../hooks/useApi';
import { DepartmentService } from '../../services/api';
import DataTable from '../../components/DataTable';
import Modal from '../../components/Modal';
import ConfirmModal from '../../components/ConfirmModal';
import { toast } from '../../components/Toast';
import { useSettings } from '../../context/SettingsContext';
import styles from '../shared.module.css';

export default function DepartmentsPage() {
  const { t, tErr } = useSettings();
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [formData, setFormData] = useState({ name: '', description: '' });
  const [editingId, setEditingId] = useState(null);
  const [confirmModal, setConfirmModal] = useState({ isOpen: false, id: null });

  const fetchDepartmentsApi = useCallback(async (signal, currentPage) => {
    const data = await DepartmentService.getAll(currentPage, 5, { signal });
    setTotalPages(data.totalPages || 0);
    return data.content || [];
  }, []);

  const { data: departments, loading, execute } = useApi(fetchDepartmentsApi, []);

  const fetchDepartments = useCallback(() => {
    execute(page).catch(error => {
      // Sadece iptal edilmeyen hataları toast ile göster
      toast.error(t('error_loading_departments'));
    });
  }, [execute, page, t]);

  useEffect(() => {
    fetchDepartments();
  }, [fetchDepartments]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingId) {
        await DepartmentService.update(editingId, formData);
      } else {
        await DepartmentService.create(formData);
      }
      setIsModalOpen(false);
      setFormData({ name: '', description: '' });
      setEditingId(null);
      fetchDepartments();
      toast.success(editingId ? t('dept_updated') : t('dept_added'));
    } catch (error) {
      toast.error(`${t('operation_failed')}:\n${error.message}`);
    }
  };

  const handleEdit = (dept) => {
    setFormData({ name: dept.name, description: dept.description });
    setEditingId(dept.id);
    setIsModalOpen(true);
  };

  const handleDelete = (id) => {
    setConfirmModal({ isOpen: true, id });
  };

  const executeDelete = async () => {
    try {
      await DepartmentService.delete(confirmModal.id);
      fetchDepartments();
      toast.success(t('dept_deleted'));
    } catch (error) {
      toast.error(t('dept_delete_failed'));
    } finally {
      setConfirmModal({ isOpen: false, id: null });
    }
  };

  const columns = [
    { header: t('id'), accessor: 'id' },
    { header: t('dept_name'), render: (row) => <span style={{ textTransform: 'uppercase' }}>{t(row.name)}</span> },
    { header: t('description'), render: (row) => t(row.description) }
  ];

  return (
    <div>
      <div className={styles.pageHeader}>
        <h1 className={styles.pageTitle}>{t('departments')}</h1>
        <button
          className={styles.primaryBtn}
          onClick={() => {
            setFormData({ name: '', description: '' });
            setEditingId(null);
            setIsModalOpen(true);
          }}
        >
          + {t('add_dept')}
        </button>
      </div>

      {loading ? (
        <div style={{ textAlign: 'center', padding: '3rem', color: 'var(--text-muted)' }}>{t('loading') || 'Yükleniyor...'}</div>
      ) : (
        <DataTable
          columns={columns}
          data={departments}
          onEdit={handleEdit}
          onDelete={handleDelete}
          page={page}
          totalPages={totalPages}
          onPageChange={setPage}
        />
      )}

      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingId ? t('edit_dept') : t('add_dept')}
      >
        <form onSubmit={handleSubmit}>
          <div className={styles.formGroup}>
            <label>{t('dept_name')}</label>
            <input
              required
              value={formData.name}
              onChange={(e) => setFormData({...formData, name: e.target.value})}
            />
          </div>
          <div className={styles.formGroup}>
            <label>{t('description')}</label>
            <textarea
              rows="3"
              value={formData.description}
              onChange={(e) => setFormData({...formData, description: e.target.value})}
            />
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
        message={t('confirm_delete_dept')}
        onConfirm={executeDelete}
        onCancel={() => setConfirmModal({ isOpen: false, id: null })}
        confirmText={t('yes_delete')}
        type="danger"
      />
    </div>
  );
}
