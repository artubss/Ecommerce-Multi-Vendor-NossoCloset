# 📁 **CONFIGURAÇÃO DE STORAGE - SEMANA 3**

## 🎯 **VISÃO GERAL**

Este documento detalha como configurar o sistema híbrido de armazenamento implementado na Semana 3:

- **AWS S3** para armazenamento de imagens
- **Google Drive API** para armazenamento de PDFs de catálogos

---

## 🔧 **CONFIGURAÇÃO AWS S3**

### **1. Criar Bucket S3**

```bash
# Criar bucket no AWS CLI
aws s3 mb s3://nosso-closet-images --region us-east-1

# Configurar política de acesso público (para imagens)
aws s3api put-bucket-policy --bucket nosso-closet-images --policy file://s3-policy.json
```

### **2. Criar Usuário IAM**

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:GetObject",
        "s3:PutObject",
        "s3:DeleteObject",
        "s3:ListBucket"
      ],
      "Resource": [
        "arn:aws:s3:::nosso-closet-images",
        "arn:aws:s3:::nosso-closet-images/*"
      ]
    }
  ]
}
```

### **3. Variáveis de Ambiente S3**

```bash
# No arquivo .env ou docker-compose
AWS_ACCESS_KEY_ID=AKIA...
AWS_SECRET_ACCESS_KEY=...
AWS_S3_BUCKET_NAME=nosso-closet-images
AWS_S3_REGION=us-east-1
AWS_S3_ENDPOINT=  # Opcional para localstack
```

---

## 📄 **CONFIGURAÇÃO GOOGLE DRIVE API**

### **1. Criar Projeto no Google Cloud Console**

1. Acesse [Google Cloud Console](https://console.cloud.google.com/)
2. Crie um novo projeto: "Nosso Closet Storage"
3. Ative a Google Drive API

### **2. Criar Service Account**

1. Vá para **IAM & Admin > Service Accounts**
2. Crie nova conta de serviço:
   - Nome: `nosso-closet-storage`
   - Descrição: `Conta para upload de catálogos PDF`
3. Gere chave JSON e baixe o arquivo

### **3. Configurar Credenciais**

```bash
# Renomear arquivo baixado para credentials.json
mv ~/Downloads/nosso-closet-storage-xxxxx.json credentials.json

# Mover para pasta de recursos
cp credentials.json sourceCode/backend-spring/src/main/resources/
```

### **4. Criar Pasta no Google Drive**

1. Acesse [Google Drive](https://drive.google.com/)
2. Crie pasta: "Nosso Closet - Catálogos"
3. Compartilhe com a service account (email da conta criada)
4. Copie o ID da pasta da URL: `https://drive.google.com/drive/folders/FOLDER_ID_HERE`

### **5. Variáveis de Ambiente Google Drive**

```bash
# No arquivo .env ou docker-compose
GOOGLE_DRIVE_ENABLED=true
GOOGLE_DRIVE_CREDENTIALS_FILE=credentials.json
GOOGLE_DRIVE_FOLDER_ID=1A2B3C4D5E6F...  # ID da pasta criada
GOOGLE_DRIVE_APP_NAME=Nosso Closet - Sistema de Catalogos
```

---

## 🚀 **CONFIGURAÇÃO DOCKER**

### **1. Atualizar docker-compose.dev.yml**

```yaml
services:
  backend:
    environment:
      # AWS S3
      AWS_ACCESS_KEY_ID: ${AWS_ACCESS_KEY_ID}
      AWS_SECRET_ACCESS_KEY: ${AWS_SECRET_ACCESS_KEY}
      AWS_S3_BUCKET_NAME: ${AWS_S3_BUCKET_NAME:nosso-closet-images}
      AWS_S3_REGION: ${AWS_S3_REGION:us-east-1}

      # Google Drive
      GOOGLE_DRIVE_ENABLED: ${GOOGLE_DRIVE_ENABLED:true}
      GOOGLE_DRIVE_FOLDER_ID: ${GOOGLE_DRIVE_FOLDER_ID}

    volumes:
      - ./credentials.json:/app/credentials.json:ro # Montar credenciais
```

### **2. Arquivo .env.example**

```bash
# AWS S3 Configuration
AWS_ACCESS_KEY_ID=your_access_key_here
AWS_SECRET_ACCESS_KEY=your_secret_key_here
AWS_S3_BUCKET_NAME=nosso-closet-images
AWS_S3_REGION=us-east-1

# Google Drive Configuration
GOOGLE_DRIVE_ENABLED=true
GOOGLE_DRIVE_FOLDER_ID=your_folder_id_here
```

---

## 🧪 **TESTANDO AS CONFIGURAÇÕES**

### **1. Teste AWS S3**

```bash
# Endpoint de status
curl -X GET "http://localhost:5454/api/upload/status" \
     -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Upload de teste
curl -X POST "http://localhost:5454/api/upload/image/user-profile" \
     -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     -F "file=@test-image.jpg" \
     -F "userId=1"
```

### **2. Teste Google Drive**

```bash
# Upload de PDF de catálogo
curl -X POST "http://localhost:5454/api/upload/pdf/catalog" \
     -H "Authorization: Bearer YOUR_JWT_TOKEN" \
     -F "file=@catalog.pdf" \
     -F "supplierName=Fornecedor Teste" \
     -F "catalogName=Catálogo Verão 2024"
```

---

## 📊 **ESTRUTURA DE PASTAS**

### **AWS S3 - Estrutura de Diretórios**

```
nosso-closet-images/
├── images/
│   ├── catalog-products/
│   │   ├── fornecedor_1/
│   │   ├── fornecedor_2/
│   │   └── ...
│   ├── custom-orders/
│   │   ├── user-1/
│   │   ├── user-2/
│   │   └── ...
│   └── user-profiles/
└── thumbnails/
    ├── catalog-products/
    ├── custom-orders/
    └── user-profiles/
```

### **Google Drive - Estrutura de Pastas**

```
Nosso Closet - Catálogos/
├── Fornecedor Premium Moda/
├── Estilo Urbano Fornecimentos/
├── Elegância Feminina Ltda/
├── Moda Jovem Brasil/
└── Documentos Administrativos/
```

---

## 🔐 **CONFIGURAÇÕES DE SEGURANÇA**

### **1. Validações Implementadas**

#### **Imagens (S3)**

- ✅ Tipos permitidos: JPG, JPEG, PNG, WEBP
- ✅ Tamanho máximo: 10MB por arquivo
- ✅ Tamanho mínimo: 1KB
- ✅ Compressão automática para otimização
- ✅ Geração de thumbnails

#### **PDFs (Google Drive)**

- ✅ Tipo permitido: PDF apenas
- ✅ Tamanho máximo: 50MB por arquivo
- ✅ Tamanho mínimo: 1KB
- ✅ Validação de cabeçalho de arquivo

### **2. Controle de Acesso**

- ✅ **ADMIN**: Acesso total (upload, visualização, deleção)
- ✅ **SELLER**: Upload de produtos e catálogos
- ✅ **CUSTOMER**: Upload apenas de pedidos personalizados
- ✅ JWT token obrigatório para todas as operações

### **3. Rate Limiting**

```yaml
# Configuração recomendada
upload:
  rate-limit:
    requests-per-minute: 30
    max-file-size: 50MB
    max-files-per-request: 10
```

---

## 📈 **MONITORAMENTO E MÉTRICAS**

### **Métricas Implementadas**

- `upload.image.s3` - Total de uploads para S3
- `upload.pdf.drive` - Total de uploads para Google Drive
- `upload.catalog.product.image` - Imagens de produtos
- `upload.custom.order.image` - Imagens de pedidos
- `upload.catalog.pdf` - PDFs de catálogos
- `upload.error` - Erros de upload por tipo

### **Endpoints de Monitoramento**

```bash
# Métricas Prometheus
curl http://localhost:5454/api/actuator/prometheus | grep upload

# Status dos serviços
curl http://localhost:5454/api/upload/status
```

---

## 🚨 **TROUBLESHOOTING**

### **Problemas Comuns - AWS S3**

#### **Erro: Access Denied**

```bash
# Verificar credenciais
aws sts get-caller-identity

# Verificar política do bucket
aws s3api get-bucket-policy --bucket nosso-closet-images
```

#### **Erro: Bucket não encontrado**

```bash
# Verificar se bucket existe
aws s3 ls | grep nosso-closet

# Criar bucket se necessário
aws s3 mb s3://nosso-closet-images
```

### **Problemas Comuns - Google Drive**

#### **Erro: Credentials not found**

```bash
# Verificar se arquivo existe
ls -la sourceCode/backend-spring/src/main/resources/credentials.json

# Verificar permissões
chmod 600 credentials.json
```

#### **Erro: Access token expired**

- Delete a pasta `tokens/` criada automaticamente
- Reautorize a aplicação no próximo uso

#### **Erro: Insufficient permissions**

- Verifique se a service account tem acesso à pasta
- Compartilhe a pasta com o email da service account

---

## 🔄 **MODO DE DESENVOLVIMENTO**

Para desenvolvimento local sem configurações externas:

### **1. Modo Simulado**

```properties
# application-dev.properties
google.drive.enabled=false
aws.s3.enabled=false
```

### **2. Usando LocalStack (S3 Local)**

```bash
# Instalar LocalStack
pip install localstack

# Iniciar LocalStack
localstack start

# Configurar endpoint local
AWS_S3_ENDPOINT=http://localhost:4566
```

### **3. URLs Simuladas**

- S3: `https://simulated-s3.nosso-closet.local/bucket/file.jpg`
- Google Drive: `https://drive.google.com/file/d/simulated_abc123/view`

---

## ✅ **CHECKLIST DE CONFIGURAÇÃO**

### **AWS S3**

- [ ] Bucket criado no AWS
- [ ] Usuário IAM configurado
- [ ] Políticas de acesso definidas
- [ ] Variáveis de ambiente configuradas
- [ ] Teste de upload realizado

### **Google Drive**

- [ ] Projeto criado no Google Cloud
- [ ] Google Drive API ativada
- [ ] Service Account criada
- [ ] Arquivo credentials.json baixado
- [ ] Pasta criada e compartilhada
- [ ] Variáveis de ambiente configuradas
- [ ] Teste de upload realizado

### **Docker & Aplicação**

- [ ] docker-compose.dev.yml atualizado
- [ ] Arquivo .env configurado
- [ ] Credenciais montadas no container
- [ ] Logs verificados
- [ ] Endpoints testados

---

## 📞 **SUPORTE**

Para dúvidas sobre configuração:

1. Verifique os logs da aplicação
2. Teste os endpoints de status
3. Consulte a documentação oficial das APIs
4. Verifique as métricas de monitoramento

**Logs úteis:**

```bash
# Logs da aplicação
docker-compose logs backend

# Logs específicos de upload
docker-compose logs backend | grep -i upload
```
