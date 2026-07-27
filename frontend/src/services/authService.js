import apiAuth from './apiAuth';

export const DEMO_CREDENTIALS = {
  email: 'demo@cambistaonline.pe',
  password: 'demo1234',
};

export const authService = {
  login: async (email, password) => {
    try {
      // 1. Llamada REST al backend Spring Boot /api/v1/auth/login para obtener JWT real
      const tokenResponse = await apiAuth.login(email, password);
      const token = tokenResponse.accessToken;

      // Almacenamos el JWT real en localStorage para interceptores Axios
      localStorage.setItem('cambista_jwt_token', token);

      // 2. Llamada REST al backend /api/v1/auth/me para obtener datos del perfil
      let userProfile = null;
      try {
        const meResponse = await apiAuth.getMe();
        userProfile = meResponse.data;
      } catch (meErr) {
        console.warn('Could not fetch user profile from /me, using token data', meErr);
      }

      const name = userProfile
        ? (userProfile.firstName ? `${userProfile.firstName} ${userProfile.lastName}` : (userProfile.companyName || email))
        : email;

      return {
        token: token,
        userId: userProfile ? userProfile.id : 'user-id',
        email: email,
        fullNameOrCompany: name || email,
        profileType: userProfile ? (userProfile.role === 'J' ? 'JURIDICA' : 'NATURAL') : 'NATURAL',
      };
    } catch (err) {
      console.error('Error during login:', err);
      throw err;
    }
  },

  register: async (registerData) => {
    try {
      // 1. Llamada REST al backend Spring Boot /api/v1/auth/register
      await apiAuth.register(registerData);

      // 2. Inicia sesión automáticamente tras registrarse obteniendo JWT real
      return await authService.login(registerData.email, registerData.password);
    } catch (err) {
      console.error('Error during registration:', err);
      throw err;
    }
  },
};
