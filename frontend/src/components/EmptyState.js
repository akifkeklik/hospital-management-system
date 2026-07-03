import styles from './EmptyState.module.css';

export default function EmptyState({ title, description, customSvg }) {
  const DefaultSvg = () => (
    <svg width="120" height="120" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1" strokeLinecap="round" strokeLinejoin="round" style={{ opacity: 0.6, color: 'var(--primary)' }}>
      <rect x="3" y="3" width="18" height="18" rx="2" ry="2" strokeDasharray="4 4" />
      <circle cx="8.5" cy="8.5" r="1.5" fill="currentColor" />
      <polyline points="21 15 16 10 5 21" />
    </svg>
  );

  return (
    <div className={styles.emptyState}>
      <div className={styles.emptyIcon}>
        {customSvg ? customSvg : <DefaultSvg />}
      </div>
      <h3 className={styles.emptyTitle}>{title}</h3>
      {description && <p className={styles.emptyDesc}>{description}</p>}
    </div>
  );
}
