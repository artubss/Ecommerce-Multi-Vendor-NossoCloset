import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { api } from '../../Config/Api';

// Types
export interface CustomOrder {
  id: number;
  client: {
    id: number;
    fullName: string;
    email: string;
    whatsapp: string;
  };
  supplier?: {
    id: number;
    name: string;
    contactName: string;
    deliveryTimeDays: number;
    adminFeePercentage: number;
  };
  collectiveOrder?: {
    id: number;
    minimumValue: number;
    currentValue: number;
    paymentDeadline: string;
  };
  productImageUrl: string;
  referenceCode?: string;
  description: string;
  preferredColor: string;
  alternativeColors: string[];
  size: string;
  category: string;
  observations?: string;
  estimatedPrice?: number;
  finalPrice?: number;
  status: 'PENDING_ANALYSIS' | 'PRICED' | 'CONFIRMED' | 'IN_COLLECTIVE_ORDER' | 'PAID' | 'IN_PRODUCTION' | 'DELIVERED' | 'CANCELLED';
  urgency: 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT';
  quantity: number;
  adminNotes?: string;
  analyzedBy?: string;
  cancellationReason?: string;
  createdAt: string;
  updatedAt: string;
  analyzedAt?: string;
  confirmedAt?: string;
  cancelledAt?: string;
  deliveredAt?: string;
  createdBy: string;
  updatedBy: string;
  totalValue: number;
  daysOld: number;
  isUrgent: boolean;
  isPending: boolean;
  isConfirmed: boolean;
  isCancelled: boolean;
  isDelivered: boolean;
}

export interface CustomOrderRequest {
  clientId: number;
  productImageUrl: string;
  referenceCode?: string;
  description: string;
  preferredColor: string;
  alternativeColors?: string[];
  size: string;
  category: string;
  observations?: string;
  estimatedPrice?: number;
  urgency?: 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT';
  quantity?: number;
}

export interface CustomOrderFilters {
  status?: string;
  supplierId?: number;
  urgency?: string;
  category?: string;
  minValue?: number;
  maxValue?: number;
  startDate?: string;
  endDate?: string;
  searchTerm?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: string;
}

interface CustomOrderState {
  orders: CustomOrder[];
  myOrders: CustomOrder[];
  currentOrder: CustomOrder | null;
  pendingAnalysis: CustomOrder[];
  urgentOrders: CustomOrder[];
  pagination: {
    totalElements: number;
    totalPages: number;
    currentPage: number;
    pageSize: number;
    hasNext: boolean;
    hasPrevious: boolean;
  };
  loading: boolean;
  error: string | null;
}

const initialState: CustomOrderState = {
  orders: [],
  myOrders: [],
  currentOrder: null,
  pendingAnalysis: [],
  urgentOrders: [],
  pagination: {
    totalElements: 0,
    totalPages: 0,
    currentPage: 0,
    pageSize: 20,
    hasNext: false,
    hasPrevious: false,
  },
  loading: false,
  error: null,
};

// Async Thunks
export const fetchCustomOrders = createAsyncThunk(
  'customOrders/fetchCustomOrders',
  async (filters: CustomOrderFilters = {}) => {
    const params = new URLSearchParams();
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== undefined && value !== null) {
        params.append(key, value.toString());
      }
    });

    const response = await api.get(`/api/custom-orders?${params.toString()}`);
    return response.data;
  }
);

export const fetchMyOrders = createAsyncThunk(
  'customOrders/fetchMyOrders',
  async (params: { page?: number; size?: number } = {}) => {
    const queryParams = new URLSearchParams();
    if (params.page !== undefined) queryParams.append('page', params.page.toString());
    if (params.size !== undefined) queryParams.append('size', params.size.toString());

    const response = await api.get(`/api/custom-orders/my-orders?${queryParams.toString()}`);
    return response.data;
  }
);

export const fetchCustomOrderById = createAsyncThunk(
  'customOrders/fetchCustomOrderById',
  async (id: number) => {
    const response = await api.get(`/api/custom-orders/${id}`);
    return response.data;
  }
);

export const createCustomOrder = createAsyncThunk(
  'customOrders/createCustomOrder',
  async (orderData: CustomOrderRequest) => {
    const response = await api.post('/api/custom-orders', orderData);
    return response.data;
  }
);

export const updateCustomOrder = createAsyncThunk(
  'customOrders/updateCustomOrder',
  async ({ id, data }: { id: number; data: CustomOrderRequest }) => {
    const response = await api.put(`/api/custom-orders/${id}`, data);
    return response.data;
  }
);

export const analyzeOrder = createAsyncThunk(
  'customOrders/analyzeOrder',
  async ({ 
    id, 
    supplierId, 
    finalPrice, 
    adminNotes 
  }: { 
    id: number; 
    supplierId: number; 
    finalPrice: number; 
    adminNotes?: string; 
  }) => {
    const params = new URLSearchParams();
    params.append('supplierId', supplierId.toString());
    params.append('finalPrice', finalPrice.toString());
    if (adminNotes) params.append('adminNotes', adminNotes);

    const response = await api.post(`/api/custom-orders/${id}/analyze?${params.toString()}`);
    return response.data;
  }
);

export const confirmOrder = createAsyncThunk(
  'customOrders/confirmOrder',
  async (id: number) => {
    const response = await api.post(`/api/custom-orders/${id}/confirm`);
    return response.data;
  }
);

export const cancelOrder = createAsyncThunk(
  'customOrders/cancelOrder',
  async ({ id, reason }: { id: number; reason: string }) => {
    const params = new URLSearchParams();
    params.append('reason', reason);

    const response = await api.post(`/api/custom-orders/${id}/cancel?${params.toString()}`);
    return response.data;
  }
);

export const fetchPendingAnalysis = createAsyncThunk(
  'customOrders/fetchPendingAnalysis',
  async () => {
    const response = await api.get('/api/custom-orders/pending-analysis');
    return response.data;
  }
);

export const fetchUrgentOrders = createAsyncThunk(
  'customOrders/fetchUrgentOrders',
  async () => {
    const response = await api.get('/api/custom-orders/urgent');
    return response.data;
  }
);

// Slice
const customOrderSlice = createSlice({
  name: 'customOrders',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
    clearCurrentOrder: (state) => {
      state.currentOrder = null;
    },
    setCurrentPage: (state, action: PayloadAction<number>) => {
      state.pagination.currentPage = action.payload;
    },
  },
  extraReducers: (builder) => {
    builder
      // Fetch custom orders
      .addCase(fetchCustomOrders.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchCustomOrders.fulfilled, (state, action) => {
        state.loading = false;
        state.orders = action.payload.orders;
        state.pagination = {
          totalElements: action.payload.totalElements,
          totalPages: action.payload.totalPages,
          currentPage: action.payload.currentPage,
          pageSize: action.payload.pageSize,
          hasNext: action.payload.hasNext,
          hasPrevious: action.payload.hasPrevious,
        };
      })
      .addCase(fetchCustomOrders.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Erro ao carregar pedidos';
      })

      // Fetch my orders
      .addCase(fetchMyOrders.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchMyOrders.fulfilled, (state, action) => {
        state.loading = false;
        state.myOrders = action.payload.orders;
        state.pagination = {
          totalElements: action.payload.totalElements,
          totalPages: action.payload.totalPages,
          currentPage: action.payload.currentPage,
          pageSize: action.payload.pageSize,
          hasNext: action.payload.hasNext || false,
          hasPrevious: action.payload.hasPrevious || false,
        };
      })
      .addCase(fetchMyOrders.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Erro ao carregar seus pedidos';
      })

      // Fetch order by ID
      .addCase(fetchCustomOrderById.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchCustomOrderById.fulfilled, (state, action) => {
        state.loading = false;
        state.currentOrder = action.payload.order;
      })
      .addCase(fetchCustomOrderById.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Erro ao carregar pedido';
      })

      // Create custom order
      .addCase(createCustomOrder.fulfilled, (state, action) => {
        state.orders.unshift(action.payload.order);
        state.myOrders.unshift(action.payload.order);
      })

      // Update custom order
      .addCase(updateCustomOrder.fulfilled, (state, action) => {
        const updateOrder = (orders: CustomOrder[]) => {
          const index = orders.findIndex(o => o.id === action.payload.order.id);
          if (index !== -1) {
            orders[index] = action.payload.order;
          }
        };

        updateOrder(state.orders);
        updateOrder(state.myOrders);
        
        if (state.currentOrder?.id === action.payload.order.id) {
          state.currentOrder = action.payload.order;
        }
      })

      // Analyze order
      .addCase(analyzeOrder.fulfilled, (state, action) => {
        const updateOrder = (orders: CustomOrder[]) => {
          const index = orders.findIndex(o => o.id === action.payload.order.id);
          if (index !== -1) {
            orders[index] = action.payload.order;
          }
        };

        updateOrder(state.orders);
        updateOrder(state.myOrders);
        
        if (state.currentOrder?.id === action.payload.order.id) {
          state.currentOrder = action.payload.order;
        }

        // Remove from pending analysis if it was there
        state.pendingAnalysis = state.pendingAnalysis.filter(o => o.id !== action.payload.order.id);
      })

      // Confirm order
      .addCase(confirmOrder.fulfilled, (state, action) => {
        const updateOrder = (orders: CustomOrder[]) => {
          const index = orders.findIndex(o => o.id === action.payload.order.id);
          if (index !== -1) {
            orders[index] = action.payload.order;
          }
        };

        updateOrder(state.orders);
        updateOrder(state.myOrders);
        
        if (state.currentOrder?.id === action.payload.order.id) {
          state.currentOrder = action.payload.order;
        }
      })

      // Cancel order
      .addCase(cancelOrder.fulfilled, (state, action) => {
        const updateOrder = (orders: CustomOrder[]) => {
          const index = orders.findIndex(o => o.id === action.payload.order.id);
          if (index !== -1) {
            orders[index] = action.payload.order;
          }
        };

        updateOrder(state.orders);
        updateOrder(state.myOrders);
        
        if (state.currentOrder?.id === action.payload.order.id) {
          state.currentOrder = action.payload.order;
        }

        // Remove from pending and urgent lists
        state.pendingAnalysis = state.pendingAnalysis.filter(o => o.id !== action.payload.order.id);
        state.urgentOrders = state.urgentOrders.filter(o => o.id !== action.payload.order.id);
      })

      // Fetch pending analysis
      .addCase(fetchPendingAnalysis.fulfilled, (state, action) => {
        state.pendingAnalysis = action.payload.orders;
      })

      // Fetch urgent orders
      .addCase(fetchUrgentOrders.fulfilled, (state, action) => {
        state.urgentOrders = action.payload.orders;
      });
  },
});

export const { clearError, clearCurrentOrder, setCurrentPage } = customOrderSlice.actions;
export default customOrderSlice.reducer;
