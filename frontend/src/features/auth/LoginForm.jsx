import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authService, DEMO_CREDENTIALS } from '../../services/authService';
import { useAuthStore } from '../../store/useAuthStore';
import { useFxStore } from '../../store/useFxStore';
import styles from './auth.module.css';

export const LoginForm = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const setAuth = useAuthStore((state) => state.setAuth);
  const navigate = useNavigate();

  const handleDemoLogin = async () => {
    setEmail(DEMO_CREDENTIALS.email);
    setPassword(DEMO_CREDENTIALS.password);
    try {
      setLoading(true);
      setError('');
      const data = await authService.login(DEMO_CREDENTIALS.email, DEMO_CREDENTIALS.password);
      setAuth(data);
      useFxStore.getState().fetchUserFxBreakdown();
      navigate('/');
    } catch (err) {
      setError('Error al ingresar con la cuenta Demo');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      setLoading(true);
      setError('');
      const data = await authService.login(email, password);
      setAuth(data);
      useFxStore.getState().fetchUserFxBreakdown();
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.message || 'Error al iniciar sesión');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={`glass-panel ${styles.authCard}`}>
      <h2 className={styles.authTitle}>Iniciar Sesión</h2>
      <p className={styles.authSubtitle}>Bienvenido a Cambista Online</p>

      {/* Demo Credentials Helper Box */}
      <div style={{ background: 'rgba(59, 130, 246, 0.12)', border: '1px solid rgba(59, 130, 246, 0.3)', padding: '0.85rem 1rem', borderRadius: 'var(--radius-sm, 8px)', marginBottom: '1.2rem', fontSize: '0.85rem' }}>
        <div style={{ fontWeight: 700, color: '#3b82f6', marginBottom: '0.2rem' }}>💡 Credenciales Demo (Acceso Rápido):</div>
        <div>Email: <strong>demo@cambistaonline.pe</strong></div>
        <div>Contraseña: <strong>demo1234</strong></div>
        <button
          type="button"
          onClick={handleDemoLogin}
          style={{ marginTop: '0.6rem', width: '100%', background: '#3b82f6', color: '#fff', padding: '0.45rem', borderRadius: '4px', fontWeight: 700, fontSize: '0.8rem', cursor: 'pointer' }}
        >
          ⚡ Ingresar directamente con Demo
        </button>
      </div>

      {error && (
        <div style={{ background: 'rgba(239, 68, 68, 0.15)', border: '1px solid #ef4444', color: '#fca5a5', padding: '0.8rem', borderRadius: 'var(--radius-sm)', marginBottom: '1rem', fontSize: '0.85rem' }}>
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit}>
        <div className={styles.formGroup}>
          <label className={styles.label}>Correo Electrónico</label>
          <input
            type="email"
            className={styles.input}
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </div>

        <div className={styles.formGroup}>
          <label className={styles.label}>Contraseña</label>
          <input
            type="password"
            className={styles.input}
            required
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
        </div>

        <button type="submit" className={styles.btnSubmit} disabled={loading}>
          {loading ? 'Ingresando...' : 'Ingresar'}
        </button>
      </form>

      <p style={{ marginTop: '1.5rem', textAlign: 'center', fontSize: '0.85rem', color: 'var(--text-muted)' }}>
        ¿No tienes una cuenta? <Link to="/register" style={{ color: 'var(--primary-500)', fontWeight: '600' }}>Regístrate gratis</Link>
      </p>
    </div>
  );
};

