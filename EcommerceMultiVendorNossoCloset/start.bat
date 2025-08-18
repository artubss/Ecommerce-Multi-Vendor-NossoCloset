@echo off
echo 🚀 Iniciando E-commerce Multi-Vendor com Docker...

REM Verificar se o Docker está rodando
docker info >nul 2>&1
if errorlevel 1 (
    echo ❌ Docker não está rodando. Por favor, inicie o Docker primeiro.
    pause
    exit /b 1
)

REM Verificar se o Docker Compose está disponível
docker-compose --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Docker Compose não está instalado. Por favor, instale o Docker Compose primeiro.
    pause
    exit /b 1
)

REM Criar diretórios necessários
echo 📁 Criando diretórios necessários...
if not exist "logs" mkdir logs
if not exist "nginx\ssl" mkdir nginx\ssl

REM Parar containers existentes
echo 🛑 Parando containers existentes...
docker-compose down

REM Construir e iniciar os containers
echo 🔨 Construindo e iniciando containers...
docker-compose up --build -d

REM Aguardar os serviços iniciarem
echo ⏳ Aguardando serviços iniciarem...
timeout /t 30 /nobreak >nul

REM Verificar status dos containers
echo 📊 Status dos containers:
docker-compose ps

REM Verificar logs do backend
echo 📋 Logs do Backend:
docker-compose logs backend --tail=20

REM Verificar logs do frontend
echo 📋 Logs do Frontend:
docker-compose logs frontend --tail=20

echo.
echo ✅ Aplicação iniciada com sucesso!
echo.
echo 🌐 URLs de acesso:
echo    Frontend: http://localhost
echo    Backend API: http://localhost:5454
echo    H2 Console (dev): http://localhost:5454/h2-console
echo    PostgreSQL: localhost:5432
echo    Redis: localhost:6379
echo.
echo 📝 Para ver logs em tempo real:
echo    docker-compose logs -f
echo.
echo 🛑 Para parar a aplicação:
echo    docker-compose down
echo.
pause

