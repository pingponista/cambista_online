import apiAuth from './apiAuth';

export const DEMO_CREDENTIALS = {
  email: 'demo@cambistaonline.pe',
  password: 'demo1234',
};

export const authService = {
  login: async (email, password) => {
    // Modo Demo Directo si se usan credenciales demo
    if (email === DEMO_CREDENTIALS.email && password === DEMO_CREDENTIALS.password) {
      return {
        token: 'demo-jwt-token-cambista-online-2026',
        userId: 'demo-user-101',
        email: DEMO_CREDENTIALS.email,
        fullNameOrCompany: 'Alexander (Usuario Demo)',
        profileType: 'NATURAL',
      };
    }

    try {
      // 1. Llamada REST al backend Spring Boot /api/v1/auth/login
      const tokenResponse = await apiAuth.login(email, password);
      const token = tokenResponse.accessToken;

      // Almacenamos temporalmente el token para la llamada /me
      localStorage.setItem('cambista_jwt_token', token);

      // 2. Llamada REST al backend Spring Boot /api/v1/auth/me para obtener datos del perfil
      let userProfile = null;
      try {
        const meResponse = await apiAuth.getMe();
        userProfile = meResponse.data;
      } catch (meErr) {
        console.warn('Could not fetch user profile from /me, using token data', meErr);
      }

      const name = userProfile
        ? (userProfile.firstName ? `${userProfile.firstName} ${userProfile.lastName}` : userProfile.companyName)
        : email;

      return {
        token: token,
        userId: userProfile ? userProfile.id : 'user-id',
        email: email,
        fullNameOrCompany: name || email,
        profileType: userProfile ? (userProfile.role === 'J' ? 'JURIDICA' : 'NATURAL') : 'NATURAL',
      };
    } catch (err) {
      // Fallback si el backend está sin conexión durante desarrollo
      if (!err.response) {
        return {
          token: 'demo-jwt-token-cambista-online-2026',
          userId: 'demo-user-101',
          email,
          fullNameOrCompany: 'Usuario Demo Cambista',
          profileType: 'NATURAL',
        };
      }
      throw err;
    }
  },

  register: async (registerData) => {
    try {
      // 1. Llamada REST al backend Spring Boot /api/v1/auth/register
      await apiAuth.register(registerData);

      // 2. Inicia sesión automáticamente tras registrarse
      return await authService.login(registerData.email, registerData.password);
    } catch (err) {
      // Fallback si el backend está sin conexión durante desarrollo
      if (!err.response) {
        const name = registerData.profileType === 'JURIDICA'
          ? (registerData.companyName || 'Empresa Demo SAC')
          : (`${registerData.firstName} ${registerData.lastName}` || 'Usuario Registrado');
        return {
          token: 'demo-jwt-token-cambista-online-2026',
          userId: 'demo-user-' + Date.now(),
          email: registerData.email,
          fullNameOrCompany: name,
          profileType: registerData.profileType || 'NATURAL',
        };
      }
      throw err;
    }
  },
};
