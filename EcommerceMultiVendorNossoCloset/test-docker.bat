@echo off
echo ======================================
echo    TESTE DOCKER E-COMMERCE MULTI-VENDOR
echo ======================================
echo.

echo Limpando containers e imagens antigas...
docker-compose down -v
docker system prune -f
echo.

echo Parando todos os containers...
docker stop $(docker ps -aq) 2>nul
echo.

echo Construindo e iniciando servicos...
docker-compose up --build -d
echo.

echo Aguardando 30 segundos para os servicos iniciarem...
timeout /t 30 /nobreak > nul
echo.

echo Status dos containers:
docker-compose ps
echo.

echo Logs do frontend:
echo ==================
docker-compose logs frontend --tail=20
echo.

echo Logs do backend:
echo ================  
docker-compose logs backend --tail=20
echo.

echo ======================================
echo    TESTE COMPLETADO
echo ======================================
echo.
echo Frontend: http://localhost:3000
echo Backend: http://localhost:5454
echo Nginx Proxy: http://localhost:80
echo PostgreSQL: localhost:5432
echo Redis: localhost:6379
echo.

echo Para parar todos os servicos:
echo docker-compose down
echo.

echo Para ver logs em tempo real:
echo docker-compose logs -f [servico]
echo.

pause
