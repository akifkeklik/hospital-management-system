import React, { useState, useEffect, useRef } from 'react';

export default function AsyncSelect({ 
  value, 
  onChange, 
  loadOptions, 
  placeholder,
  disabled = false,
  initialLabel = ''
}) {
  const [query, setQuery] = useState(initialLabel);
  const [prevInitialLabel, setPrevInitialLabel] = useState(initialLabel);
  const [options, setOptions] = useState([]);
  const [isOpen, setIsOpen] = useState(false);
  const [loading, setLoading] = useState(false);

  if (initialLabel !== prevInitialLabel) {
    setPrevInitialLabel(initialLabel);
    if (initialLabel && !query && !value) {
      setQuery(initialLabel);
    }
  }
  const wrapperRef = useRef(null);

  useEffect(() => {
    function handleClickOutside(event) {
      if (wrapperRef.current && !wrapperRef.current.contains(event.target)) {
        setIsOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  useEffect(() => {
    if (!isOpen) return;
    
    const fetchOptions = async () => {
      setLoading(true);
      try {
        const results = await loadOptions(query);
        setOptions(results || []);
      } catch (err) {
        setOptions([]);
      } finally {
        setLoading(false);
      }
    };

    const delayDebounce = setTimeout(() => {
      fetchOptions();
    }, 300);

    return () => clearTimeout(delayDebounce);
  }, [query, isOpen, loadOptions]);

  const handleSelect = (option) => {
    setQuery(option.label);
    onChange(option.value);
    setIsOpen(false);
  };

  return (
    <div ref={wrapperRef} style={{ position: 'relative', width: '100%' }}>
      <input
        type="text"
        value={query}
        disabled={disabled}
        placeholder={placeholder}
        onChange={(e) => {
          setQuery(e.target.value);
          setIsOpen(true);
          onChange(''); // Reset value when typing
        }}
        onFocus={() => setIsOpen(true)}
        style={{
          width: '100%',
          padding: '8px 12px',
          borderRadius: '6px',
          border: '1px solid var(--border-color, #e2e8f0)',
          backgroundColor: disabled ? '#f8fafc' : '#fff',
          fontSize: '0.875rem'
        }}
      />
      {isOpen && (
        <div style={{
          position: 'absolute',
          top: '100%',
          left: 0,
          right: 0,
          zIndex: 50,
          maxHeight: '200px',
          overflowY: 'auto',
          backgroundColor: '#fff',
          border: '1px solid #e2e8f0',
          borderRadius: '6px',
          boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
          marginTop: '4px'
        }}>
          {loading ? (
            <div style={{ padding: '8px 12px', color: '#64748b', fontSize: '0.875rem' }}>Aranıyor...</div>
          ) : options.length > 0 ? (
            options.map((opt, i) => (
              <div
                key={i}
                onClick={() => handleSelect(opt)}
                style={{
                  padding: '8px 12px',
                  cursor: 'pointer',
                  fontSize: '0.875rem',
                  borderBottom: i < options.length - 1 ? '1px solid #f1f5f9' : 'none',
                }}
                onMouseEnter={(e) => e.target.style.backgroundColor = '#f8fafc'}
                onMouseLeave={(e) => e.target.style.backgroundColor = 'transparent'}
              >
                {opt.label}
              </div>
            ))
          ) : (
            <div style={{ padding: '8px 12px', color: '#64748b', fontSize: '0.875rem' }}>Sonuç bulunamadı</div>
          )}
        </div>
      )}
    </div>
  );
}
