@echo off

REM ==========================================
REM FECHA Y HORA YYYYMMDD_HHMMSS
REM ==========================================

for /f %%i in ('powershell -command "Get-Date -Format yyyyMMdd_HHmmss"') do set FECHA=%%i

REM ==========================================
REM BACKUP POSTGRES
REM ==========================================

"C:\Program Files\PostgreSQL\17\bin\pg_dump.exe" ^
-h localhost ^
-v ^
-U postgres ^
-d assip_erp ^
-F c ^
-f "D:\backups_assip\backup_assip_erp_%FECHA%.backup"

echo.
echo Backup terminado:
echo D:\backups_assip\backup_assip_erp_%FECHA%.backup
pause