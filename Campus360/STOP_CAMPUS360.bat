@echo off
title Stop Campus 360
cd /d "%~dp0"
echo Stopping Campus 360 backend and tunnel...
powershell -NoProfile -ExecutionPolicy Bypass -Command "Get-Process -Name 'cloudflared' -ErrorAction SilentlyContinue | Stop-Process -Force; $conns = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique; if ($conns) { foreach ($p in $conns) { Stop-Process -Id $p -Force -ErrorAction SilentlyContinue } }; Write-Host 'Campus360 stopped successfully!' -ForegroundColor Green"
echo Done.
timeout /t 3
