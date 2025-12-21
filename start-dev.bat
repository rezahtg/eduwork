@echo off
echo Starting Eduwork Platform (Development Mode)...
echo.

REM Check if Docker services are running
docker ps | findstr "eduwork-postgres" >nul
if errorlevel 1 (
    echo [ERROR] PostgreSQL container is not running!
    echo Please run: docker-compose up -d
    pause
    exit /b 1
)

echo [OK] Docker services running
echo.

REM Set profile and run
echo Starting Spring Boot application...
set SPRING_PROFILES_ACTIVE=dev
cd backend
mvn spring-boot:run

pause
