# Levanta todo el proyecto en local: microservicios, BFF y front.
# Uso (desde la raiz del monorepo):
#   powershell -ExecutionPolicy Bypass -File .\levantar-micros.ps1
#   powershell -ExecutionPolicy Bypass -File .\levantar-micros.ps1 -Infra   # tambien levanta MySQL, RabbitMQ y Kafka
#
# El BFF carga sus variables de entorno desde bff\entorno-local.ps1 (NO se sube a Git).

param(
    [switch]$Infra
)

$raiz = $PSScriptRoot
$timeoutSeg = 180

# Si tus compose locales estan en otra ruta, ajusta estas dos lineas
$composeInfra = Join-Path $raiz "infra"          # docker-compose.yml de db + rabbit
$composeKafka = Join-Path $raiz "infra\kafka"    # docker-compose.yml de Kafka

function Esperar-Puerto {
    param([int]$Puerto, [string]$Nombre)
    Write-Host "  Esperando a $Nombre (puerto $Puerto)..." -NoNewline
    $fin = (Get-Date).AddSeconds($timeoutSeg)
    while ((Get-Date) -lt $fin) {
        if (Get-NetTCPConnection -LocalPort $Puerto -State Listen -ErrorAction SilentlyContinue) {
            Write-Host " listo" -ForegroundColor Green
            return $true
        }
        Start-Sleep -Seconds 2
    }
    Write-Host " NO respondio en $timeoutSeg s" -ForegroundColor Red
    return $false
}

function Abrir-Ventana {
    param([string]$Nombre, [string]$Comando)
    $dir = Join-Path $raiz $Nombre
    if (-not (Test-Path $dir)) {
        Write-Warning "No existe la carpeta $dir"
        return
    }
    $cmd = "`$Host.UI.RawUI.WindowTitle = '$Nombre'; $Comando"
    Start-Process powershell -WorkingDirectory $dir -ArgumentList "-NoExit", "-Command", $cmd
    Write-Host "Iniciado $Nombre" -ForegroundColor Cyan
}

function Iniciar-Servicio {
    param([string]$Nombre)
    $dir = Join-Path $raiz $Nombre
    $mvn = if (Test-Path (Join-Path $dir "mvnw.cmd")) { ".\mvnw.cmd" } else { "mvn" }
    Abrir-Ventana $Nombre "$mvn spring-boot:run"
}

function Iniciar-Bff {
    $dir = Join-Path $raiz "bff"
    $mvn = if (Test-Path (Join-Path $dir "mvnw.cmd")) { ".\mvnw.cmd" } else { "mvn" }
    if (-not (Test-Path (Join-Path $dir "entorno-local.ps1"))) {
        Write-Warning "Falta bff\entorno-local.ps1: el BFF arrancara sin variables de entorno."
    }
    $cmd = "if (Test-Path .\entorno-local.ps1) { . .\entorno-local.ps1 }; $mvn spring-boot:run"
    Abrir-Ventana "bff" $cmd
}

function Iniciar-Front {
    $cmd = "if (-not (Test-Path node_modules)) { npm install }; npm run dev"
    Abrir-Ventana "tienda-insectos" $cmd
}

if ($Infra) {
    Write-Host "== Infraestructura (Docker) ==" -ForegroundColor Yellow
    Push-Location $composeInfra; docker compose up -d; Pop-Location
    Push-Location $composeKafka; docker compose up -d; Pop-Location
    Write-Host "Esperando 30 s a que MySQL, RabbitMQ y Kafka arranquen..."
    Start-Sleep -Seconds 30
}

Write-Host "== Microservicios ==" -ForegroundColor Yellow

# 1. ms-kafka-admin primero: crea los topicos que usan los demas
Iniciar-Servicio "ms-kafka-admin"
Esperar-Puerto 8087 "ms-kafka-admin" | Out-Null

# 2. El resto, en paralelo
Iniciar-Servicio "ms-proveedores"
Iniciar-Servicio "ms-pedidos"
Iniciar-Servicio "ms-notificaciones"
Iniciar-Servicio "ms-auditoria"
Iniciar-Servicio "ms-reportes"

# 3. ms-catalogo solo cuando ms-notificaciones ya responde
if (Esperar-Puerto 8085 "ms-notificaciones") {
    Iniciar-Servicio "ms-catalogo"
} else {
    Write-Warning "ms-notificaciones no levanto; no se inicia ms-catalogo. Revisa su ventana."
    exit 1
}

# 4. BFF y front al final
Write-Host "== BFF y front ==" -ForegroundColor Yellow
Iniciar-Bff
Iniciar-Front

Write-Host ""
Write-Host "Listo. Cada servicio corre en su propia ventana." -ForegroundColor Green
Write-Host "Front: http://localhost:5173   BFF: http://localhost:8080" -ForegroundColor Green
