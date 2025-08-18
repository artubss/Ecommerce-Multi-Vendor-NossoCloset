# 🚀 E-commerce Multi-Vendor - Docker Setup

Este projeto foi configurado para rodar completamente em containers Docker, incluindo backend Spring Boot, frontend React, banco de dados PostgreSQL e Redis.

## 🏗️ Arquitetura

- **Backend**: Spring Boot 3.3.2 com Java 17
- **Frontend**: React 19 com Vite
- **Database**: PostgreSQL 15 (produção) / H2 (desenvolvimento)
- **Cache**: Redis 7
- **Reverse Proxy**: Nginx
- **Containerização**: Docker + Docker Compose

## 📋 Pré-requisitos

- Docker Desktop instalado e rodando
- Docker Compose instalado
- Mínimo 4GB RAM disponível
- Portas 80, 3000, 5432, 5454, 6379 disponíveis

## 🚀 Inicialização Rápida

### Windows
```bash
start.bat
```

### Linux/Mac
```bash
chmod +x start.sh
./start.sh
```

### Manual
```bash
docker-compose up --build -d
```

## 🔧 Configuração

### Variáveis de Ambiente
Copie o arquivo `env.example` para `.env` e ajuste conforme necessário:

```bash
cp env.example .env
```

### Portas
- **80**: Nginx (Frontend + API)
- **3000**: Frontend React
- **5432**: PostgreSQL
- **5454**: Backend Spring Boot
- **6379**: Redis

## 📊 Serviços

### 1. PostgreSQL Database
- **Container**: `ecommerce_postgres`
- **Porta**: 5432
- **Database**: `ecommerce_multi_vendor`
- **Usuário**: `postgres`
- **Senha**: `postgres`

### 2. Redis Cache
- **Container**: `ecommerce_redis`
- **Porta**: 6379
- **Persistência**: Volume Docker

### 3. Backend Spring Boot
- **Container**: `ecommerce_backend`
- **Porta**: 5454
- **Profile**: `docker`
- **Logs**: `./logs/`

### 4. Frontend React
- **Container**: `ecommerce_frontend`
- **Porta**: 3000
- **Build**: Vite + TypeScript

### 5. Nginx Reverse Proxy
- **Container**: `ecommerce_nginx`
- **Porta**: 80
- **SSL**: 443 (configurável)

## 🌐 URLs de Acesso

- **Frontend**: http://localhost
- **Backend API**: http://localhost:5454
- **H2 Console (dev)**: http://localhost:5454/h2-console
- **PostgreSQL**: localhost:5432
- **Redis**: localhost:6379

## 📝 Comandos Úteis

### Ver logs em tempo real
```bash
docker-compose logs -f
```

### Ver logs de um serviço específico
```bash
docker-compose logs -f backend
docker-compose logs -f frontend
```

### Parar todos os serviços
```bash
docker-compose down
```

### Parar e remover volumes
```bash
docker-compose down -v
```

### Reconstruir um serviço
```bash
docker-compose up --build -d backend
```

### Ver status dos containers
```bash
docker-compose ps
```

### Acessar container
```bash
docker-compose exec backend bash
docker-compose exec postgres psql -U postgres -d ecommerce_multi_vendor
```

## 🔍 Troubleshooting

### Backend não inicia
1. Verifique se o PostgreSQL está rodando: `docker-compose logs postgres`
2. Verifique as variáveis de ambiente
3. Verifique os logs: `docker-compose logs backend`

### Frontend não carrega
1. Verifique se o backend está rodando
2. Verifique os logs: `docker-compose logs frontend`
3. Verifique se a porta 3000 está disponível

### Database connection error
1. Aguarde o PostgreSQL inicializar completamente
2. Verifique as credenciais no arquivo `.env`
3. Verifique se a porta 5432 está disponível

### Porta já em uso
```bash
# Encontrar processo usando a porta
netstat -ano | findstr :80
# Matar o processo
taskkill /PID <PID> /F
```

## 🧹 Limpeza

### Remover todos os containers e volumes
```bash
docker-compose down -v
docker system prune -a
```

### Remover apenas volumes
```bash
docker-compose down -v
```

## 📚 Desenvolvimento

### Modo Desenvolvimento (H2 Database)
Para desenvolvimento local sem Docker, use o profile padrão que usa H2 Database em memória.

### Modo Produção (PostgreSQL)
Para produção, use o profile `docker` que conecta ao PostgreSQL.

## 🔐 Segurança

- **CORS**: Configurado para permitir todas as origens (ajuste para produção)
- **Rate Limiting**: Configurado no Nginx
- **SSL**: Configurável no Nginx (certificados em `./nginx/ssl/`)

## 📞 Suporte

Em caso de problemas:
1. Verifique os logs: `docker-compose logs`
2. Verifique o status: `docker-compose ps`
3. Reinicie os serviços: `docker-compose restart`
4. Reconstrua: `docker-compose up --build -d`

