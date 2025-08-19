import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { api } from '../../Config/Api';

// Types
export interface Supplier {
  id: number;
  name: string;
  contactName: string;
  whatsapp: string;
  website?: string;
  email?: string;
  minimumOrderValue: number;
  deliveryTimeDays: number;
  adminFeePercentage: number;
  categories: string[];
  performanceRating: number;
  status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED' | 'PENDING_VERIFICATION';
  notes?: string;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
  updatedBy: string;
  catalogCount: number;
  activeOrdersCount: number;
  totalOrdersValue: number;
  isActive: boolean;
}

export interface SupplierRequest {
  name: string;
  contactName: string;
  whatsapp: string;
  website?: string;
  email?: string;
  minimumOrderValue: number;
  deliveryTimeDays: number;
  adminFeePercentage: number;
  categories: string[];
  performanceRating?: number;
  notes?: string;
}

export interface SupplierFilters {
  status?: string;
  category?: string;
  minRating?: number;
  maxMinimumValue?: number;
  maxDeliveryDays?: number;
  searchTerm?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: string;
}

interface SupplierState {
  suppliers: Supplier[];
  activeSuppliers: Supplier[];
  currentSupplier: Supplier | null;
  categories: string[];
  statistics: {
    totalActive: number;
    totalInactive: number;
    totalSuspended: number;
    totalPending: number;
    totalCategories: number;
    categories: string[];
  } | null;
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

const initialState: SupplierState = {
  suppliers: [],
  activeSuppliers: [],
  currentSupplier: null,
  categories: [],
  statistics: null,
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
export const fetchSuppliers = createAsyncThunk(
  'suppliers/fetchSuppliers',
  async (filters: SupplierFilters = {}) => {
    const params = new URLSearchParams();
    Object.entries(filters).forEach(([key, value]) => {
      if (value !== undefined && value !== null) {
        params.append(key, value.toString());
      }
    });

    const response = await api.get(`/api/suppliers?${params.toString()}`);
    return response.data;
  }
);

export const fetchActiveSuppliers = createAsyncThunk(
  'suppliers/fetchActiveSuppliers',
  async () => {
    const response = await api.get('/api/suppliers/active');
    return response.data;
  }
);

export const fetchSupplierById = createAsyncThunk(
  'suppliers/fetchSupplierById',
  async (id: number) => {
    const response = await api.get(`/api/suppliers/${id}`);
    return response.data;
  }
);

export const createSupplier = createAsyncThunk(
  'suppliers/createSupplier',
  async (supplierData: SupplierRequest) => {
    const response = await api.post('/api/suppliers', supplierData);
    return response.data;
  }
);

export const updateSupplier = createAsyncThunk(
  'suppliers/updateSupplier',
  async ({ id, data }: { id: number; data: SupplierRequest }) => {
    const response = await api.put(`/api/suppliers/${id}`, data);
    return response.data;
  }
);

export const deleteSupplier = createAsyncThunk(
  'suppliers/deleteSupplier',
  async (id: number) => {
    const response = await api.delete(`/api/suppliers/${id}`);
    return { ...response.data, deletedId: id };
  }
);

export const updateSupplierStatus = createAsyncThunk(
  'suppliers/updateSupplierStatus',
  async ({ id, status }: { id: number; status: 'activate' | 'deactivate' | 'suspend' }) => {
    const response = await api.patch(`/api/suppliers/${id}/${status}`);
    return response.data;
  }
);

export const fetchSupplierCategories = createAsyncThunk(
  'suppliers/fetchCategories',
  async () => {
    const response = await api.get('/api/suppliers/categories');
    return response.data;
  }
);

export const fetchSupplierStatistics = createAsyncThunk(
  'suppliers/fetchStatistics',
  async () => {
    const response = await api.get('/api/suppliers/statistics');
    return response.data;
  }
);

// Slice
const supplierSlice = createSlice({
  name: 'suppliers',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
    clearCurrentSupplier: (state) => {
      state.currentSupplier = null;
    },
    setCurrentPage: (state, action: PayloadAction<number>) => {
      state.pagination.currentPage = action.payload;
    },
  },
  extraReducers: (builder) => {
    builder
      // Fetch suppliers
      .addCase(fetchSuppliers.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchSuppliers.fulfilled, (state, action) => {
        state.loading = false;
        state.suppliers = action.payload.suppliers;
        state.pagination = {
          totalElements: action.payload.totalElements,
          totalPages: action.payload.totalPages,
          currentPage: action.payload.currentPage,
          pageSize: action.payload.pageSize,
          hasNext: action.payload.hasNext,
          hasPrevious: action.payload.hasPrevious,
        };
      })
      .addCase(fetchSuppliers.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Erro ao carregar fornecedores';
      })

      // Fetch active suppliers
      .addCase(fetchActiveSuppliers.fulfilled, (state, action) => {
        state.activeSuppliers = action.payload.suppliers;
      })

      // Fetch supplier by ID
      .addCase(fetchSupplierById.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchSupplierById.fulfilled, (state, action) => {
        state.loading = false;
        state.currentSupplier = action.payload.supplier;
      })
      .addCase(fetchSupplierById.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Erro ao carregar fornecedor';
      })

      // Create supplier
      .addCase(createSupplier.fulfilled, (state, action) => {
        state.suppliers.unshift(action.payload.supplier);
      })

      // Update supplier
      .addCase(updateSupplier.fulfilled, (state, action) => {
        const index = state.suppliers.findIndex(s => s.id === action.payload.supplier.id);
        if (index !== -1) {
          state.suppliers[index] = action.payload.supplier;
        }
        if (state.currentSupplier?.id === action.payload.supplier.id) {
          state.currentSupplier = action.payload.supplier;
        }
      })

      // Delete supplier
      .addCase(deleteSupplier.fulfilled, (state, action) => {
        state.suppliers = state.suppliers.filter(s => s.id !== action.payload.deletedId);
      })

      // Update supplier status
      .addCase(updateSupplierStatus.fulfilled, (state, action) => {
        const index = state.suppliers.findIndex(s => s.id === action.payload.supplier.id);
        if (index !== -1) {
          state.suppliers[index] = action.payload.supplier;
        }
        if (state.currentSupplier?.id === action.payload.supplier.id) {
          state.currentSupplier = action.payload.supplier;
        }
      })

      // Fetch categories
      .addCase(fetchSupplierCategories.fulfilled, (state, action) => {
        state.categories = action.payload.categories;
      })

      // Fetch statistics
      .addCase(fetchSupplierStatistics.fulfilled, (state, action) => {
        state.statistics = action.payload.statistics;
      });
  },
});

export const { clearError, clearCurrentSupplier, setCurrentPage } = supplierSlice.actions;
export default supplierSlice.reducer;
