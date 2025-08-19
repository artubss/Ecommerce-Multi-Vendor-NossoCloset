import React, { useState } from "react";
import {
  Box,
  Typography,
  Card,
  CardContent,
  TextField,
  Button,
  Grid,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Chip,
  Alert,
  Paper,
  IconButton,
  LinearProgress,
} from "@mui/material";
import { CloudUpload, Add, Delete, Send, ArrowBack } from "@mui/icons-material";
import { Formik, Form, Field, FieldArray } from "formik";
import * as Yup from "yup";
import { useAppDispatch, useAppSelector } from "../../../Redux Toolkit/Store";
import {
  createCustomOrder,
  type CustomOrderRequest,
} from "../../../Redux Toolkit/Customer/CustomOrderSlice";

const validationSchema = Yup.object({
  productImageUrl: Yup.string().required("Imagem do produto é obrigatória"),
  description: Yup.string()
    .min(10, "Descrição deve ter pelo menos 10 caracteres")
    .max(1000, "Descrição deve ter no máximo 1000 caracteres")
    .required("Descrição é obrigatória"),
  preferredColor: Yup.string().required("Cor preferencial é obrigatória"),
  size: Yup.string().required("Tamanho é obrigatório"),
  category: Yup.string().required("Categoria é obrigatória"),
  quantity: Yup.number()
    .min(1, "Quantidade deve ser pelo menos 1")
    .max(10, "Quantidade máxima é 10")
    .required("Quantidade é obrigatória"),
});

const categories = [
  "Vestidos",
  "Blusas",
  "Calças",
  "Saias",
  "Jaquetas",
  "Acessórios",
  "Calçados",
  "Lingerie",
  "Esportivo",
  "Outros",
];

const sizes = [
  "PP",
  "P",
  "M",
  "G",
  "GG",
  "XGG",
  "34",
  "36",
  "38",
  "40",
  "42",
  "44",
  "46",
  "48",
];

const urgencyLevels = [
  { value: "LOW", label: "Baixa" },
  { value: "NORMAL", label: "Normal" },
  { value: "HIGH", label: "Alta" },
  { value: "URGENT", label: "Urgente" },
];

const CreateCustomOrder: React.FC = () => {
  const dispatch = useAppDispatch();
  const { loading, error } = useAppSelector((state) => state.customOrders);
  const { user } = useAppSelector((state) => state.auth);

  const [imagePreview, setImagePreview] = useState<string | null>(null);
  const [uploading, setUploading] = useState(false);

  const handleImageUpload = async (file: File, setFieldValue: any) => {
    setUploading(true);
    try {
      // TODO: Implement actual image upload to S3
      // For now, create a preview URL
      const previewUrl = URL.createObjectURL(file);
      setImagePreview(previewUrl);
      setFieldValue("productImageUrl", previewUrl);
    } catch (error) {
      console.error("Error uploading image:", error);
    } finally {
      setUploading(false);
    }
  };

  const handleSubmit = async (
    values: any,
    { setSubmitting, resetForm }: any
  ) => {
    try {
      const orderData: CustomOrderRequest = {
        clientId: user?.id || 1, // TODO: Get from auth state
        productImageUrl: values.productImageUrl,
        referenceCode: values.referenceCode || undefined,
        description: values.description,
        preferredColor: values.preferredColor,
        alternativeColors: values.alternativeColors.filter((color: string) =>
          color.trim()
        ),
        size: values.size,
        category: values.category,
        observations: values.observations || undefined,
        estimatedPrice: values.estimatedPrice
          ? parseFloat(values.estimatedPrice)
          : undefined,
        urgency: values.urgency || "NORMAL",
        quantity: parseInt(values.quantity),
      };

      await dispatch(createCustomOrder(orderData)).unwrap();

      // Reset form and show success message
      resetForm();
      setImagePreview(null);

      // TODO: Navigate to orders list or show success dialog
    } catch (error) {
      console.error("Error creating order:", error);
    } finally {
      setSubmitting(false);
    }
  };

  const initialValues = {
    productImageUrl: "",
    referenceCode: "",
    description: "",
    preferredColor: "",
    alternativeColors: [""],
    size: "",
    category: "",
    observations: "",
    estimatedPrice: "",
    urgency: "NORMAL",
    quantity: 1,
  };

  return (
    <Box sx={{ p: 3, maxWidth: 800, mx: "auto" }}>
      {/* Header */}
      <Box sx={{ display: "flex", alignItems: "center", mb: 3 }}>
        <IconButton onClick={() => window.history.back()} sx={{ mr: 2 }}>
          <ArrowBack />
        </IconButton>
        <Typography variant="h4" component="h1">
          Criar Pedido Personalizado
        </Typography>
      </Box>

      {/* Error Alert */}
      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {/* Form */}
      <Formik
        initialValues={initialValues}
        validationSchema={validationSchema}
        onSubmit={handleSubmit}
      >
        {({ values, errors, touched, setFieldValue, isSubmitting }) => (
          <Form>
            <Grid container spacing={3}>
              {/* Image Upload */}
              <Grid item xs={12}>
                <Card>
                  <CardContent>
                    <Typography variant="h6" gutterBottom>
                      Imagem do Produto *
                    </Typography>
                    <Typography
                      variant="body2"
                      color="text.secondary"
                      sx={{ mb: 2 }}
                    >
                      Envie uma foto clara da peça que você deseja
                    </Typography>

                    {imagePreview ? (
                      <Box
                        sx={{ position: "relative", display: "inline-block" }}
                      >
                        <img
                          src={imagePreview}
                          alt="Preview"
                          style={{
                            maxWidth: "100%",
                            maxHeight: 300,
                            borderRadius: 8,
                          }}
                        />
                        <IconButton
                          sx={{
                            position: "absolute",
                            top: 8,
                            right: 8,
                            bgcolor: "rgba(0,0,0,0.5)",
                            color: "white",
                            "&:hover": { bgcolor: "rgba(0,0,0,0.7)" },
                          }}
                          onClick={() => {
                            setImagePreview(null);
                            setFieldValue("productImageUrl", "");
                          }}
                        >
                          <Delete />
                        </IconButton>
                      </Box>
                    ) : (
                      <Paper
                        sx={{
                          border: "2px dashed #ccc",
                          borderRadius: 2,
                          p: 4,
                          textAlign: "center",
                          cursor: "pointer",
                          "&:hover": { borderColor: "primary.main" },
                        }}
                        onClick={() =>
                          document.getElementById("image-upload")?.click()
                        }
                      >
                        <CloudUpload
                          sx={{ fontSize: 48, color: "text.secondary", mb: 2 }}
                        />
                        <Typography variant="body1" color="text.secondary">
                          Clique para enviar uma imagem
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          Formatos aceitos: JPG, PNG (máx. 10MB)
                        </Typography>
                      </Paper>
                    )}

                    <input
                      id="image-upload"
                      type="file"
                      accept="image/*"
                      style={{ display: "none" }}
                      onChange={(e) => {
                        const file = e.target.files?.[0];
                        if (file) {
                          handleImageUpload(file, setFieldValue);
                        }
                      }}
                    />

                    {uploading && <LinearProgress sx={{ mt: 2 }} />}

                    {errors.productImageUrl && touched.productImageUrl && (
                      <Typography
                        color="error"
                        variant="caption"
                        sx={{ mt: 1, display: "block" }}
                      >
                        {errors.productImageUrl}
                      </Typography>
                    )}
                  </CardContent>
                </Card>
              </Grid>

              {/* Basic Information */}
              <Grid item xs={12}>
                <Card>
                  <CardContent>
                    <Typography variant="h6" gutterBottom>
                      Informações Básicas
                    </Typography>

                    <Grid container spacing={2}>
                      <Grid item xs={12}>
                        <Field
                          as={TextField}
                          name="description"
                          label="Descrição do Produto *"
                          multiline
                          rows={4}
                          fullWidth
                          placeholder="Descreva detalhadamente a peça que você deseja..."
                          error={errors.description && touched.description}
                          helperText={
                            errors.description && touched.description
                              ? errors.description
                              : ""
                          }
                        />
                      </Grid>

                      <Grid item xs={12} sm={6}>
                        <Field
                          as={TextField}
                          name="referenceCode"
                          label="Código de Referência"
                          fullWidth
                          placeholder="Se a peça tiver um código"
                        />
                      </Grid>

                      <Grid item xs={12} sm={6}>
                        <FormControl fullWidth>
                          <InputLabel>Categoria *</InputLabel>
                          <Field
                            as={Select}
                            name="category"
                            label="Categoria"
                            error={errors.category && touched.category}
                          >
                            {categories.map((category) => (
                              <MenuItem key={category} value={category}>
                                {category}
                              </MenuItem>
                            ))}
                          </Field>
                        </FormControl>
                      </Grid>
                    </Grid>
                  </CardContent>
                </Card>
              </Grid>

              {/* Colors and Size */}
              <Grid item xs={12}>
                <Card>
                  <CardContent>
                    <Typography variant="h6" gutterBottom>
                      Cor e Tamanho
                    </Typography>

                    <Grid container spacing={2}>
                      <Grid item xs={12} sm={6}>
                        <Field
                          as={TextField}
                          name="preferredColor"
                          label="Cor Preferencial *"
                          fullWidth
                          error={
                            errors.preferredColor && touched.preferredColor
                          }
                          helperText={
                            errors.preferredColor && touched.preferredColor
                              ? errors.preferredColor
                              : ""
                          }
                        />
                      </Grid>

                      <Grid item xs={12} sm={6}>
                        <FormControl fullWidth>
                          <InputLabel>Tamanho *</InputLabel>
                          <Field
                            as={Select}
                            name="size"
                            label="Tamanho"
                            error={errors.size && touched.size}
                          >
                            {sizes.map((size) => (
                              <MenuItem key={size} value={size}>
                                {size}
                              </MenuItem>
                            ))}
                          </Field>
                        </FormControl>
                      </Grid>

                      <Grid item xs={12}>
                        <Typography variant="subtitle2" gutterBottom>
                          Cores Alternativas
                        </Typography>
                        <FieldArray name="alternativeColors">
                          {({ push, remove }) => (
                            <Box>
                              {values.alternativeColors.map((color, index) => (
                                <Box
                                  key={index}
                                  sx={{ display: "flex", gap: 1, mb: 1 }}
                                >
                                  <Field
                                    as={TextField}
                                    name={`alternativeColors.${index}`}
                                    placeholder="Cor alternativa"
                                    size="small"
                                    fullWidth
                                  />
                                  {values.alternativeColors.length > 1 && (
                                    <IconButton
                                      onClick={() => remove(index)}
                                      size="small"
                                      color="error"
                                    >
                                      <Delete />
                                    </IconButton>
                                  )}
                                </Box>
                              ))}
                              <Button
                                startIcon={<Add />}
                                onClick={() => push("")}
                                size="small"
                                variant="outlined"
                              >
                                Adicionar Cor
                              </Button>
                            </Box>
                          )}
                        </FieldArray>
                      </Grid>
                    </Grid>
                  </CardContent>
                </Card>
              </Grid>

              {/* Additional Information */}
              <Grid item xs={12}>
                <Card>
                  <CardContent>
                    <Typography variant="h6" gutterBottom>
                      Informações Adicionais
                    </Typography>

                    <Grid container spacing={2}>
                      <Grid item xs={12} sm={4}>
                        <Field
                          as={TextField}
                          name="quantity"
                          label="Quantidade *"
                          type="number"
                          fullWidth
                          inputProps={{ min: 1, max: 10 }}
                          error={errors.quantity && touched.quantity}
                          helperText={
                            errors.quantity && touched.quantity
                              ? errors.quantity
                              : ""
                          }
                        />
                      </Grid>

                      <Grid item xs={12} sm={4}>
                        <Field
                          as={TextField}
                          name="estimatedPrice"
                          label="Valor Estimado (R$)"
                          type="number"
                          fullWidth
                          placeholder="Opcional"
                          inputProps={{ min: 0, step: 0.01 }}
                        />
                      </Grid>

                      <Grid item xs={12} sm={4}>
                        <FormControl fullWidth>
                          <InputLabel>Urgência</InputLabel>
                          <Field as={Select} name="urgency" label="Urgência">
                            {urgencyLevels.map((level) => (
                              <MenuItem key={level.value} value={level.value}>
                                {level.label}
                              </MenuItem>
                            ))}
                          </Field>
                        </FormControl>
                      </Grid>

                      <Grid item xs={12}>
                        <Field
                          as={TextField}
                          name="observations"
                          label="Observações"
                          multiline
                          rows={3}
                          fullWidth
                          placeholder="Informações adicionais, prazo desejado, etc..."
                        />
                      </Grid>
                    </Grid>
                  </CardContent>
                </Card>
              </Grid>

              {/* Submit Button */}
              <Grid item xs={12}>
                <Box
                  sx={{ display: "flex", justifyContent: "flex-end", gap: 2 }}
                >
                  <Button
                    variant="outlined"
                    onClick={() => window.history.back()}
                    disabled={isSubmitting}
                  >
                    Cancelar
                  </Button>
                  <Button
                    type="submit"
                    variant="contained"
                    startIcon={<Send />}
                    disabled={isSubmitting || loading}
                    sx={{ minWidth: 150 }}
                  >
                    {isSubmitting ? "Enviando..." : "Criar Pedido"}
                  </Button>
                </Box>
              </Grid>
            </Grid>
          </Form>
        )}
      </Formik>
    </Box>
  );
};

export default CreateCustomOrder;
