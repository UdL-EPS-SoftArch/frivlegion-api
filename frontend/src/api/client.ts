import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import { ApiError, Game, HalCollection } from '../types/api';

export const TOKEN_STORAGE_KEY = 'frivlegion_token';

export const API_BASE_URL =
  import.meta.env.VITE_API_URL || 'http://localhost:8080';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = localStorage.getItem(TOKEN_STORAGE_KEY);
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiError & { errors?: Array<{ property?: string; message?: string }> }>) => {
    const status = error.response?.status;
    const requestUrl = error.config?.url || '';

    if (status === 401 && !requestUrl.includes('/auth/login')) {
      localStorage.removeItem(TOKEN_STORAGE_KEY);
    }

    const data = error.response?.data;
    let message = 'An unexpected error occurred';

    if (data && typeof data.message === 'string' && data.message.trim().length > 0) {
      message = data.message;
    } else if (data && Array.isArray(data.errors) && data.errors.length > 0) {
      message = data.errors
        .map((e) => (e.property ? `${e.property}: ${e.message}` : e.message))
        .filter(Boolean)
        .join('; ');
    } else if (typeof error.message === 'string' && error.message.trim().length > 0) {
      message = error.message;
    }

    const normalizedError = new Error(message) as Error & { status?: number };
    normalizedError.status = status;
    return Promise.reject(normalizedError);
  }
);

export function normalizeGame(raw: Game): Game {
  const selfHref = raw._links?.self?.href || raw.uri || '';
  const extractedId =
    raw.id ||
    (selfHref ? decodeURIComponent(selfHref.split('/').filter(Boolean).pop() || '') : '');
  return {
    ...raw,
    id: extractedId,
  };
}

export function extractHalGames(data: HalCollection<'games', Game>): Game[] {
  const list = data?._embedded?.games || [];
  return list.map(normalizeGame);
}
