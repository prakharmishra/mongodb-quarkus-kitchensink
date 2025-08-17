import axios from 'axios';
import { keycloak } from '../utils/auth';

export interface Member {
  id: string;
  userId: string;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
}

export interface CursorPage<T> {
  data: T[];
  nextCursor?: string;
  hasMore: boolean;
}

interface ErrorResponse {
  errors: string[];
}

const api = axios.create({
  baseURL: '/api'
});

// Add a request interceptor to include the auth token
api.interceptors.request.use(
  (config) => {
    if (keycloak?.token) {
      config.headers.Authorization = `Bearer ${keycloak.token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // If the response indicates registration is required
      if (error.response?.data?.registrationUrl) {
        window.location.href = error.response.data.registrationUrl;
        return;
      }
    }

    if (error.response?.data?.errors) {
      return Promise.reject({
        message: error.response.data.errors.join(', '),
        errors: error.response.data.errors
      });
    }
    return Promise.reject({
      message: error.message || 'An unexpected error occurred',
      errors: [error.message || 'An unexpected error occurred']
    });
  }
);

export interface RegistrationData {
  userId: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  createdAt: string;
  complete: boolean;
}

export const MemberService = {
  async getAllMembers(cursor?: string, limit: number = 10): Promise<CursorPage<Member>> {
    const response = await api.get('/members', { params: { cursor, limit } });
    return response.data;
  },

  async getMember(id: string): Promise<Member> {
    const response = await api.get(`/members/${id}`);
    return response.data;
  },

  async createMember(member: Omit<Member, 'id'>): Promise<Member> {
    const response = await api.post('/members', member);
    return response.data;
  },

  async updateMember(id: string, member: Omit<Member, 'id'>): Promise<Member> {
    const response = await api.put(`/members/${id}`, member);
    return response.data;
  },

  async deleteMember(id: string): Promise<void> {
    await api.delete(`/members/${id}`);
  }
};

export const RegistrationService = {
  async getRegistrationData(): Promise<RegistrationData> {
    const response = await api.get('/registration');
    return response.data;
  },

  async completeRegistration(data: { firstName: string; lastName: string; phoneNumber: string }): Promise<void> {
    await api.put('/registration/complete', data);
  }
};
