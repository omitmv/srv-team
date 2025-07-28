@echo off
title Iniciando projeto srv-team
color 0A

echo ========================================
echo     INICIANDO PROJETO SRV-TEAM
echo ========================================
echo.

REM Verificar se o Maven está instalado
REM echo [INFO] Verificando instalacao do Maven...
REM mvn --version >nul 2>&1
REM if errorlevel 1 (
REM     echo [ERRO] Maven nao encontrado! Por favor, instale o Maven e adicione ao PATH.
REM     echo [INFO] Download: https://maven.apache.org/download.cgi
REM     pause
REM     exit /b 1
REM )
REM echo [OK] Maven encontrado!
REM echo.

REM Verificar se o Java está instalado
REM echo [INFO] Verificando instalacao do Java...
REM java --version >nul 2>&1
REM if errorlevel 1 (
REM     echo [ERRO] Java nao encontrado! Por favor, instale o Java 17+ e adicione ao PATH.
REM     pause
REM     exit /b 1
REM )
REM echo [OK] Java encontrado!
REM echo.

REM Navegar para o diretório do projeto
cd /d "%~dp0"
echo [INFO] Diretorio atual: %CD%
echo.

REM Limpar e compilar o projeto
echo [INFO] Limpando e compilando o projeto...
echo ----------------------------------------
mvn clean compile
if errorlevel 1 (
    echo.
    echo [ERRO] Falha na compilacao do projeto!
    pause
    exit /b 1
)
echo.
echo [OK] Projeto compilado com sucesso!
echo.

REM Executar testes (opcional - comentado por padrão)
echo [INFO] Executando testes...
mvn test
if errorlevel 1 (
    echo [AVISO] Alguns testes falharam, mas continuando...
)
echo.

REM Iniciar a aplicação Spring Boot
echo ========================================
echo     INICIANDO APLICACAO SPRING BOOT
echo ========================================
echo.
echo [INFO] Iniciando srv-team na porta 8080...
echo [INFO] Acesse: http://localhost:8080
echo [INFO] Para parar a aplicacao, pressione Ctrl+C
echo.

mvn spring-boot:run

REM Se chegou até aqui, a aplicação foi encerrada
echo.
echo ========================================
echo       APLICACAO ENCERRADA
echo ========================================
pause
