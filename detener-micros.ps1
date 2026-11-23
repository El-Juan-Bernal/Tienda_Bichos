# Detiene todo el proyecto local: microservicios, BFF y front.
# Uso: powershell -ExecutionPolicy Bypass -File .\detener-micros.ps1

$servicios = [ordered]@{
    "tienda-insectos"   = 5173
    "bff"               = 8080
    "ms-catalogo"       = 8082
    "ms-pedidos"        = 8084
    "ms-notificaciones" = 8085
    "ms-proveedores"    = 8086
    "ms-kafka-admin"    = 8087
    "ms-reportes"       = 8088
    "ms-auditoria"      = 8089
}

foreach ($nombre in $servicios.Keys) {
    $puerto = $servicios[$nombre]
    $conexion = Get-NetTCPConnection -LocalPort $puerto -State Listen -ErrorAction SilentlyContinue |
        Select-Object -First 1
    if ($conexion) {
        taskkill /PID $conexion.OwningProcess /T /F | Out-Null
        Write-Host "Detenido $nombre (puerto $puerto)" -ForegroundColor Yellow
    } else {
        Write-Host "$nombre no estaba corriendo" -ForegroundColor DarkGray
    }
}
