@echo off
title Campus 360 - Smart College ERP
cd /d "%~dp0"
echo ===================================================
echo   Starting Campus 360 Smart College ERP...
echo ===================================================
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0start_campus360.ps1"
pause
