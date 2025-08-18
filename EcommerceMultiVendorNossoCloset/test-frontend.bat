@echo off
chcp 65001 >nul
echo 🧪 Testando Frontend...
echo.

echo 🚀 Fazendo build do frontend...
docker-compose build frontend
echo.

echo 📊 Iniciando frontend...
docker-compose up -d frontend
echo.

echo ⏳ Aguardando 10 segundos...
timeout /t 10 /nobreak >nul

echo 📋 Status do frontend:
docker-compose ps frontend
echo.

echo 📝 Logs do frontend:
docker-compose logs --tail=5 frontend
echo.

echo 🌐 Testando acesso ao frontend...
curl -s http://localhost:3000 | findstr "E-commerce"
echo.

echo ✅ Teste concluído!
pause

