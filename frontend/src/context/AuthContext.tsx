import React, {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from 'react';
import { apiClient, TOKEN_STORAGE_KEY } from '../api/client';
import { AuthResponse, IdentityResponse } from '../types/api';

export interface AuthContextValue {
  user: IdentityResponse | null;
  token: string | null;
  roles: string[];
  isAuthenticated: boolean;
  isAdmin: boolean;
  isLoading: boolean;
  login: (username: string, password: string) => Promise<void>;
  register: (username: string, email: string, password: string) => Promise<void>;
  logout: () => void;
  refreshIdentity: () => Promise<IdentityResponse | null>;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const [token, setToken] = useState<string | null>(() =>
    localStorage.getItem(TOKEN_STORAGE_KEY)
  );
  const [user, setUser] = useState<IdentityResponse | null>(null);
  const [roles, setRoles] = useState<string[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(() =>
    Boolean(localStorage.getItem(TOKEN_STORAGE_KEY))
  );

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
    setToken(null);
    setUser(null);
    setRoles([]);
  }, []);

  const refreshIdentity = useCallback(async (): Promise<IdentityResponse | null> => {
    const activeToken = localStorage.getItem(TOKEN_STORAGE_KEY);
    if (!activeToken) {
      setUser(null);
      setRoles([]);
      return null;
    }

    try {
      const response = await apiClient.get<IdentityResponse>('/identity');
      const identity = response.data;
      setUser(identity);
      setRoles(Array.isArray(identity.roles) ? identity.roles : []);
      return identity;
    } catch {
      logout();
      return null;
    }
  }, [logout]);

  useEffect(() => {
    let isMounted = true;
    const storedToken = localStorage.getItem(TOKEN_STORAGE_KEY);
    if (!storedToken) {
      setIsLoading(false);
      return;
    }

    (async () => {
      setIsLoading(true);
      await refreshIdentity();
      if (isMounted) {
        setToken(localStorage.getItem(TOKEN_STORAGE_KEY));
        setIsLoading(false);
      }
    })();

    return () => {
      isMounted = false;
    };
  }, [refreshIdentity]);

  const login = useCallback(
    async (username: string, password: string): Promise<void> => {
      const response = await apiClient.post<AuthResponse>('/auth/login', {
        username,
        password,
      });
      const { token: jwt, roles: authRoles } = response.data;
      localStorage.setItem(TOKEN_STORAGE_KEY, jwt);
      setToken(jwt);
      if (Array.isArray(authRoles)) {
        setRoles(authRoles);
      }
      await refreshIdentity();
    },
    [refreshIdentity]
  );

  const register = useCallback(
    async (username: string, email: string, password: string): Promise<void> => {
      const response = await apiClient.post<AuthResponse>('/auth/register', {
        username,
        email,
        password,
      });
      const { token: jwt, roles: authRoles } = response.data;
      localStorage.setItem(TOKEN_STORAGE_KEY, jwt);
      setToken(jwt);
      if (Array.isArray(authRoles)) {
        setRoles(authRoles);
      }
      await refreshIdentity();
    },
    [refreshIdentity]
  );

  const value = useMemo<AuthContextValue>(() => {
    const effectiveRoles = user?.roles || roles;
    return {
      user,
      token,
      roles: effectiveRoles,
      isAuthenticated: Boolean(token && user),
      isAdmin: effectiveRoles.includes('ROLE_ADMIN'),
      isLoading,
      login,
      register,
      logout,
      refreshIdentity,
    };
  }, [user, token, roles, isLoading, login, register, logout, refreshIdentity]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return ctx;
}
