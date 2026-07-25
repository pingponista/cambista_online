import apiClient from './apiClient';

export const exchangeService = {
  getLiveRate: async (origin = 'USD', target = 'PEN') => {
    const response = await apiClient.get(`/rates?origin=${origin}&target=${target}`);
    return response.data;
  },

  createOrder: async (orderPayload) => {
    const response = await apiClient.post('/orders', orderPayload);
    return response.data;
  },

  getUserOrders: async () => {
    const response = await apiClient.get('/orders');
    return response.data;
  },

  confirmTransfer: async (orderId, transactionNumber) => {
    const response = await apiClient.post(`/orders/${orderId}/confirm-transfer`, { transactionNumber });
    return response.data;
  },

  getUserFxBreakdown: async () => {
    const response = await apiClient.get('/rates/breakdown');
    return response.data;
  },
};

