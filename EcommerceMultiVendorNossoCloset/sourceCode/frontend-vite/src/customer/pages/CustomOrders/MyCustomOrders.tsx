import React, { useEffect, useState } from "react";
import {
  Box,
  Typography,
  Card,
  CardContent,
  CardMedia,
  Chip,
  Button,
  Grid,
  Pagination,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Fab,
  LinearProgress,
  Alert,
  Divider,
} from "@mui/material";
import {
  Add,
  Visibility,
  Cancel,
  CheckCircle,
  Schedule,
  LocalShipping,
  Payment,
} from "@mui/icons-material";
import { useAppDispatch, useAppSelector } from "../../../Redux Toolkit/Store";
import {
  fetchMyOrders,
  confirmOrder,
  cancelOrder,
  clearError,
  type CustomOrder,
} from "../../../Redux Toolkit/Customer/CustomOrderSlice";

const MyCustomOrders: React.FC = () => {
  const dispatch = useAppDispatch();
  const { myOrders, pagination, loading, error } = useAppSelector(
    (state) => state.customOrders
  );

  const [page, setPage] = useState(1);
  const [selectedOrder, setSelectedOrder] = useState<CustomOrder | null>(null);
  const [detailsDialogOpen, setDetailsDialogOpen] = useState(false);
  const [cancelDialogOpen, setCancelDialogOpen] = useState(false);
  const [cancelReason, setCancelReason] = useState("");

  useEffect(() => {
    dispatch(fetchMyOrders({ page: page - 1, size: 12 }));
  }, [dispatch, page]);

  useEffect(() => {
    if (error) {
      console.error("Custom orders error:", error);
      dispatch(clearError());
    }
  }, [error, dispatch]);

  const handlePageChange = (_: React.ChangeEvent<unknown>, value: number) => {
    setPage(value);
  };

  const handleConfirmOrder = async (orderId: number) => {
    try {
      await dispatch(confirmOrder(orderId)).unwrap();
      dispatch(fetchMyOrders({ page: page - 1, size: 12 }));
    } catch (error) {
      console.error("Error confirming order:", error);
    }
  };

  const handleCancelOrder = async () => {
    if (selectedOrder && cancelReason.trim()) {
      try {
        await dispatch(
          cancelOrder({ id: selectedOrder.id, reason: cancelReason })
        ).unwrap();
        dispatch(fetchMyOrders({ page: page - 1, size: 12 }));
        setCancelDialogOpen(false);
        setCancelReason("");
        setSelectedOrder(null);
      } catch (error) {
        console.error("Error canceling order:", error);
      }
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case "PENDING_ANALYSIS":
        return "warning";
      case "PRICED":
        return "info";
      case "CONFIRMED":
        return "primary";
      case "IN_COLLECTIVE_ORDER":
        return "secondary";
      case "PAID":
        return "success";
      case "IN_PRODUCTION":
        return "primary";
      case "DELIVERED":
        return "success";
      case "CANCELLED":
        return "error";
      default:
        return "default";
    }
  };

  const getStatusText = (status: string) => {
    switch (status) {
      case "PENDING_ANALYSIS":
        return "Aguardando Análise";
      case "PRICED":
        return "Precificado";
      case "CONFIRMED":
        return "Confirmado";
      case "IN_COLLECTIVE_ORDER":
        return "Em Pedido Coletivo";
      case "PAID":
        return "Pago";
      case "IN_PRODUCTION":
        return "Em Produção";
      case "DELIVERED":
        return "Entregue";
      case "CANCELLED":
        return "Cancelado";
      default:
        return status;
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case "PENDING_ANALYSIS":
        return <Schedule />;
      case "PRICED":
        return <Payment />;
      case "CONFIRMED":
        return <CheckCircle />;
      case "IN_COLLECTIVE_ORDER":
        return <Schedule />;
      case "PAID":
        return <Payment />;
      case "IN_PRODUCTION":
        return <Schedule />;
      case "DELIVERED":
        return <LocalShipping />;
      case "CANCELLED":
        return <Cancel />;
      default:
        return <Schedule />;
    }
  };

  const formatCurrency = (value?: number) => {
    if (!value) return "A definir";
    return new Intl.NumberFormat("pt-BR", {
      style: "currency",
      currency: "BRL",
    }).format(value);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("pt-BR", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const getProgressValue = (status: string) => {
    switch (status) {
      case "PENDING_ANALYSIS":
        return 10;
      case "PRICED":
        return 25;
      case "CONFIRMED":
        return 40;
      case "IN_COLLECTIVE_ORDER":
        return 60;
      case "PAID":
        return 75;
      case "IN_PRODUCTION":
        return 90;
      case "DELIVERED":
        return 100;
      case "CANCELLED":
        return 0;
      default:
        return 0;
    }
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
          Meus Pedidos Personalizados
        </Typography>
      </Box>

      {/* Error Alert */}
      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {/* Loading */}
      {loading && (
        <Box sx={{ width: "100%", mb: 3 }}>
          <LinearProgress />
        </Box>
      )}

      {/* Orders Grid */}
      <Grid container spacing={3}>
        {myOrders.length === 0 && !loading ? (
          <Grid item xs={12}>
            <Card sx={{ textAlign: "center", py: 6 }}>
              <CardContent>
                <Typography variant="h6" color="text.secondary" gutterBottom>
                  Você ainda não tem pedidos personalizados
                </Typography>
                <Typography
                  variant="body2"
                  color="text.secondary"
                  sx={{ mb: 3 }}
                >
                  Crie seu primeiro pedido personalizado enviando uma foto da
                  peça desejada
                </Typography>
                <Button
                  variant="contained"
                  startIcon={<Add />}
                  onClick={() => {
                    // TODO: Navigate to create order page
                  }}
                >
                  Criar Pedido
                </Button>
              </CardContent>
            </Card>
          </Grid>
        ) : (
          myOrders.map((order) => (
            <Grid item xs={12} sm={6} md={4} key={order.id}>
              <Card
                sx={{
                  height: "100%",
                  display: "flex",
                  flexDirection: "column",
                }}
              >
                <CardMedia
                  component="img"
                  height="200"
                  image={order.productImageUrl}
                  alt={order.description}
                  sx={{ objectFit: "cover" }}
                />

                <CardContent sx={{ flexGrow: 1 }}>
                  <Box
                    sx={{
                      display: "flex",
                      justifyContent: "space-between",
                      alignItems: "flex-start",
                      mb: 2,
                    }}
                  >
                    <Typography
                      variant="h6"
                      component="h3"
                      sx={{ flexGrow: 1, mr: 1 }}
                    >
                      Pedido #{order.id}
                    </Typography>
                    <Chip
                      icon={getStatusIcon(order.status)}
                      label={getStatusText(order.status)}
                      color={getStatusColor(order.status) as any}
                      size="small"
                    />
                  </Box>

                  <Typography
                    variant="body2"
                    color="text.secondary"
                    sx={{ mb: 2 }}
                  >
                    {order.description.length > 100
                      ? `${order.description.substring(0, 100)}...`
                      : order.description}
                  </Typography>

                  <Box sx={{ mb: 2 }}>
                    <Typography variant="caption" color="text.secondary">
                      Progresso do Pedido
                    </Typography>
                    <LinearProgress
                      variant="determinate"
                      value={getProgressValue(order.status)}
                      sx={{ mt: 0.5, height: 6, borderRadius: 3 }}
                    />
                  </Box>

                  <Box
                    sx={{
                      display: "flex",
                      justifyContent: "space-between",
                      mb: 2,
                    }}
                  >
                    <Box>
                      <Typography variant="caption" color="text.secondary">
                        Cor Preferencial
                      </Typography>
                      <Typography variant="body2">
                        {order.preferredColor}
                      </Typography>
                    </Box>
                    <Box>
                      <Typography variant="caption" color="text.secondary">
                        Tamanho
                      </Typography>
                      <Typography variant="body2">{order.size}</Typography>
                    </Box>
                    <Box>
                      <Typography variant="caption" color="text.secondary">
                        Quantidade
                      </Typography>
                      <Typography variant="body2">{order.quantity}</Typography>
                    </Box>
                  </Box>

                  <Divider sx={{ mb: 2 }} />

                  <Box
                    sx={{
                      display: "flex",
                      justifyContent: "space-between",
                      mb: 2,
                    }}
                  >
                    <Typography variant="body2" color="text.secondary">
                      Valor:
                    </Typography>
                    <Typography variant="body2" fontWeight="bold">
                      {formatCurrency(order.finalPrice || order.estimatedPrice)}
                    </Typography>
                  </Box>

                  <Typography variant="caption" color="text.secondary">
                    Criado em {formatDate(order.createdAt)}
                  </Typography>
                </CardContent>

                <Box sx={{ p: 2, pt: 0 }}>
                  <Box sx={{ display: "flex", gap: 1 }}>
                    <Button
                      size="small"
                      variant="outlined"
                      startIcon={<Visibility />}
                      onClick={() => {
                        setSelectedOrder(order);
                        setDetailsDialogOpen(true);
                      }}
                      fullWidth
                    >
                      Detalhes
                    </Button>

                    {order.status === "PRICED" && (
                      <Button
                        size="small"
                        variant="contained"
                        color="success"
                        onClick={() => handleConfirmOrder(order.id)}
                        fullWidth
                      >
                        Confirmar
                      </Button>
                    )}

                    {(order.status === "PENDING_ANALYSIS" ||
                      order.status === "PRICED") && (
                      <Button
                        size="small"
                        variant="outlined"
                        color="error"
                        onClick={() => {
                          setSelectedOrder(order);
                          setCancelDialogOpen(true);
                        }}
                        fullWidth
                      >
                        Cancelar
                      </Button>
                    )}
                  </Box>
                </Box>
              </Card>
            </Grid>
          ))
        )}
      </Grid>

      {/* Pagination */}
      {pagination.totalPages > 1 && (
        <Box sx={{ display: "flex", justifyContent: "center", mt: 4 }}>
          <Pagination
            count={pagination.totalPages}
            page={page}
            onChange={handlePageChange}
            color="primary"
            showFirstButton
            showLastButton
          />
        </Box>
      )}

      {/* Floating Action Button */}
      <Fab
        color="primary"
        aria-label="add"
        sx={{ position: "fixed", bottom: 16, right: 16 }}
        onClick={() => {
          // TODO: Navigate to create order page
        }}
      >
        <Add />
      </Fab>

      {/* Order Details Dialog */}
      <Dialog
        open={detailsDialogOpen}
        onClose={() => setDetailsDialogOpen(false)}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>Detalhes do Pedido #{selectedOrder?.id}</DialogTitle>
        <DialogContent>
          {selectedOrder && (
            <Box>
              <Grid container spacing={3}>
                <Grid item xs={12} md={6}>
                  <img
                    src={selectedOrder.productImageUrl}
                    alt="Produto"
                    style={{ width: "100%", borderRadius: 8 }}
                  />
                </Grid>
                <Grid item xs={12} md={6}>
                  <Box
                    sx={{ display: "flex", flexDirection: "column", gap: 2 }}
                  >
                    <Box>
                      <Typography variant="h6" gutterBottom>
                        Status
                      </Typography>
                      <Chip
                        icon={getStatusIcon(selectedOrder.status)}
                        label={getStatusText(selectedOrder.status)}
                        color={getStatusColor(selectedOrder.status) as any}
                      />
                    </Box>

                    <Box>
                      <Typography variant="h6" gutterBottom>
                        Descrição
                      </Typography>
                      <Typography variant="body2">
                        {selectedOrder.description}
                      </Typography>
                    </Box>

                    <Box>
                      <Typography variant="h6" gutterBottom>
                        Especificações
                      </Typography>
                      <Typography variant="body2">
                        <strong>Cor preferencial:</strong>{" "}
                        {selectedOrder.preferredColor}
                      </Typography>
                      <Typography variant="body2">
                        <strong>Tamanho:</strong> {selectedOrder.size}
                      </Typography>
                      <Typography variant="body2">
                        <strong>Categoria:</strong> {selectedOrder.category}
                      </Typography>
                      <Typography variant="body2">
                        <strong>Quantidade:</strong> {selectedOrder.quantity}
                      </Typography>
                      {selectedOrder.referenceCode && (
                        <Typography variant="body2">
                          <strong>Código de referência:</strong>{" "}
                          {selectedOrder.referenceCode}
                        </Typography>
                      )}
                    </Box>

                    {selectedOrder.alternativeColors.length > 0 && (
                      <Box>
                        <Typography variant="h6" gutterBottom>
                          Cores Alternativas
                        </Typography>
                        <Box sx={{ display: "flex", gap: 1, flexWrap: "wrap" }}>
                          {selectedOrder.alternativeColors.map(
                            (color, index) => (
                              <Chip
                                key={index}
                                label={color}
                                size="small"
                                variant="outlined"
                              />
                            )
                          )}
                        </Box>
                      </Box>
                    )}

                    <Box>
                      <Typography variant="h6" gutterBottom>
                        Valores
                      </Typography>
                      {selectedOrder.estimatedPrice && (
                        <Typography variant="body2">
                          <strong>Valor estimado:</strong>{" "}
                          {formatCurrency(selectedOrder.estimatedPrice)}
                        </Typography>
                      )}
                      {selectedOrder.finalPrice && (
                        <Typography variant="body2">
                          <strong>Valor final:</strong>{" "}
                          {formatCurrency(selectedOrder.finalPrice)}
                        </Typography>
                      )}
                    </Box>

                    {selectedOrder.observations && (
                      <Box>
                        <Typography variant="h6" gutterBottom>
                          Observações
                        </Typography>
                        <Typography variant="body2">
                          {selectedOrder.observations}
                        </Typography>
                      </Box>
                    )}

                    {selectedOrder.adminNotes && (
                      <Box>
                        <Typography variant="h6" gutterBottom>
                          Notas da Análise
                        </Typography>
                        <Typography variant="body2">
                          {selectedOrder.adminNotes}
                        </Typography>
                      </Box>
                    )}
                  </Box>
                </Grid>
              </Grid>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDetailsDialogOpen(false)}>Fechar</Button>
        </DialogActions>
      </Dialog>

      {/* Cancel Order Dialog */}
      <Dialog
        open={cancelDialogOpen}
        onClose={() => setCancelDialogOpen(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>Cancelar Pedido</DialogTitle>
        <DialogContent>
          <Typography variant="body2" sx={{ mb: 2 }}>
            Por favor, informe o motivo do cancelamento:
          </Typography>
          <TextField
            fullWidth
            multiline
            rows={3}
            value={cancelReason}
            onChange={(e) => setCancelReason(e.target.value)}
            placeholder="Motivo do cancelamento..."
            required
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCancelDialogOpen(false)}>Cancelar</Button>
          <Button
            onClick={handleCancelOrder}
            color="error"
            variant="contained"
            disabled={!cancelReason.trim()}
          >
            Confirmar Cancelamento
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default MyCustomOrders;
