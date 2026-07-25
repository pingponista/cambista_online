import apiClient from './apiClient';

/**
 * Servicio API de Autenticación para el Frontend.
 * Ciego a la arquitectura del backend (Onion Architecture).
 * Realiza peticiones HTTP REST estandarizadas hacia /api/v1/auth.
 */
export const apiAuth = {
  /**
   * Registro de usuario (Persona Natural / Persona Jurídica)
   * POST /api/v1/auth/register
   */
  register: async (payload) => {
    const response = await apiClient.post('/auth/register', payload);
    return response.data;
  },

  /**
   * Autenticación de usuario
   * POST /api/v1/auth/login
   */
  login: async (email, password) => {
    const response = await apiClient.post('/auth/login', { email, password });
    return response.data; // { accessToken, tokenType: "Bearer", expiresIn: 3600 }
  },

  /**
   * Obtener perfil del usuario autenticado mediante JWT
   * GET /api/v1/auth/me
   */
  getMe: async () => {
    const response = await apiClient.get('/auth/me');
    return response.data; // ApiResponse<UserProfileResponse>
  },
};

export default apiAuth;
