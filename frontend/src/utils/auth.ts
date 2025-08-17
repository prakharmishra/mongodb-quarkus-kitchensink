import Keycloak from 'keycloak-js';

const keycloakConfig = {
  url: import.meta.env.VITE_KEYCLOAK_URL || window.location.origin,  // Use the proxied URL
  realm: import.meta.env.VITE_KEYCLOAK_REALM || 'KitchensinkDev',
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID || 'kitchensink-frontend'
};

export const keycloak = new Keycloak(keycloakConfig);

let isInitialized = false;

export const initKeycloak = () => {
  if (isInitialized) {
    return Promise.resolve(keycloak.authenticated || false);
  }
  
  isInitialized = true;
  return keycloak.init({
    onLoad: 'login-required',
    pkceMethod: 'S256',
    checkLoginIframe: false,
    flow: 'standard',
    enableLogging: true
  });
};

export const login = async () => {
  try {
    await keycloak.login({
      redirectUri: window.location.origin,
      prompt: 'login'
    });
  } catch (error) {
    console.error('Login failed:', error);
    throw error;
  }
};

export const logout = async () => {
  try {
    await keycloak.logout({
      redirectUri: window.location.origin
    });
  } catch (error) {
    console.error('Logout failed:', error);
    throw error;
  }
};

export const updateToken = async (minValidity: number = 70) => {
  try {
    const refreshed = await keycloak.updateToken(minValidity);
    if (refreshed) {
      console.log('Token was successfully refreshed');
    } else {
      console.log('Token is still valid');
    }
  } catch (error) {
    console.error('Failed to refresh the token, or the session has expired');
    throw error;
  }
};

export const getToken = () => keycloak.token;

export const isAuthenticated = () => !!keycloak.authenticated;
