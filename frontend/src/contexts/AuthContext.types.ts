import { createContext } from 'react';

export interface User {
  sub: string;
  email: string;
  name: string;
  given_name?: string;
  family_name?: string;
  email_verified: boolean;
  preferred_username: string;
}

export interface AuthContextType {
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  token: string | null;
  getUser: () => User | null;
  login: () => Promise<void>;
  logout: () => Promise<void>;
  clearError: () => void;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);
