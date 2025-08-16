@echo off
title srv-team - Inicio Rapido
color 0A

echo Iniciando srv-team...
echo Acesse: http://localhost:8080
echo.

REM Navegar para o diretório do projeto
cd /d "%~dp0"

REM Iniciar diretamente a aplicação
mvn spring-boot:run

pause
