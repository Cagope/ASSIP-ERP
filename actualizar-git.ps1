<#
==========================================================================
 📦 ASSIP-ERP — Script de actualización Git
 Autor: Dirección de Tecnología — ERP ASSIP Solidaria y Financiera
 Fecha: (Actualizado automáticamente)
--------------------------------------------------------------------------
 Propósito:
 Sincroniza los cambios del backend y frontend con el repositorio GitHub.
 - Verifica si hay cambios pendientes.
 - Realiza commit automático con fecha/hora.
 - Ejecuta push seguro a la rama configurada.
==========================================================================
#>

# === CONFIGURACIÓN INICIAL =============================================
$RAIZ  = "D:\assip-erp"      # Ruta raíz del proyecto
$RAMA  = "main"              # Rama de trabajo
$FECHA = Get-Date -Format "yyyy-MM-dd HH:mm:ss"

Write-Host "🔹 Iniciando actualización ASSIP-ERP — $FECHA" -ForegroundColor Cyan
Set-Location $RAIZ

# === FUNCIÓN DE PROCESO =================================================
function Actualizar-SubProyecto($ruta, $nombre) {
    Write-Host "`n📁 Procesando $nombre..." -ForegroundColor Yellow
    Set-Location $ruta

    # Verificar estado actual
    $estado = git status --porcelain
    if (-not $estado) {
        Write-Host "✅ Sin cambios pendientes en $nombre." -ForegroundColor Green
        return
    }

    # Agregar cambios
    git add .
    if ($LASTEXITCODE -ne 0) { Write-Host "❌ Error al agregar cambios en $nombre"; return }

    # Commit automático
    $mensaje = "Actualización automática $nombre — $FECHA"
    git commit -m "$mensaje"
    if ($LASTEXITCODE -ne 0) { Write-Host "⚠️ No se realizó commit en $nombre (posiblemente sin cambios nuevos)"; return }

    # Push remoto
    git push origin $RAMA
    if ($LASTEXITCODE -eq 0) {
        Write-Host "🚀 Cambios enviados correctamente en $nombre." -ForegroundColor Green
    } else {
        Write-Host "⚠️ Error al hacer push en $nombre." -ForegroundColor Red
    }
}

# === ACTUALIZAR BACKEND Y FRONTEND =====================================
Actualizar-SubProyecto "$RAIZ\backend" "Backend"
Actualizar-SubProyecto "$RAIZ\frontend" "Frontend"

# === FINAL ==============================================================
Write-Host "`n✅ Proceso completado. Fecha: $FECHA" -ForegroundColor Cyan
Pause
