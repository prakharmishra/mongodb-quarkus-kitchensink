import axios from 'axios';

export interface Member {
  id: string;
  name: string;
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

api.interceptors.response.use(
  (response) => response,
  (error) => {
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
