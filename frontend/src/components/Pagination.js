import { useSettings } from '../context/SettingsContext';
import styles from './Pagination.module.css';

export default function Pagination({ page, totalPages, onPageChange }) {
  const { t } = useSettings();

  if (totalPages <= 1) {
    return null;
  }

  return (
    <div className={styles.pagination}>
      <button 
        className={styles.pageBtn} 
        disabled={page === 0} 
        onClick={() => onPageChange(page - 1)}
      >
        {t('previous')}
      </button>
      <span className={styles.pageInfo}>{t('page')} {page + 1} / {totalPages}</span>
      <button 
        className={styles.pageBtn} 
        disabled={page >= totalPages - 1} 
        onClick={() => onPageChange(page + 1)}
      >
        {t('next')}
      </button>
    </div>
  );
}
