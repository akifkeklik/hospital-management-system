'use client';
import { useEffect, useState, Suspense, useCallback } from 'react';
import { useApi } from '../../../hooks/useApi';
import { useSearchParams } from 'next/navigation';
import { PolyclinicService, DepartmentService } from '../../../services/api';
import { toast } from '../../../components/Toast';
import { useSettings } from '../../../context/SettingsContext';
import styles from '../../shared.module.css';

export default function PolyclinicsPage() {
  return (
    <Suspense fallback={<div style={{padding:'3rem', textAlign:'center', color:'var(--text-muted)'}}>Loading...</div>}>
      <PolyclinicsContent />
    </Suspense>
  );
}

function PolyclinicsContent() {
  const { t } = useSettings();
  const searchParams = useSearchParams();
  const filterDeptId = searchParams.get('departmentId');
  const highlightId = searchParams.get('highlight');


  const fetchPolyclinicsApi = useCallback(async (signal) => {
    const [polyData, deptData] = await Promise.all([
      PolyclinicService.getAll({ signal }),
      DepartmentService.getAll(0, 100, { signal })
    ]);
    return {
      polyclinics: polyData || [],
      departments: deptData.content || deptData || []
    };
  }, []);

  const { data: apiData, loading, execute } = useApi(fetchPolyclinicsApi, null);

  const polyclinics = apiData ? apiData.polyclinics : [];
  const departments = apiData ? apiData.departments : [];

  const [name, setName] = useState('');
  const [roomNumber, setRoomNumber] = useState('');
  const [departmentId, setDepartmentId] = useState('');

  const [page, setPage] = useState(1);
  const [itemsPerPage, setItemsPerPage] = useState(4);

  const fetchData = useCallback(() => {
    execute().catch(error => {
      console.error("Error fetching data:", error);
    });
  }, [execute]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  const handleAddPolyclinic = async (e) => {
    e.preventDefault();
    try {
      await PolyclinicService.create({
        name,
        roomNumber,
        departmentId: parseInt(departmentId)
      });
      toast.success(t('polyclinic_added_success') || 'Poliklinik başarıyla eklendi!');
      setName('');
      setRoomNumber('');
      setDepartmentId('');
      fetchData();
    } catch (error) {
      toast.error(t('polyclinic_add_error') || 'Poliklinik eklenirken hata oluştu.');
    }
  };

  const handleDelete = async (id) => {
    if (confirm(t('confirm_delete_polyclinic') || 'Bu polikliniği silmek istediğinize emin misiniz?')) {
      try {
        await PolyclinicService.delete(id);
        fetchData();
      } catch (error) {
        toast.error(t('delete_failed') || 'Silinemedi.');
      }
    }
  };

  const getDeptName = (id) => {
    const dept = departments.find(d => d.id === parseInt(id));
    return dept ? dept.name : (t('unknown_department') || 'Bilinmeyen Bölüm');
  };

  const filteredPolyclinics = filterDeptId
    ? polyclinics.filter(p => p.departmentId === parseInt(filterDeptId))
    : polyclinics;

  const totalPages = Math.max(1, Math.ceil(filteredPolyclinics.length / itemsPerPage));
  const paginatedPolyclinics = filteredPolyclinics.slice((page - 1) * itemsPerPage, page * itemsPerPage);

  return (
    <div className={styles.container} style={{ padding: '1rem 2rem' }}>
      <div className={styles.header} style={{ marginBottom: '1rem' }}>
        <div>
          <h1 className={styles.title} style={{ fontSize: '1.6rem', fontWeight: '700', color: 'var(--text-main)' }}>{t('polyclinic_management')}</h1>
          <p style={{ color: 'var(--text-muted)', marginTop: '0.2rem', fontSize: '0.9rem' }}>{t('polyclinic_management_desc')}</p>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 2.5fr', gap: '1.5rem' }}>

        {/* Ekleme Formu */}
        <div style={{
          backgroundColor: 'var(--surface)',
          padding: '1.2rem',
          borderRadius: '12px',
          boxShadow: '0 4px 20px rgba(0,0,0,0.05)',
          border: '1px solid rgba(var(--primary-rgb), 0.1)',
          height: 'fit-content'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem' }}>
            <div style={{ padding: '6px', backgroundColor: 'rgba(var(--primary-rgb), 0.1)', borderRadius: '8px', color: 'var(--primary)' }}>
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect><line x1="12" y1="8" x2="12" y2="16"></line><line x1="8" y1="12" x2="16" y2="12"></line></svg>
            </div>
            <h2 style={{ fontSize: '1.05rem', fontWeight: '600', color: 'var(--text-main)', margin: 0 }}>{t('add_new_polyclinic')}</h2>
          </div>

          <form onSubmit={handleAddPolyclinic} style={{ display: 'flex', flexDirection: 'column', gap: '1.2rem' }}>
            <div>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.85rem', fontWeight: '500', color: 'var(--text-muted)' }}>{t('linked_department')}</label>
              <select
                required
                value={departmentId}
                onChange={(e) => setDepartmentId(e.target.value)}
                style={{ width: '100%', padding: '0.8rem', borderRadius: '8px', border: '1px solid var(--border)', backgroundColor: 'var(--background)', color: 'var(--text-main)', outline: 'none' }}
              >
                <option value="" disabled>{t('select_department')}</option>
                {departments.map(dept => (
                  <option key={dept.id} value={dept.id}>{dept.name}</option>
                ))}
              </select>
            </div>

            <div>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.85rem', fontWeight: '500', color: 'var(--text-muted)' }}>{t('polyclinic_name')}</label>
              <input
                type="text"
                placeholder={t('placeholder_polyclinic_name') || "Örn: Dahiliye Polikliniği 1"}
                required
                value={name}
                onChange={(e) => setName(e.target.value)}
                style={{ width: '100%', padding: '0.8rem', borderRadius: '8px', border: '1px solid var(--border)', backgroundColor: 'var(--background)', color: 'var(--text-main)', outline: 'none' }}
              />
            </div>

            <div>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.85rem', fontWeight: '500', color: 'var(--text-muted)' }}>{t('room_number')}</label>
              <input
                type="text"
                placeholder={t('placeholder_room_number') || "Örn: B Blok 104"}
                required
                value={roomNumber}
                onChange={(e) => setRoomNumber(e.target.value)}
                style={{ width: '100%', padding: '0.8rem', borderRadius: '8px', border: '1px solid var(--border)', backgroundColor: 'var(--background)', color: 'var(--text-main)', outline: 'none' }}
              />
            </div>

            <button type="submit" style={{
              padding: '0.8rem',
              backgroundColor: 'var(--primary)',
              color: 'white',
              border: 'none',
              borderRadius: '8px',
              fontWeight: '600',
              marginTop: '0.5rem',
              cursor: 'pointer',
              transition: 'background-color 0.2s',
              boxShadow: '0 4px 12px rgba(var(--primary-rgb), 0.3)'
            }}>
              {t('save_to_system')}
            </button>
          </form>
        </div>

        {/* Liste */}
        <div style={{
          backgroundColor: 'var(--surface)',
          padding: '0',
          borderRadius: '16px',
          boxShadow: '0 4px 20px rgba(0,0,0,0.05)',
          border: '1px solid var(--border)',
          overflow: 'hidden'
        }}>
          <div style={{ padding: '1.5rem', borderBottom: '1px solid var(--border)', display: 'flex', justifyContent: 'space-between', alignItems: 'center', backgroundColor: 'rgba(var(--background-rgb), 0.5)', flexWrap: 'wrap', gap: '1rem' }}>
            <h2 style={{ fontSize: '1.1rem', fontWeight: '600', color: 'var(--text-main)', margin: 0 }}>
              {filterDeptId ? `${getDeptName(filterDeptId)} ${t('polyclinics')}` : (t('all_polyclinics'))}
            </h2>
            <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', backgroundColor: 'var(--background)', padding: '0.2rem 0.5rem 0.2rem 1rem', borderRadius: '8px', border: '1px solid var(--border)' }}>
                <span style={{ fontSize: '0.85rem', fontWeight: '500', color: 'var(--text-muted)', whiteSpace: 'nowrap' }}>Kayıt Sayısı:</span>
                <select 
                  value={itemsPerPage} 
                  onChange={(e) => { setItemsPerPage(Number(e.target.value)); setPage(1); }}
                  style={{ width: 'auto', padding: '0.4rem 2rem 0.4rem 0.8rem', border: 'none', backgroundColor: 'transparent', boxShadow: 'none', fontWeight: '600', color: 'var(--primary)' }}
                >
                  <option value={3}>3</option>
                  <option value={4}>4</option>
                  <option value={5}>5</option>
                  <option value={10}>10</option>
                  <option value={20}>20</option>
                </select>
              </div>
              <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)', backgroundColor: 'var(--background)', padding: '4px 12px', borderRadius: '20px', border: '1px solid var(--border)' }}>
                {t('total')} {filteredPolyclinics.length}
              </span>
            </div>
          </div>

          {loading ? (
            <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>
              <div style={{ width: '40px', height: '40px', border: '3px solid rgba(var(--primary-rgb), 0.2)', borderTopColor: 'var(--primary)', borderRadius: '50%', animation: 'spin 1s linear infinite', margin: '0 auto 1rem' }}></div>
              {t('loading_data')}
            </div>
          ) : filteredPolyclinics.length === 0 ? (
            <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>
              <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1" strokeLinecap="round" strokeLinejoin="round" style={{ marginBottom: '1rem', opacity: 0.5 }}><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
              <p>{filterDeptId ? t('no_polyclinics_dept') : t('no_polyclinics_system')}</p>
            </div>
          ) : (
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                <thead>
                  <tr style={{ backgroundColor: 'rgba(var(--background-rgb), 0.3)' }}>
                    <th style={{ padding: '1rem 1.5rem', fontSize: '0.75rem', textTransform: 'uppercase', letterSpacing: '0.05em', color: 'var(--text-muted)', borderBottom: '1px solid var(--border)' }}>{t('room_no')}</th>
                    <th style={{ padding: '1rem 1.5rem', fontSize: '0.75rem', textTransform: 'uppercase', letterSpacing: '0.05em', color: 'var(--text-muted)', borderBottom: '1px solid var(--border)' }}>{t('polyclinic_name_col')}</th>
                    <th style={{ padding: '1rem 1.5rem', fontSize: '0.75rem', textTransform: 'uppercase', letterSpacing: '0.05em', color: 'var(--text-muted)', borderBottom: '1px solid var(--border)' }}>{t('linked_department')}</th>
                    <th style={{ padding: '1rem 1.5rem', fontSize: '0.75rem', textTransform: 'uppercase', letterSpacing: '0.05em', color: 'var(--text-muted)', borderBottom: '1px solid var(--border)', textAlign: 'right' }}>{t('action')}</th>
                  </tr>
                </thead>
                <tbody>
                  {paginatedPolyclinics.map((poly, idx) => (
                    <tr
                      key={poly.id}
                      style={{
                        borderBottom: idx === paginatedPolyclinics.length - 1 ? 'none' : '1px solid var(--border)',
                        transition: 'all 0.3s',
                        backgroundColor: highlightId && parseInt(highlightId) === poly.id ? 'rgba(var(--primary-rgb), 0.1)' : 'transparent'
                      }}
                    >
                      <td style={{ padding: '1rem 1.5rem', fontWeight: '500', color: 'var(--text-main)' }}>
                        <div style={{ display: 'inline-block', padding: '4px 8px', backgroundColor: 'var(--background)', border: '1px solid var(--border)', borderRadius: '6px', fontSize: '0.85rem' }}>
                          {poly.roomNumber}
                        </div>
                      </td>
                      <td style={{ padding: '1rem 1.5rem', color: 'var(--text-main)', fontWeight: '500' }}>{poly.name}</td>
                      <td style={{ padding: '1rem 1.5rem' }}>
                        <span style={{
                          display: 'inline-flex', alignItems: 'center', gap: '4px',
                          padding: '4px 10px',
                          backgroundColor: 'rgba(var(--primary-rgb), 0.08)',
                          color: 'var(--primary)',
                          borderRadius: '20px',
                          fontSize: '0.85rem',
                          fontWeight: '500'
                        }}>
                          <div style={{ width: '6px', height: '6px', borderRadius: '50%', backgroundColor: 'currentColor' }}></div>
                          {getDeptName(poly.departmentId)}
                        </span>
                      </td>
                      <td style={{ padding: '1rem 1.5rem', textAlign: 'right' }}>
                        <button
                          onClick={() => handleDelete(poly.id)}
                          style={{
                            padding: '6px 12px',
                            backgroundColor: 'rgba(239, 68, 68, 0.1)',
                            color: '#ef4444',
                            border: '1px solid rgba(239, 68, 68, 0.2)',
                            borderRadius: '6px',
                            cursor: 'pointer',
                            fontSize: '0.85rem',
                            fontWeight: '500',
                            transition: 'all 0.2s'
                          }}
                          onMouseOver={(e) => { e.currentTarget.style.backgroundColor = '#ef4444'; e.currentTarget.style.color = 'white'; }}
                          onMouseOut={(e) => { e.currentTarget.style.backgroundColor = 'rgba(239, 68, 68, 0.1)'; e.currentTarget.style.color = '#ef4444'; }}
                        >
                          {t('delete')}
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {/* Pagination Controls */}
          {filteredPolyclinics.length > 0 && (
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '1rem', padding: '1rem', borderTop: '1px solid var(--border)', backgroundColor: 'rgba(var(--background-rgb), 0.3)' }}>
              <button
                onClick={() => setPage(p => Math.max(1, p - 1))}
                disabled={page === 1}
                style={{
                  padding: '0.5rem 1rem',
                  backgroundColor: page === 1 ? 'var(--surface-hover)' : 'var(--surface)',
                  color: page === 1 ? 'var(--text-muted)' : 'var(--text-main)',
                  border: '1px solid var(--border)',
                  borderRadius: '8px',
                  cursor: page === 1 ? 'not-allowed' : 'pointer'
                }}
              >
                {t('previous')}
              </button>
              <span style={{ fontSize: '0.9rem', color: 'var(--text-main)', fontWeight: '500' }}>
                {t('page')} {page} / {totalPages}
              </span>
              <button
                onClick={() => setPage(p => Math.min(totalPages, p + 1))}
                disabled={page === totalPages}
                style={{
                  padding: '0.5rem 1rem',
                  backgroundColor: page === totalPages ? 'var(--surface-hover)' : 'var(--surface)',
                  color: page === totalPages ? 'var(--text-muted)' : 'var(--text-main)',
                  border: '1px solid var(--border)',
                  borderRadius: '8px',
                  cursor: page === totalPages ? 'not-allowed' : 'pointer'
                }}
              >
                {t('next')}
              </button>
            </div>
          )}
        </div>
      </div>
      <style dangerouslySetInnerHTML={{__html: `
        @keyframes spin { 100% { transform: rotate(360deg); } }
      `}} />
    </div>
  );
}
