import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/v1';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor - token ekle
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor - hata yönetimi
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Auth API
export const authAPI = {
  register: (data) => api.post('/auth/kayit', data),
  login: (data) => api.post('/auth/giris', data),
  getProfile: () => api.get('/kullanici/profil'),
};

// Scraping API
export const scrapingAPI = {
  start: (data) => api.post('/scraping/baslat', data),
  getStatus: (islemId) => api.get(`/scraping/durum/${islemId}`),
};

// İlan API
export const ilanAPI = {
  list: (params) => api.get('/ilanlar', { params }),
  getById: (id) => api.get(`/ilanlar/${id}`),
};

// Şablon API
export const sablonAPI = {
  create: (data) => api.post('/sablonlar', data),
  list: () => api.get('/sablonlar'),
};

// Mesajlaşma API
export const mesajAPI = {
  send: (data) => api.post('/mesajlasma/gonder', data),
  getQueueStatus: () => api.get('/mesajlasma/kuyruk-durumu'),
};

export default api;

