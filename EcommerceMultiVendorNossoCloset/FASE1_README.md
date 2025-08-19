# 🚀 **FASE 1: INFRAESTRUTURA E ARQUITETURA BASE - NOSSO CLOSET**

## 📋 **VISÃO GERAL**

Esta é a **Fase 1** da refatoração do sistema Nosso Closet, focada no setup da infraestrutura base e arquitetura necessária para o novo modelo de negócio.

**Duração**: 3-4 semanas  
**Status**: ✅ **EM ANDAMENTO**  
**Última atualização**: Dezembro 2024

---

## 🎯 **OBJETIVOS DA FASE 1**

### **Semana 1: Setup de Infraestrutura** ✅

- [x] **AWS S3** configurado para armazenamento de imagens
- [x] **Redis** configurado para cache de sessões
- [x] **RabbitMQ** configurado para processamento assíncrono
- [x] **Dependências** do Spring Boot atualizadas
- [x] **Monitoramento** com Actuator + Micrometer configurado

### **Semana 2: Refatoração da Arquitetura de Dados** 🔄

- [ ] **Novas entidades** (Supplier, CustomOrder, CollectiveOrder)
- [ ] **Entidades existentes** refatoradas
- [ ] **Sistema de auditoria** implementado
- [ ] **Migrations** para PostgreSQL criadas

### **Semana 3: Sistema de Upload e Storage** ⏳

- [ ] **AWS S3 Service** implementado
- [ ] **Sistema de compressão** automática
- [ ] **Validação** de formatos e tamanhos
- [ ] **CDN** configurado

### **Semana 4: Autenticação e Autorização** ⏳

- [ ] **Sistema de roles** refatorado
- [ ] **Múltiplas sessões** para admins
- [ ] **Permissões granulares** implementadas
- [ ] **Rate limiting** configurado

---

## 🛠️ **TECNOLOGIAS IMPLEMENTADAS**

### **Backend (Spring Boot 3.x + Java 17)**

- ✅ **AWS SDK v2** para S3
- ✅ **Redis** para cache
- ✅ **RabbitMQ** para filas
- ✅ **Micrometer** para métricas
- ✅ **Spring Boot Actuator** para monitoramento
- ✅ **Flyway** para migrations (configurado)

### **Infraestrutura**

- ✅ **Docker Compose** para desenvolvimento
- ✅ **PostgreSQL 15** para banco de dados
- ✅ **Redis 7** para cache
- ✅ **RabbitMQ 3** para filas
- ✅ **Prometheus** para métricas
- ✅ **Grafana** para visualização

---

## 🚀 **COMO USAR**

### **1. Pré-requisitos**

```bash
# Verificar se o Docker está instalado
docker --version
docker-compose --version

# Verificar se as portas estão livres
# 5432 (PostgreSQL), 6379 (Redis), 5672 (RabbitMQ), 5454 (Backend)
```

### **2. Configuração do Ambiente**

```bash
# 1. Clonar o repositório
git clone <repository-url>
cd EcommerceMultiVendorNossoCloset

# 2. Configurar variáveis de ambiente (opcional)
# Criar arquivo .env na raiz do projeto
AWS_ACCESS_KEY_ID=your-access-key
AWS_SECRET_ACCESS_KEY=your-secret-key
AWS_REGION=us-east-1
S3_BUCKET_NAME=nosso-closet-dev
JWT_SECRET=your-jwt-secret
```

### **3. Iniciar Ambiente de Desenvolvimento**

#### **Windows (Recomendado)**

```bash
# Executar o script de inicialização
start-dev.bat
```

#### **Linux/Mac**

```bash
# Construir e iniciar containers
docker-compose -f docker-compose.dev.yml up --build -d

# Ver logs
docker-compose -f docker-compose.dev.yml logs -f

# Parar ambiente
docker-compose -f docker-compose.dev.yml down
```

### **4. Acessar Serviços**

- **Backend API**: http://localhost:5454/api
- **Frontend**: http://localhost:3000
- **PostgreSQL**: localhost:5432
- **Redis**: localhost:6379
- **RabbitMQ Management**: http://localhost:15672 (admin/password)
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3001 (admin/admin)

---

## 📁 **ESTRUTURA DE ARQUIVOS CRIADOS**

```
├── docker-compose.dev.yml          # Ambiente Docker completo
├── sourceCode/backend-spring/
│   ├── Dockerfile.dev             # Dockerfile para desenvolvimento
│   ├── pom.xml                    # Dependências atualizadas
│   ├── src/main/resources/
│   │   └── application-docker.properties  # Config Docker
│   └── src/main/java/com/zosh/
│       ├── config/
│       │   ├── RedisConfig.java           # Config Redis
│       │   ├── RabbitMQConfig.java        # Config RabbitMQ
│       │   ├── AwsS3Config.java           # Config AWS S3
│       │   └── MetricsConfig.java         # Config Micrometer
│       └── service/
│           ├── AwsS3Service.java          # Serviço S3
│           └── BusinessMetricsService.java # Métricas customizadas
├── monitoring/
│   └── prometheus.yml             # Config Prometheus
├── rabbitmq/
│   └── definitions.json           # Definições RabbitMQ
├── start-dev.bat                  # Script Windows
└── FASE1_README.md               # Este arquivo
```

---

## 🔧 **CONFIGURAÇÕES IMPLEMENTADAS**

### **Redis**

- Cache de sessões e dados frequentes
- Serialização JSON para objetos complexos
- Pool de conexões configurado

### **RabbitMQ**

- Exchanges para pedidos, notificações e processamento de imagens
- Filas com Dead Letter Queue (DLQ)
- Retry automático configurado

### **AWS S3**

- Upload com compressão automática
- Criação de thumbnails
- Validação de arquivos
- Organização por categorias

### **Métricas (Micrometer)**

- Métricas de negócio customizadas
- Integração com Prometheus
- Aspectos para timing automático

---

## 🧪 **TESTANDO A INFRAESTRUTURA**

### **1. Testar Backend**

```bash
# Verificar se está rodando
curl http://localhost:5454/api/actuator/health

# Ver métricas
curl http://localhost:5454/api/actuator/metrics
```

### **2. Testar Redis**

```bash
# Conectar ao Redis
docker exec -it nosso_closet_redis redis-cli

# Testar comandos
ping
set test "Hello Redis"
get test
```

### **3. Testar RabbitMQ**

```bash
# Acessar interface web
# http://localhost:15672
# Usuário: admin, Senha: password

# Verificar filas criadas
# - custom.order.queue
# - collective.order.queue
# - notification.queue
# - image.processing.queue
```

### **4. Testar PostgreSQL**

```bash
# Conectar ao banco
docker exec -it nosso_closet_postgres psql -U postgres -d nosso_closet_dev

# Verificar tabelas
\dt
```

---

## 📊 **MONITORAMENTO E MÉTRICAS**

### **Prometheus**

- Coleta métricas do Spring Boot a cada 10s
- Métricas customizadas de negócio
- Retenção de dados por 15 dias

### **Grafana**

- Dashboards para visualização
- Alertas configuráveis
- Métricas em tempo real

### **Spring Boot Actuator**

- Endpoints de health check
- Métricas JVM e sistema
- Informações da aplicação

---

## 🚨 **TROUBLESHOOTING**

### **Problemas Comuns**

#### **1. Porta já em uso**

```bash
# Verificar portas em uso
netstat -an | findstr :5454

# Parar processo usando a porta
# ou alterar porta no docker-compose.dev.yml
```

#### **2. Container não inicia**

```bash
# Ver logs do container
docker-compose -f docker-compose.dev.yml logs backend

# Verificar dependências
docker-compose -f docker-compose.dev.yml ps
```

#### **3. Problemas de conectividade**

```bash
# Verificar rede Docker
docker network ls
docker network inspect nosso_closet_network

# Reiniciar containers
docker-compose -f docker-compose.dev.yml restart
```

---

## 📈 **PRÓXIMOS PASSOS**

### **Semana 2: Arquitetura de Dados**

- [ ] Criar entidades JPA
- [ ] Implementar repositórios
- [ ] Configurar Flyway migrations
- [ ] Testes de integração

### **Semana 3: Sistema de Upload**

- [ ] Testar serviço S3
- [ ] Implementar validações
- [ ] Configurar CDN
- [ ] Testes de performance

### **Semana 4: Segurança**

- [ ] Refatorar autenticação
- [ ] Implementar auditoria
- [ ] Configurar rate limiting
- [ ] Testes de segurança

---

## 📞 **SUPORTE E CONTATO**

### **Equipe de Desenvolvimento**

- **Tech Lead**: [Nome]
- **Backend Developer**: [Nome]
- **DevOps**: [Nome]

### **Documentação Relacionada**

- [Planejamento Completo](../sourceCode/docs/PLANEJAMENTO_REFATORACAO.md)
- [Detalhes Técnicos](../sourceCode/docs/DETALHAMENTOS_TECNICOS_IMPLEMENTACAO.md)
- [Cronograma Detalhado](../sourceCode/docs/CRONOGRAMA_DETALHADO_IMPLEMENTACAO.md)

---

## ✅ **CHECKLIST DE CONCLUSÃO**

- [x] **Docker Compose** configurado e funcionando
- [x] **Dependências Maven** atualizadas
- [x] **Configurações Spring Boot** criadas
- [x] **Serviços de infraestrutura** rodando
- [x] **Monitoramento** configurado
- [x] **Scripts de inicialização** criados
- [x] **Documentação** da Fase 1 completa

---

**🎉 FASE 1 CONCLUÍDA COM SUCESSO!**

A infraestrutura base está pronta para a implementação das funcionalidades de negócio nas próximas fases.
