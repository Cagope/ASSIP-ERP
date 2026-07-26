@echo off

REM ==========================================================
REM RESTAURACION DE BASE DE DATOS POSTGRESQL
REM ==========================================================
REM CAMBIAR ARCHIVO:
REM Ruta completa del backup que se desea restaurar
REM ==========================================================

set ARCHIVO=D:\backups_assip\backup_assip_erp_20260719_060101.backup

REM ==========================================================
REM CAMBIAR BASE_DESTINO:
REM Nombre de la base donde se restaurará la información
REM La base debe existir previamente.
REM Ejemplos:
REM assip_erp_prueba
REM assip_erp_desarrollo
REM assip_erp_test
REM ==========================================================

set BASE_DESTINO=assip_erp_prueba

REM ==========================================================
REM CAMBIAR USUARIO SI ES NECESARIO
REM Normalmente: postgres
REM ==========================================================

set USUARIO=postgres

REM ==========================================================
REM CAMBIAR RUTA SI CAMBIA LA VERSION DE POSTGRESQL
REM ==========================================================

set PG_BIN=C:\Program Files\PostgreSQL\17\bin

echo.
echo =========================================================
echo INICIANDO RESTAURACION
echo =========================================================
echo Archivo : %ARCHIVO%
echo Destino : %BASE_DESTINO%
echo Usuario : %USUARIO%
echo Inicio  : %DATE% %TIME%
echo =========================================================
echo.

"%PG_BIN%\pg_restore.exe" ^
-v ^
-c ^
-h localhost ^
-U %USUARIO% ^
-d %BASE_DESTINO% ^
"%ARCHIVO%"

echo.
echo =========================================================
echo RESTAURACION TERMINADA
echo Fecha : %DATE%
echo Hora  : %TIME%
echo =========================================================
echo.

pause