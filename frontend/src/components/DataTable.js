import { useSettings } from '../context/SettingsContext';
import styles from './DataTable.module.css';
import Pagination from './Pagination';

export default function DataTable({ columns, data, onEdit, onDelete, actions, page = 0, totalPages = 0, onPageChange }) {
  const { t } = useSettings();

  if (!data || data.length === 0) {
    return <div className={styles.emptyState}>{t('no_data')}</div>;
  }

  return (
    <div className={styles.tableContainer}>
      <table className={styles.table}>
        <thead>
          <tr>
            {columns.map((col, idx) => (
              <th key={idx}>{col.header}</th>
            ))}
            {(onEdit || onDelete || actions) && <th>{t('actions')}</th>}
          </tr>
        </thead>
        <tbody>
          {data.map((row, rowIndex) => (
            <tr key={row.id || rowIndex}>
              {columns.map((col, colIndex) => (
                <td key={colIndex}>
                  {col.render ? col.render(row) : row[col.accessor]}
                </td>
              ))}
              {(onEdit || onDelete || actions) && (
                <td className={styles.actions}>
                  {actions && actions(row)}
                  {onEdit && (
                    <button className={styles.editBtn} onClick={() => onEdit(row)}>
                      {t('edit')}
                    </button>
                  )}
                  {onDelete && (
                    <button className={styles.deleteBtn} onClick={() => onDelete(row.id)}>
                      {t('delete')}
                    </button>
                  )}
                </td>
              )}
            </tr>
          ))}
        </tbody>
      </table>
      
      <Pagination 
        page={page} 
        totalPages={totalPages} 
        onPageChange={onPageChange} 
      />
    </div>
  );
}
