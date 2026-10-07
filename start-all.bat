@echo off
setlocal
echo ========================================================
echo   AutoLeilao - Inicializando Microsservicos (REST APIs)
echo ========================================================
echo.

set "JAVA_HOME=C:\Program Files\Java\jdk-25"
cd /d "%~dp0"

echo [1/2] Iniciando veiculos-service na porta 8081...
start "Microsservico: Veiculos (:8081)" cmd /k "%~dp0mvnw.bat" spring-boot:run -pl veiculos-service

timeout /t 5 /nobreak >nul

echo [2/2] Iniciando leilao-service na porta 8082...
start "Microsservico: Leilao (:8082)" cmd /k "%~dp0mvnw.bat" spring-boot:run -pl leilao-service

echo.
echo ========================================================
echo   Microsservicos iniciados com sucesso!
echo.
echo   MFE Cadastro (Porta 3001) conecta direto em:
echo     -> http://localhost:8081/api/veiculos
echo     -> Swagger: http://localhost:8081/swagger-ui.html
echo.
echo   MFE Leilao (Porta 3002) conecta direto em:
echo     -> http://localhost:8082/api/leiloes
echo     -> Swagger: http://localhost:8082/swagger-ui.html
echo ========================================================
echo.
pause
