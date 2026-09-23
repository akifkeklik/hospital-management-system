const rawUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
const API_BASE_URL = rawUrl.endsWith('/api') ? rawUrl : `${rawUrl}/api`;

export async function fetchAPI(endpoint, options = {}) {
  // Runtime guard: ensure NEXT_PUBLIC_API_URL is set in production (does NOT run at build time)
  if (typeof window !== 'undefined' && !process.env.NEXT_PUBLIC_API_URL && process.env.NODE_ENV === 'production') {
    throw new Error('CRITICAL: NEXT_PUBLIC_API_URL must be provided in production environment variables.');
  }
  const url = `${API_BASE_URL}${endpoint}`;
  
  const defaultHeaders = {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  };

  const config = {
    ...options,
    credentials: 'include', // Cookie'leri backend'e gönder!
    headers: {
      ...defaultHeaders,
      ...options.headers,
    },
  };

  try {
    const response = await fetch(url, config);

    // Eğer başarılı değilse hata fırlat
    if (!response.ok) {
      // Sadece açık 401 (Unauthorized) hatalarında login'e yönlendir
      // /me endpointi 500 dönerse bu geçici bir hata olabilir, token silme!
      if (response.status === 401) {
        if (typeof window !== 'undefined' && window.location.pathname !== '/login' && window.location.pathname !== '/register' && window.location.pathname !== '/forgot-password') {
          window.location.href = '/login';
          return new Promise(() => {}); // Redirect esnasında hata fırlatmayı engellemek için askıda bırak
        }
      }

      let errorMessage = response.statusText;
      try {
        const rawText = await response.text();
        if (rawText) {
          try {
            const errorData = JSON.parse(rawText);
            if (errorData.details) {
              const detailsStr = Object.values(errorData.details).join('\n');
              errorMessage = `${errorData.error}:\n${detailsStr}`;
            } else if (errorData.message) {
              errorMessage = errorData.message;
            }
          } catch (e) {
            errorMessage = rawText; // Not JSON, use raw text
          }
        }
      } catch (e) {
        // Ignore and fallback to statusText
      }
      throw new Error(errorMessage);
    }

    // 204 No Content ise boş dön (Örn: DELETE işleminde)
    if (response.status === 204) {
      return null;
    }

    const text = await response.text();
    return text ? JSON.parse(text) : null;
  } catch (error) {
    // console.error(`Fetch error on ${url}:`, error); // Removed to prevent Next.js overlay
    throw error;
  }
}

// ── BÖLÜM (DEPARTMENT) API ──
export const DepartmentService = {
  getAll: (page = 0, size = 5, options = {}) => fetchAPI(`/departments?page=${page}&size=${size}`, options),
  search: (query, page = 0, size = 5) => fetchAPI(`/departments/search?query=${encodeURIComponent(query)}&page=${page}&size=${size}`),
  getById: (id) => fetchAPI(`/departments/${id}`),
  create: (data) => fetchAPI('/departments', { method: 'POST', body: JSON.stringify(data) }),
  update: (id, data) => fetchAPI(`/departments/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  delete: (id) => fetchAPI(`/departments/${id}`, { method: 'DELETE' }),
};

// ── HASTA (PATIENT) API ──
export const PatientService = {
  getAll: (page = 0, size = 5, options = {}) => fetchAPI(`/patients?page=${page}&size=${size}`, options),
  search: (query, page = 0, size = 5) => fetchAPI(`/patients/search?query=${encodeURIComponent(query)}&page=${page}&size=${size}`),
  getById: (id) => fetchAPI(`/patients/${id}`),
  create: (data) => fetchAPI('/patients', { method: 'POST', body: JSON.stringify(data) }),
  update: (id, data) => fetchAPI(`/patients/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  delete: (id) => fetchAPI(`/patients/${id}`, { method: 'DELETE' }),
};

// ── DOKTOR (DOCTOR) API ──
export const DoctorService = {
  getAll: (page = 0, size = 5, options = {}) => fetchAPI(`/doctors?page=${page}&size=${size}`, options),
  search: (query, page = 0, size = 5, options = {}) => fetchAPI(`/doctors/search?query=${encodeURIComponent(query)}&page=${page}&size=${size}`, options),
  getById: (id, options = {}) => fetchAPI(`/doctors/${id}`, options),
  create: (data) => fetchAPI('/doctors', { method: 'POST', body: JSON.stringify(data) }),
  update: (id, data) => fetchAPI(`/doctors/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  delete: (id) => fetchAPI(`/doctors/${id}`, { method: 'DELETE' }),
};

// Helper to build query strings
const buildQueryString = (params) => {
  const query = new URLSearchParams();
  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== null && value !== '') {
      query.append(key, value);
    }
  }
  return query.toString();
};

// ── RANDEVU (APPOINTMENT) API ──
export const AppointmentService = {
  getAll: (page = 0, size = 5, options = {}) => fetchAPI(`/appointments?page=${page}&size=${size}`, options).then(normalizePagination),
  getById: (id, options = {}) => fetchAPI(`/appointments/${id}`, options),
  getByPatient: (patientId, page = 0, size = 100, filters = {}, options = {}) => fetchAPI(`/appointments/patient/${patientId}?${buildQueryString({ page, size, ...filters })}`, options).then(normalizePagination),
  getByDoctor: (doctorId, page = 0, size = 100, filters = {}, options = {}) => fetchAPI(`/appointments/doctor/${doctorId}?${buildQueryString({ page, size, ...filters })}`, options).then(normalizePagination),
  getWaitEstimate: (appointmentId, options = {}) => fetchAPI(`/appointments/${appointmentId}/wait-estimate`, options),
  create: (data) => fetchAPI('/appointments', { method: 'POST', body: JSON.stringify(data) }),
  update: (id, data) => fetchAPI(`/appointments/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  updateStatus: (id, status) => fetchAPI(`/appointments/${id}/status`, { 
    method: 'PATCH', 
    body: JSON.stringify({ status }) 
  }),
  delete: (id) => fetchAPI(`/appointments/${id}`, { method: 'DELETE' }),
  getAvailableSlots: (doctorId, date) => fetchAPI(`/appointments/available-slots?doctorId=${doctorId}&date=${date}`),
};

// ── AUTHENTICATION API ──
export const AuthService = {
  login: (username, password) => fetchAPI('/auth/login', { 
    method: 'POST', 
    body: JSON.stringify({ username, password }) 
  }),
  register: (data) => fetchAPI('/auth/register', { 
    method: 'POST', 
    body: JSON.stringify(data) 
  }),
  doctorRegister: (data) => fetchAPI('/auth/doctor-register', {
    method: 'POST',
    body: JSON.stringify(data)
  }),
  forgotPassword: (tcIdentityNumber, email) => fetchAPI('/auth/forgot-password', {
    method: 'POST',
    body: JSON.stringify({ tcIdentityNumber, email })
  }),
  resetPassword: (token, newPassword) => fetchAPI('/auth/reset-password', {
    method: 'POST',
    body: JSON.stringify({ token, newPassword })
  }),
  forceChangePassword: (tcIdentityNumber, newPassword) => fetchAPI('/auth/force-change-password', {
    method: 'POST',
    body: JSON.stringify({ tcIdentityNumber, newPassword })
  }),
  getMe: (options = {}) => fetchAPI('/auth/me', options),
  logout: async () => {
    if (typeof window !== 'undefined') {
      try {
        await fetchAPI('/auth/logout', { method: 'POST' });
      } catch (e) {
        console.error("Logout fetch error", e);
      }
      // Geri butonu ile tekrar girişi engellemek için replace kullan
      window.location.replace('/login');
    }
  }
};

// ── SYSTEM SETTINGS API ──
export const SystemSettingService = {
  getSettings: () => fetchAPI('/admin/settings'),
  updateSettings: (data) => fetchAPI('/admin/settings', {
    method: 'PUT',
    body: JSON.stringify(data)
  })
};

// ── DOCTOR LEAVES API ──
export const DoctorLeaveService = {
  getAll: () => fetchAPI('/doctor-leaves'),
  getByDoctorId: (doctorId) => fetchAPI(`/doctor-leaves/doctor/${doctorId}`),
  create: (data) => fetchAPI('/doctor-leaves', {
    method: 'POST',
    body: JSON.stringify(data)
  }),
  updateStatus: (id, status) => fetchAPI(`/doctor-leaves/${id}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ status })
  }),
  delete: (id) => fetchAPI(`/doctor-leaves/${id}`, {
    method: 'DELETE'
  })
};

// ── NOTIFICATION API ──
export const NotificationService = {
  getByPatient: (patientId, options = {}) => fetchAPI(`/notifications/patient/${patientId}`, options),
  getUnreadCountByPatient: (patientId, options = {}) => fetchAPI(`/notifications/patient/${patientId}/unread-count`, options),
  getByDoctor: (doctorId, options = {}) => fetchAPI(`/notifications/doctor/${doctorId}`, options),
  getUnreadCountByDoctor: (doctorId, options = {}) => fetchAPI(`/notifications/doctor/${doctorId}/unread-count`, options),
  getByAdmin: (adminId, options = {}) => fetchAPI(`/notifications/admin/${adminId}`, options),
  getUnreadCountByAdmin: (adminId, options = {}) => fetchAPI(`/notifications/admin/${adminId}/unread-count`, options),
  broadcastToDoctors: (message) => fetchAPI('/notifications/broadcast', {
    method: 'POST',
    body: JSON.stringify(message)
  }),
  broadcastToAll: (message) => fetchAPI('/notifications/broadcast-all', {
    method: 'POST',
    body: JSON.stringify(message)
  }),
  markAsRead: (id) => fetchAPI(`/notifications/${id}/read`, { method: 'PATCH' })
};

// ── POLYCLINIC API ──
export const PolyclinicService = {
  getAll: (options = {}) => fetchAPI('/admin/polyclinics', options),
  getByDepartmentId: (departmentId, options = {}) => fetchAPI(`/admin/polyclinics/department/${departmentId}`, options),
  create: (data) => fetchAPI('/admin/polyclinics', {
    method: 'POST',
    body: JSON.stringify(data)
  }),
  update: (id, data) => fetchAPI(`/admin/polyclinics/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data)
  }),
  delete: (id) => fetchAPI(`/admin/polyclinics/${id}`, {
    method: 'DELETE'
  })
};

// ── YAPAY ZEKA (AI) SERVİSİ ──
export const AiService = {
  analyzeSymptoms: (symptoms, availableDepartments) => fetchAPI('/ai/analyze-symptoms', {
    method: 'POST',
    body: JSON.stringify({ symptoms, availableDepartments })
  })
};

// ── EXAMINATION API ──
export const ExaminationService = {
  addDiagnosis: (data) => fetchAPI('/examinations/diagnoses', { method: 'POST', body: JSON.stringify(data) }),
  getDiagnoses: (appointmentId) => fetchAPI(`/examinations/appointments/${appointmentId}/diagnoses`),
  deleteDiagnosis: (id) => fetchAPI(`/examinations/diagnoses/${id}`, { method: 'DELETE' }),
  
  addPrescription: (data) => fetchAPI('/examinations/prescriptions', { method: 'POST', body: JSON.stringify(data) }),
  getPrescriptions: (appointmentId) => fetchAPI(`/examinations/appointments/${appointmentId}/prescriptions`),
  deletePrescription: (id) => fetchAPI(`/examinations/prescriptions/${id}`, { method: 'DELETE' })
};

// ── DASHBOARD STATS API ──
export const DashboardStatsService = {
  getDashboardStats: () => fetchAPI('/stats/dashboard')
};

// Normalizes standard Pageable responses
const normalizePagination = (res) => {
  if (res && res.content !== undefined) {
    return {
      items: res.content,
      page: res.page?.number || 0,
      totalPages: res.page?.totalPages || (res.totalPages !== undefined ? res.totalPages : 1),
      totalElements: res.page?.totalElements || res.totalElements || res.content.length
    };
  }
  // Fallback for list endpoints
  return {
    items: res || [],
    page: 0,
    totalPages: 1,
    totalElements: res ? res.length : 0
  };
};

