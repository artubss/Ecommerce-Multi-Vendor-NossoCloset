package com.zosh.config;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

/**
 * Configuração do Google Drive API para armazenamento de PDFs
 * Fase 1 - Semana 3: Sistema de Upload e Storage
 */
@Slf4j
@Configuration
public class GoogleDriveConfig {

    private static final String APPLICATION_NAME = "Nosso Closet - Sistema de Catalogos";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);

    @Value("${google.drive.credentials.file.path:credentials.json}")
    private String credentialsFilePath;

    @Value("${google.drive.application.name:" + APPLICATION_NAME + "}")
    private String applicationName;

    @Value("${google.drive.folder.id:}")
    private String defaultFolderId;

    /**
     * Cria o cliente HTTP autorizado para acessar o Google Drive
     */
    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Carrega segredos do cliente
        InputStream in = GoogleDriveConfig.class.getResourceAsStream("/" + credentialsFilePath);
        if (in == null) {
            // Se não encontrar no classpath, tenta carregar do arquivo do sistema
            Path credentialsPath = Paths.get(credentialsFilePath);
            if (Files.exists(credentialsPath)) {
                in = Files.newInputStream(credentialsPath);
            } else {
                throw new FileNotFoundException("Arquivo de credenciais não encontrado: " + credentialsFilePath);
            }
        }

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Constrói o fluxo de autorização e trigga o fluxo de autorização
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    /**
     * Cria e configura o serviço do Google Drive
     */
    @Bean
    public Drive googleDriveService() throws GeneralSecurityException, IOException {
        try {
            // Constrói um novo serviço autorizado da API
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(applicationName)
                    .build();

            log.info("Google Drive API configurado com sucesso");
            return service;
        } catch (Exception e) {
            log.error("Erro ao configurar Google Drive API: {}", e.getMessage());
            log.warn("Google Drive será executado em modo simulado");
            return null; // Retorna null para indicar modo de desenvolvimento sem Google Drive
        }
    }

    /**
     * Retorna o ID da pasta padrão do Google Drive
     */
    public String getDefaultFolderId() {
        return defaultFolderId;
    }

    /**
     * Verifica se o Google Drive está configurado corretamente
     */
    public boolean isGoogleDriveEnabled() {
        try {
            return googleDriveService() != null;
        } catch (Exception e) {
            return false;
        }
    }
}
