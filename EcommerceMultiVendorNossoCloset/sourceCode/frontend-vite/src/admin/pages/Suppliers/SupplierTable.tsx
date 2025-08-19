import React, { useEffect, useState } from "react";
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Chip,
  IconButton,
  Button,
  TextField,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Box,
  Typography,
  Pagination,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Menu,
  ListItemIcon,
  ListItemText,
} from "@mui/material";
import {
  Edit,
  Delete,
  MoreVert,
  Add,
  Search,
  FilterList,
  CheckCircle,
  Cancel,
  Pause,
  Visibility,
  Phone,
  Email,
  Language,
} from "@mui/icons-material";
import { useAppDispatch, useAppSelector } from "../../../Redux Toolkit/Store";
import {
  fetchSuppliers,
  updateSupplierStatus,
  deleteSupplier,
  clearError,
  setCurrentPage,
  type SupplierFilters,
} from "../../../Redux Toolkit/Admin/SupplierSlice";

const SupplierTable: React.FC = () => {
  const dispatch = useAppDispatch();
  const { suppliers, pagination, loading, error } = useAppSelector(
    (state) => state.suppliers
  );

  const [filters, setFilters] = useState<SupplierFilters>({
    page: 0,
    size: 20,
    sortBy: "name",
    sortDir: "asc",
  });
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState("");
  const [categoryFilter, setCategoryFilter] = useState("");
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [selectedSupplier, setSelectedSupplier] = useState<number | null>(null);
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [menuSupplierId, setMenuSupplierId] = useState<number | null>(null);

  useEffect(() => {
    dispatch(fetchSuppliers(filters));
  }, [dispatch, filters]);

  useEffect(() => {
    if (error) {
      console.error("Supplier error:", error);
      dispatch(clearError());
    }
  }, [error, dispatch]);

  const handleSearch = () => {
    setFilters((prev) => ({
      ...prev,
      searchTerm: searchTerm.trim() || undefined,
      page: 0,
    }));
  };

  const handleFilterChange = (field: keyof SupplierFilters, value: any) => {
    setFilters((prev) => ({
      ...prev,
      [field]: value || undefined,
      page: 0,
    }));
  };

  const handlePageChange = (_: React.ChangeEvent<unknown>, value: number) => {
    const newPage = value - 1;
    setFilters((prev) => ({ ...prev, page: newPage }));
    dispatch(setCurrentPage(newPage));
  };

  const handleStatusUpdate = async (
    id: number,
    action: "activate" | "deactivate" | "suspend"
  ) => {
    try {
      await dispatch(updateSupplierStatus({ id, status: action })).unwrap();
      dispatch(fetchSuppliers(filters));
    } catch (error) {
      console.error("Error updating supplier status:", error);
    }
    handleMenuClose();
  };

  const handleDelete = async () => {
    if (selectedSupplier) {
      try {
        await dispatch(deleteSupplier(selectedSupplier)).unwrap();
        dispatch(fetchSuppliers(filters));
      } catch (error) {
        console.error("Error deleting supplier:", error);
      }
    }
    setDeleteDialogOpen(false);
    setSelectedSupplier(null);
  };

  const handleMenuOpen = (
    event: React.MouseEvent<HTMLElement>,
    supplierId: number
  ) => {
    setAnchorEl(event.currentTarget);
    setMenuSupplierId(supplierId);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
    setMenuSupplierId(null);
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case "ACTIVE":
        return "success";
      case "INACTIVE":
        return "default";
      case "SUSPENDED":
        return "error";
      case "PENDING_VERIFICATION":
        return "warning";
      default:
        return "default";
    }
  };

  const getStatusText = (status: string) => {
    switch (status) {
      case "ACTIVE":
        return "Ativo";
      case "INACTIVE":
        return "Inativo";
      case "SUSPENDED":
        return "Suspenso";
      case "PENDING_VERIFICATION":
        return "Pendente";
      default:
        return status;
    }
  };

  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat("pt-BR", {
      style: "currency",
      currency: "BRL",
    }).format(value);
  };

  return (
    <Box sx={{ p: 3 }}>
      {/* Header */}
      <Box
        sx={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          mb: 3,
        }}
      >
        <Typography variant="h4" component="h1">
          Fornecedores
        </Typography>
        <Button
          variant="contained"
          startIcon={<Add />}
          onClick={() => {
            // TODO: Open create supplier dialog
          }}
        >
          Novo Fornecedor
        </Button>
      </Box>

      {/* Filters */}
      <Paper sx={{ p: 2, mb: 3 }}>
        <Box
          sx={{
            display: "flex",
            gap: 2,
            flexWrap: "wrap",
            alignItems: "center",
          }}
        >
          <TextField
            size="small"
            placeholder="Buscar fornecedores..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            onKeyPress={(e) => e.key === "Enter" && handleSearch()}
            InputProps={{
              startAdornment: (
                <Search sx={{ mr: 1, color: "text.secondary" }} />
              ),
            }}
            sx={{ minWidth: 250 }}
          />

          <FormControl size="small" sx={{ minWidth: 150 }}>
            <InputLabel>Status</InputLabel>
            <Select
              value={statusFilter}
              label="Status"
              onChange={(e) => {
                setStatusFilter(e.target.value);
                handleFilterChange("status", e.target.value);
              }}
            >
              <MenuItem value="">Todos</MenuItem>
              <MenuItem value="ACTIVE">Ativo</MenuItem>
              <MenuItem value="INACTIVE">Inativo</MenuItem>
              <MenuItem value="SUSPENDED">Suspenso</MenuItem>
              <MenuItem value="PENDING_VERIFICATION">Pendente</MenuItem>
            </Select>
          </FormControl>

          <TextField
            size="small"
            placeholder="Categoria"
            value={categoryFilter}
            onChange={(e) => {
              setCategoryFilter(e.target.value);
              handleFilterChange("category", e.target.value);
            }}
            sx={{ minWidth: 150 }}
          />

          <Button
            variant="outlined"
            startIcon={<Search />}
            onClick={handleSearch}
          >
            Buscar
          </Button>

          <Button
            variant="outlined"
            startIcon={<FilterList />}
            onClick={() => {
              setSearchTerm("");
              setStatusFilter("");
              setCategoryFilter("");
              setFilters({
                page: 0,
                size: 20,
                sortBy: "name",
                sortDir: "asc",
              });
            }}
          >
            Limpar
          </Button>
        </Box>
      </Paper>

      {/* Table */}
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Nome</TableCell>
              <TableCell>Contato</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Categorias</TableCell>
              <TableCell>Valor Mínimo</TableCell>
              <TableCell>Prazo Entrega</TableCell>
              <TableCell>Taxa Admin</TableCell>
              <TableCell>Rating</TableCell>
              <TableCell>Catálogos</TableCell>
              <TableCell align="center">Ações</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={10} align="center">
                  Carregando...
                </TableCell>
              </TableRow>
            ) : suppliers.length === 0 ? (
              <TableRow>
                <TableCell colSpan={10} align="center">
                  Nenhum fornecedor encontrado
                </TableCell>
              </TableRow>
            ) : (
              suppliers.map((supplier) => (
                <TableRow key={supplier.id} hover>
                  <TableCell>
                    <Box>
                      <Typography variant="subtitle2" fontWeight="bold">
                        {supplier.name}
                      </Typography>
                      <Box sx={{ display: "flex", gap: 1, mt: 0.5 }}>
                        {supplier.whatsapp && (
                          <IconButton size="small" color="success">
                            <Phone fontSize="small" />
                          </IconButton>
                        )}
                        {supplier.email && (
                          <IconButton size="small" color="primary">
                            <Email fontSize="small" />
                          </IconButton>
                        )}
                        {supplier.website && (
                          <IconButton size="small" color="info">
                            <Language fontSize="small" />
                          </IconButton>
                        )}
                      </Box>
                    </Box>
                  </TableCell>

                  <TableCell>
                    <Typography variant="body2">
                      {supplier.contactName}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {supplier.whatsapp}
                    </Typography>
                  </TableCell>

                  <TableCell>
                    <Chip
                      label={getStatusText(supplier.status)}
                      color={getStatusColor(supplier.status) as any}
                      size="small"
                    />
                  </TableCell>

                  <TableCell>
                    <Box sx={{ display: "flex", flexWrap: "wrap", gap: 0.5 }}>
                      {supplier.categories
                        .slice(0, 2)
                        .map((category, index) => (
                          <Chip
                            key={index}
                            label={category}
                            size="small"
                            variant="outlined"
                          />
                        ))}
                      {supplier.categories.length > 2 && (
                        <Chip
                          label={`+${supplier.categories.length - 2}`}
                          size="small"
                          variant="outlined"
                        />
                      )}
                    </Box>
                  </TableCell>

                  <TableCell>
                    {formatCurrency(supplier.minimumOrderValue)}
                  </TableCell>

                  <TableCell>{supplier.deliveryTimeDays} dias</TableCell>

                  <TableCell>{supplier.adminFeePercentage}%</TableCell>

                  <TableCell>
                    <Box
                      sx={{ display: "flex", alignItems: "center", gap: 0.5 }}
                    >
                      <Typography variant="body2">
                        {supplier.performanceRating}/5
                      </Typography>
                      <Box
                        sx={{
                          width: 40,
                          height: 4,
                          bgcolor: "grey.300",
                          borderRadius: 2,
                          overflow: "hidden",
                        }}
                      >
                        <Box
                          sx={{
                            width: `${(supplier.performanceRating / 5) * 100}%`,
                            height: "100%",
                            bgcolor:
                              supplier.performanceRating >= 4
                                ? "success.main"
                                : supplier.performanceRating >= 3
                                ? "warning.main"
                                : "error.main",
                          }}
                        />
                      </Box>
                    </Box>
                  </TableCell>

                  <TableCell>
                    <Typography variant="body2">
                      {supplier.catalogCount}
                    </Typography>
                  </TableCell>

                  <TableCell align="center">
                    <IconButton
                      onClick={(e) => handleMenuOpen(e, supplier.id)}
                      size="small"
                    >
                      <MoreVert />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Pagination */}
      {pagination.totalPages > 1 && (
        <Box sx={{ display: "flex", justifyContent: "center", mt: 3 }}>
          <Pagination
            count={pagination.totalPages}
            page={pagination.currentPage + 1}
            onChange={handlePageChange}
            color="primary"
            showFirstButton
            showLastButton
          />
        </Box>
      )}

      {/* Action Menu */}
      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={handleMenuClose}
      >
        <MenuItem onClick={() => console.log("View supplier", menuSupplierId)}>
          <ListItemIcon>
            <Visibility fontSize="small" />
          </ListItemIcon>
          <ListItemText>Visualizar</ListItemText>
        </MenuItem>

        <MenuItem onClick={() => console.log("Edit supplier", menuSupplierId)}>
          <ListItemIcon>
            <Edit fontSize="small" />
          </ListItemIcon>
          <ListItemText>Editar</ListItemText>
        </MenuItem>

        {menuSupplierId && (
          <>
            <MenuItem
              onClick={() => handleStatusUpdate(menuSupplierId, "activate")}
            >
              <ListItemIcon>
                <CheckCircle fontSize="small" color="success" />
              </ListItemIcon>
              <ListItemText>Ativar</ListItemText>
            </MenuItem>

            <MenuItem
              onClick={() => handleStatusUpdate(menuSupplierId, "deactivate")}
            >
              <ListItemIcon>
                <Pause fontSize="small" color="warning" />
              </ListItemIcon>
              <ListItemText>Desativar</ListItemText>
            </MenuItem>

            <MenuItem
              onClick={() => handleStatusUpdate(menuSupplierId, "suspend")}
            >
              <ListItemIcon>
                <Cancel fontSize="small" color="error" />
              </ListItemIcon>
              <ListItemText>Suspender</ListItemText>
            </MenuItem>
          </>
        )}

        <MenuItem
          onClick={() => {
            setSelectedSupplier(menuSupplierId);
            setDeleteDialogOpen(true);
            handleMenuClose();
          }}
          sx={{ color: "error.main" }}
        >
          <ListItemIcon>
            <Delete fontSize="small" color="error" />
          </ListItemIcon>
          <ListItemText>Excluir</ListItemText>
        </MenuItem>
      </Menu>

      {/* Delete Confirmation Dialog */}
      <Dialog
        open={deleteDialogOpen}
        onClose={() => setDeleteDialogOpen(false)}
      >
        <DialogTitle>Confirmar Exclusão</DialogTitle>
        <DialogContent>
          <Typography>
            Tem certeza que deseja excluir este fornecedor? Esta ação não pode
            ser desfeita.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)}>Cancelar</Button>
          <Button onClick={handleDelete} color="error" variant="contained">
            Excluir
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default SupplierTable;
