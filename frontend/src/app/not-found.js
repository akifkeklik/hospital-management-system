import Link from 'next/link';

export default function NotFound() {
  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        minHeight: '100vh',
        fontFamily: 'var(--font-inter), Inter, system-ui, sans-serif',
        color: '#334155',
        background: '#f8fafc',
        textAlign: 'center',
        padding: '2rem',
      }}
    >
      <h1
        style={{
          fontSize: '6rem',
          fontWeight: 800,
          margin: 0,
          lineHeight: 1,
          background: 'linear-gradient(135deg, #4f46e5, #7c3aed)',
          WebkitBackgroundClip: 'text',
          WebkitTextFillColor: 'transparent',
        }}
      >
        404
      </h1>
      <p style={{ fontSize: '1.25rem', marginTop: '1rem', color: '#64748b' }}>
        Sayfa bulunamadı / Page not found
      </p>
      <Link
        href="/"
        style={{
          marginTop: '2rem',
          padding: '0.75rem 2rem',
          borderRadius: '0.5rem',
          background: '#4f46e5',
          color: '#fff',
          textDecoration: 'none',
          fontWeight: 600,
          fontSize: '1rem',
          transition: 'background 0.2s',
        }}
      >
        Ana Sayfa / Home
      </Link>
    </div>
  );
}
