import { handleApiError } from './index';
import type { Campus } from '../types/api';

const API_BASE = (import.meta as any).env?.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

export const getCampusList = async (): Promise<Campus[]> => {
  try {
    const response = await fetch(`${API_BASE}/campus`, {
      method: 'GET',
      credentials: 'include',
    });
    
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }
    
    const data = await response.json();
    return data;
  } catch (error) {
    throw handleApiError(error);
  }
};
