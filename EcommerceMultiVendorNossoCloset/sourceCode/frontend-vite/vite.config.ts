import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
// import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [react()],
  server: {
    host: true, // Permite conexões de qualquer IP (necessário para Docker)
    port: 3001,
    strictPort: true,
  },
  preview: {
    host: true,
    port: 3001,
    strictPort: true,
  },
  build: {
    outDir: "dist",
    sourcemap: false,
    // Ignorar erros de TypeScript durante build
    rollupOptions: {
      external: [],
      onwarn(warning, warn) {
        // Suprimir warnings específicos
        if (warning.code === "THIS_IS_UNDEFINED") return;
        if (warning.code === "CIRCULAR_DEPENDENCY") return;
        warn(warning);
      },
      output: {
        manualChunks: {
          vendor: ["react", "react-dom"],
          mui: ["@mui/material", "@mui/icons-material"],
          redux: ["@reduxjs/toolkit", "react-redux"],
        },
      },
    },
    // Continuar mesmo com erros
    minify: "esbuild",
    target: "esnext",
  },
  // Configurações para resolver erros comuns
  resolve: {
    alias: {
      "@": "/src",
    },
  },
  // Desabilitar verificação de tipos
  esbuild: {
    logOverride: { "this-is-undefined-in-esm": "silent" },
  },
});
