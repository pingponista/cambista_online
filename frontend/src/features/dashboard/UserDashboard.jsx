import React, { useState } from 'react';
import { useAuthStore } from '../../store/useAuthStore';
import { Card } from '../../components/ui/Card/Card';
import { Button } from '../../components/ui/Button/Button';
import { BankAccountsManager } from './BankAccountsManager';
import { RefreshCw, History, CreditCard, User, PlusCircle } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import styles from './dashboard.module.css';

export const UserDashboard = () => {
  const { user } = useAuthStore();
  const [activeTab, setActiveTab] = useState('historial');
  const [filterStatus, setFilterStatus] = useState('ALL');
  const navigate = useNavigate();

  const [orders] = useState([
    { id: 'ORD-891293', type: 'BUY', origin: 'USD 1,000.00', target: 'PEN 3,735.00', rate: '3.7350', status: 'COMPLETED', date: '2026-07-22' },
    { id: 'ORD-771291', type: 'SELL', origin: 'PEN 2,000.00', target: 'USD 531.21', rate: '3.7650', status: 'PENDING', date: '2026-07-21' },
  ]);

  const filteredOrders = filterStatus === 'ALL'
    ? orders
    : orders.filter((o) => o.status === filterStatus);

  return (
    <div className={`container ${styles.dashboardGrid}`}>
      {/* Sidebar Navigation */}
      <aside className={styles.sidebar}>
        <div
          className={`${styles.navItem} ${activeTab === 'operar' ? styles.navActive : ''}`}
          onClick={() => navigate('/')}
        >
          <RefreshCw size={18} /> Nueva Operación
        </div>

        <div
          className={`${styles.navItem} ${activeTab === 'historial' ? styles.navActive : ''}`}
          onClick={() => setActiveTab('historial')}
        >
          <History size={18} /> Historial
        </div>

        <div
          className={`${styles.navItem} ${activeTab === 'cuentas' ? styles.navActive : ''}`}
          onClick={() => setActiveTab('cuentas')}
        >
          <CreditCard size={18} /> Mis Cuentas
        </div>

        <div
          className={`${styles.navItem} ${activeTab === 'perfil' ? styles.navActive : ''}`}
          onClick={() => setActiveTab('perfil')}
        >
          <User size={18} /> Mi Perfil
        </div>
      </aside>

      {/* Main Content Area */}
      <main>
        <div className={styles.profileCard}>
          <div>
            <h2 style={{ fontSize: '1.4rem', fontWeight: 800 }}>{user?.name || 'Hola, bienvenido'}</h2>
            <span style={{ fontSize: '0.85rem', color: 'var(--color-accent-green-hover)', fontWeight: 700 }}>
              Perfil {user?.profileType === 'JURIDICA' ? 'Empresarial (Persona Jurídica)' : 'Persona Natural'}
            </span>
          </div>
          <Button variant="primary" size="md" onClick={() => navigate('/')}>
            <PlusCircle size={16} /> Operar ahora
          </Button>
        </div>

        {activeTab === 'historial' && (
          <Card>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.2rem' }}>
              <h3 style={{ fontSize: '1.2rem', fontWeight: 800 }}>Historial de Operaciones</h3>

              <div className={styles.tableFilters}>
                <select
                  value={filterStatus}
                  onChange={(e) => setFilterStatus(e.target.value)}
                  className={styles.filterSelect}
                >
                  <option value="ALL">Todos los estados</option>
                  <option value="COMPLETED">Completadas</option>
                  <option value="PENDING">En proceso</option>
                </select>
              </div>
            </div>

            <table className={styles.table}>
              <thead>
                <tr>
                  <th>N° Orden</th>
                  <th>Operación</th>
                  <th>Monto Enviado</th>
                  <th>Monto Recibido</th>
                  <th>Tasa</th>
                  <th>Estado</th>
                  <th>Fecha</th>
                </tr>
              </thead>
              <tbody>
                {filteredOrders.map((ord) => (
                  <tr key={ord.id}>
                    <td><strong className="tabular-nums">{ord.id}</strong></td>
                    <td>{ord.type === 'BUY' ? 'Compra Dólar' : 'Venta Dólar'}</td>
                    <td className="tabular-nums">{ord.origin}</td>
                    <td className="tabular-nums" style={{ color: 'var(--color-accent-green-hover)', fontWeight: 700 }}>{ord.target}</td>
                    <td className="tabular-nums">{ord.rate}</td>
                    <td>
                      <span className={ord.status === 'COMPLETED' ? styles.statusCompleted : styles.statusPending}>
                        {ord.status === 'COMPLETED' ? 'Completada' : 'En Verificación'}
                      </span>
                    </td>
                    <td className="tabular-nums">{ord.date}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </Card>
        )}

        {activeTab === 'cuentas' && <BankAccountsManager />}

        {activeTab === 'perfil' && (
          <Card>
            <h3 style={{ fontSize: '1.2rem', fontWeight: 800, marginBottom: '1rem' }}>Información de mi Perfil</h3>
            <div style={{ lineHeight: '1.8', fontSize: '0.95rem' }}>
              <div><strong>Email:</strong> {user?.email || 'usuario@cambistaonline.pe'}</div>
              <div><strong>Tipo de Perfil:</strong> {user?.profileType || 'Persona Natural'}</div>
              <div><strong>Estado de Identidad:</strong> <span style={{ color: 'var(--color-accent-green-hover)', fontWeight: 700 }}>Verificado por la SBS</span></div>
            </div>
          </Card>
        )}
      </main>
    </div>
  );
};
