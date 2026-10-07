# Script PowerShell para iniciar os microsserviços (conexão direta com os MFEs)
$ErrorActionPreference = "Stop"

if (-not $env:JAVA_HOME) {
    if (Test-Path "C:\Program Files\Java\jdk-25") {
        $env:JAVA_HOME = "C:\Program Files\Java\jdk-25"
    }
}

Set-Location $PSScriptRoot

Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "  AutoLeilão — Inicializando Microsserviços (REST APIs)" -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan

Write-Host "`n[1/2] Iniciando veiculos-service na porta 8081..." -ForegroundColor Green
Start-Process cmd.exe -ArgumentList "/k `"$PSScriptRoot\mvnw.bat`" spring-boot:run -pl veiculos-service"

Start-Sleep -Seconds 5

Write-Host "[2/2] Iniciando leilao-service na porta 8082..." -ForegroundColor Yellow
Start-Process cmd.exe -ArgumentList "/k `"$PSScriptRoot\mvnw.bat`" spring-boot:run -pl leilao-service"

Write-Host "`n========================================================" -ForegroundColor Cyan
Write-Host "  Microsserviços prontos para conexão direta com os MFEs!" -ForegroundColor Cyan
Write-Host "  - mfe-cadastro (:3001) -> http://localhost:8081/api/veiculos"
Write-Host "  - mfe-leilao   (:3002) -> http://localhost:8082/api/leiloes"
Write-Host "========================================================`n" -ForegroundColor Cyan
