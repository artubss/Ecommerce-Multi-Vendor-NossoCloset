@echo off
echo 🚀 Executando apenas o Frontend...

REM Parar container do frontend se existir
docker compose stop frontend
docker compose rm -f frontend

REM Construir e executar apenas o frontend
echo 🔨 Construindo e executando frontend...
docker compose up --build -d frontend

REM Aguardar um pouco
timeout /t 10 /nobreak >nul

REM Verificar status
echo 📊 Status do frontend:
docker compose ps frontend

REM Ver logs
echo 📋 Logs do frontend:
docker compose logs frontend --tail=20

echo.
echo ✅ Frontend executado!
echo 🌐 Acesse: http://localhost:3000
echo.
pause

