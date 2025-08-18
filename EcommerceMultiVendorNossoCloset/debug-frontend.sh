#!/bin/bash

echo "🔍 Diagnosticando problemas do Frontend..."
echo

echo "📊 Status dos containers:"
docker-compose ps
echo

echo "📋 Logs do frontend (últimas 20 linhas):"
docker-compose logs --tail=20 frontend
echo

echo "🔧 Verificando arquitetura da imagem:"
docker image inspect ecommercemultivendornossocloset-frontend --format='{{.Architecture}}' 2>/dev/null || echo "Imagem não encontrada"
echo

echo "🚀 Tentando reconstruir o frontend..."
docker-compose stop frontend
docker-compose rm -f frontend
docker-compose build --no-cache frontend
echo

echo "📊 Status após reconstrução:"
docker-compose up -d frontend
echo

echo "⏳ Aguardando 15 segundos para inicialização..."
sleep 15

echo "📋 Status final:"
docker-compose ps frontend
echo

echo "📝 Logs finais:"
docker-compose logs --tail=10 frontend
echo

echo "✅ Debug concluído!"
echo
echo "💡 Se o problema persistir, verifique:"
echo "   - Docker está rodando"
echo "   - Arquitetura do sistema (x64/x86)"
echo "   - Espaço em disco disponível"
echo "   - Configurações de firewall"
echo

