import React, { useContext, useEffect, useState } from 'react';
import { AuthContext } from './AuthContext.types';
import type { User, AuthContextType } from './AuthContext.types';
import { keycloak, initKeycloak } from '../utils/auth';

const isDev = process.env.NODE_ENV === 'development';
const log = (...args: any[]) => isDev && console.log(...args);
const logError = (...args: any[]) => console.error(...args);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [user, setUser] = useState<User | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [initialized, setInitialized] = useState(false);

  useEffect(() => {
    if (initialized) return;
    const initAuth = async () => {
      try {
        setError(null);
        
        // Skip initialization if we're in the middle of the OAuth callback
        if (window.location.hash && window.location.hash.includes('access_token')) {
          log('Skipping auth init during OAuth callback');
          return;
        }

        const authenticated = await initKeycloak();
        log('Auth initialized, authenticated:', authenticated);

        if (authenticated) {
          log('User is authenticated');
          setIsAuthenticated(true);
          setUser(keycloak.tokenParsed as User);

          // Set up token refresh
          keycloak.onTokenExpired = () => {
            log('Token expired, updating...');
            keycloak.updateToken(-1)
              .then(refreshed => {
                if (refreshed) {
                  log('Token refreshed successfully');
                } else {
                  log('Token still valid, no refresh needed');
                }
              })
              .catch(() => {
                logError('Token refresh failed');
                setIsAuthenticated(false);
                setUser(null);
                setError('Session expired. Please log in again.');
              });
          };
        } else {
          log('User not authenticated, but not redirecting automatically');
          setIsAuthenticated(false);
        }
      } catch (error) {
        logError('Auth initialization failed:', error);
        setIsAuthenticated(false);
        setError('Authentication failed. Please try again.');
      } finally {
        setIsLoading(false);
      }
    };

    initAuth();
    setInitialized(true);
  }, [initialized]);

  const login = async () => {
    try {
      setError(null);
      setIsLoading(true);
      await keycloak.login({
        redirectUri: window.location.origin
      });
    } catch (error) {
      logError('Login failed:', error);
      setError('Login failed. Please try again.');
      setIsLoading(false);
    }
  };

  const logout = async () => {
    try {
      setError(null);
      setIsLoading(true);
      setIsAuthenticated(false);
      setUser(null);
      await keycloak.logout({
        redirectUri: window.location.origin
      });
    } catch (error) {
      logError('Logout failed:', error);
      setError('Logout failed. Please try again.');
      setIsLoading(false);
    }
  };

  const getUser = () => user;
  
  const getToken = () => keycloak?.token || null;
  
  const clearError = () => setError(null);

  const value = {
    isAuthenticated,
    isLoading,
    error,
    token: getToken(),
    getUser,
    login,
    logout,
    clearError
  };

  if (isLoading) {
    return <div>Loading...</div>;
  }

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
