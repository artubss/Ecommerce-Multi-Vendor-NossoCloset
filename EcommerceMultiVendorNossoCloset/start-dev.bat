@echo off
REM ===== SCRIPT DE INICIALIZAÇÃO - AMBIENTE DE DESENVOLVIMENTO =====
REM Fase 1 - Semana 1: Setup de Infraestrutura
REM Nosso Closet - Sistema de Pedidos Personalizados

echo.
echo ========================================
echo    NOSSO CLOSET - DESENVOLVIMENTO
echo ========================================
echo.

echo [1/5] Verificando Docker...
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERRO: Docker nao esta instalado ou nao esta rodando!
    echo Por favor, instale o Docker Desktop e inicie-o.
    pause
    exit /b 1
)

echo [2/5] Parando containers existentes...
docker-compose -f docker-compose.dev.yml down

echo [3/5] Removendo volumes antigos (opcional)...
set /p clean="Deseja limpar volumes antigos? (s/N): "
if /i "%clean%"=="s" (
    echo Limpando volumes...
    docker-compose -f docker-compose.dev.yml down -v
    docker system prune -f
)

echo [4/5] Construindo e iniciando containers...
docker-compose -f docker-compose.dev.yml up --build -d

echo [5/5] Aguardando servicos ficarem prontos...
timeout /t 30 /nobreak >nul

echo.
echo ========================================
echo    AMBIENTE INICIADO COM SUCESSO!
echo ========================================
echo.
echo Servicos disponiveis:
echo - Backend API: http://localhost:5454/api
echo - Frontend: http://localhost:3000
echo - PostgreSQL: localhost:5432
echo - Redis: localhost:6379
echo - RabbitMQ: http://localhost:15672 (admin/password)
echo - Prometheus: http://localhost:9090
echo - Grafana: http://localhost:3001 (admin/admin)
echo - Nginx: http://localhost:80
echo.
echo Para ver logs: docker-compose -f docker-compose.dev.yml logs -f
echo Para parar: docker-compose -f docker-compose.dev.yml down
echo.

pause
