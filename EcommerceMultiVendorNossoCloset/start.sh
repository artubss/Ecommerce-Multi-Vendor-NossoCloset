#!/bin/bash

echo "🚀 Iniciando E-commerce Multi-Vendor com Docker..."

# Verificar se o Docker está rodando
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker não está rodando. Por favor, inicie o Docker primeiro."
    exit 1
fi

# Verificar se o Docker Compose está disponível
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose não está instalado. Por favor, instale o Docker Compose primeiro."
    exit 1
fi

# Criar diretórios necessários
echo "📁 Criando diretórios necessários..."
mkdir -p logs
mkdir -p nginx/ssl

# Parar containers existentes
echo "🛑 Parando containers existentes..."
docker-compose down

# Remover volumes antigos (opcional - descomente se quiser limpar dados)
# echo "🧹 Removendo volumes antigos..."
# docker-compose down -v

# Construir e iniciar os containers
echo "🔨 Construindo e iniciando containers..."
docker-compose up --build -d

# Aguardar os serviços iniciarem
echo "⏳ Aguardando serviços iniciarem..."
sleep 30

# Verificar status dos containers
echo "📊 Status dos containers:"
docker-compose ps

# Verificar logs do backend
echo "📋 Logs do Backend:"
docker-compose logs backend --tail=20

# Verificar logs do frontend
echo "📋 Logs do Frontend:"
docker-compose logs frontend --tail=20

echo ""
echo "✅ Aplicação iniciada com sucesso!"
echo ""
echo "🌐 URLs de acesso:"
echo "   Frontend: http://localhost"
echo "   Backend API: http://localhost:5454"
echo "   H2 Console (dev): http://localhost:5454/h2-console"
echo "   PostgreSQL: localhost:5432"
echo "   Redis: localhost:6379"
echo ""
echo "📝 Para ver logs em tempo real:"
echo "   docker-compose logs -f"
echo ""
echo "🛑 Para parar a aplicação:"
echo "   docker-compose down"

