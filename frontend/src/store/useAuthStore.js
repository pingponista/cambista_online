import { create } from 'zustand';

export const useAuthStore = create((set) => ({
  user: null,
  token: localStorage.getItem('cambista_jwt_token') || null,
  isAuthenticated: !!localStorage.getItem('cambista_jwt_token'),
  profileType: 'NATURAL', // NATURAL or JURIDICA

  setProfileType: (type) => set({ profileType: type }),

  setAuth: (authData) => {
    localStorage.setItem('cambista_jwt_token', authData.token);
    set({
      user: {
        id: authData.userId,
        email: authData.email,
        name: authData.fullNameOrCompany,
        profileType: authData.profileType,
      },
      token: authData.token,
      isAuthenticated: true,
      profileType: authData.profileType,
    });
  },

  logout: () => {
    localStorage.removeItem('cambista_jwt_token');
    set({ user: null, token: null, isAuthenticated: false, profileType: 'NATURAL' });
  },
}));
