'use client';

import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { useRouter, usePathname } from 'next/navigation';
import { AuthService } from '../services/api';
import LoadingScreen from '../components/LoadingScreen';

const AuthContext = createContext();

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [role, setRole] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  
  const router = useRouter();
  const pathname = usePathname();

  const isAuthPage = pathname === '/login' || pathname === '/register' || pathname === '/forgot-password';

  const checkAuth = useCallback(async () => {
    try {
      setIsLoading(true);
      
      try {
        const userData = await AuthService.getMe();
        setUser(userData);
        setRole(userData.role);
        setIsAuthenticated(true);
      } catch (err) {
        // Fetch failed (likely 401 Unauthorized), which means no active session
        setIsAuthenticated(false);
        setUser(null);
        setRole(null);
      }
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    checkAuth();
  }, [checkAuth]);

  // Handle routing protection
  useEffect(() => {
    if (!isLoading) {
      if (!isAuthenticated && !isAuthPage) {
        router.replace('/login');
      }
    }
  }, [isLoading, isAuthenticated, isAuthPage, router]);

  const value = {
    user,
    role,
    isAuthenticated,
    isLoading,
    refreshUser: checkAuth
  };

  // Prevent flicker by showing a global loading screen if we are still checking auth,
  // EXCEPT on auth pages where we don't need auth to show the page.
  if (isLoading && !isAuthPage) {
    return <LoadingScreen />;
  }

  // Also prevent rendering protected content if not authenticated (before the redirect happens)
  if (!isLoading && !isAuthenticated && !isAuthPage) {
    return null;
  }

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
