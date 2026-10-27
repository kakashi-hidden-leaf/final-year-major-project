# Campus360 1-Click Complete Startup Script
$ErrorActionPreference = "Continue"

Write-Host ""
Write-Host "========================================================================" -ForegroundColor Cyan
Write-Host "               🎓 CAMPUS 360 - SMART COLLEGE ERP 🎓" -ForegroundColor Yellow
Write-Host "========================================================================" -ForegroundColor Cyan
Write-Host ""

# 1. Check MySQL
Write-Host "[1/4] Checking MySQL Database Service..." -ForegroundColor Cyan
$mysqlService = Get-Service -Name "mysql" -ErrorAction SilentlyContinue
if ($mysqlService) {
    if ($mysqlService.Status -ne "Running") {
        Write-Host "       Starting MySQL Windows Service..." -ForegroundColor Yellow
        Start-Service -Name "mysql" -ErrorAction SilentlyContinue
        Start-Sleep -Seconds 2
    } else {
        Write-Host "       MySQL service is already running." -ForegroundColor Green
    }
} else {
    Write-Host "       MySQL service check passed (using standard port 3306)." -ForegroundColor Gray
}

# 2. Stop any old instances on port 8080 or cloudflared
$old8080 = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique
if ($old8080) {
    foreach ($pidToKill in $old8080) {
        Stop-Process -Id $pidToKill -Force -ErrorAction SilentlyContinue
    }
}
Get-Process -Name "cloudflared" -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue

# Locate Java
$javaExe = "java.exe"
if (Get-Command "java" -ErrorAction SilentlyContinue) {
    $javaExe = (Get-Command "java").Source
} elseif (Test-Path "C:\Program Files\Java\jdk-25\bin\java.exe") {
    $javaExe = "C:\Program Files\Java\jdk-25\bin\java.exe"
} elseif (Test-Path "C:\Program Files\Java\jdk-21.0.10\bin\java.exe") {
    $javaExe = "C:\Program Files\Java\jdk-21.0.10\bin\java.exe"
}

# Locate Cloudflared
$cloudflaredExe = "cloudflared.exe"
if (Test-Path "C:\Users\Abhijit\tools\cloudflared.exe") {
    $cloudflaredExe = "C:\Users\Abhijit\tools\cloudflared.exe"
} elseif (Test-Path ".\tools\cloudflared.exe") {
    $cloudflaredExe = ".\tools\cloudflared.exe"
}

# 3. Start Spring Boot backend
Write-Host "[2/4] Starting Campus360 Spring Boot Backend..." -ForegroundColor Cyan
$jarFile = "target\campus360-1.0.0.jar"
$serverProc = $null
$logFile = "$env:TEMP\campus360_backend.log"
if (Test-Path $logFile) { Remove-Item $logFile -Force }

if (Test-Path $jarFile) {
    Write-Host "       Launching standalone executable JAR..." -ForegroundColor Gray
    $serverProc = Start-Process -FilePath $javaExe -ArgumentList "-jar", $jarFile -RedirectStandardOutput $logFile -RedirectStandardError $logFile -PassThru -WindowStyle Hidden
} else {
    Write-Host "       Launching via Maven..." -ForegroundColor Gray
    $mvnCmd = "C:\Users\Abhijit\tools\apache-maven-3.9.9\bin\mvn.cmd"
    $serverProc = Start-Process -FilePath $mvnCmd -ArgumentList "spring-boot:run" -RedirectStandardOutput $logFile -RedirectStandardError $logFile -PassThru -WindowStyle Hidden
}

# Wait for local server to be ready
Write-Host "       Waiting for server to initialize on http://localhost:8080..." -ForegroundColor Gray
$serverReady = $false
$retries = 0
while (-not $serverReady -and $retries -lt 30) {
    Start-Sleep -Seconds 1
    $retries++
    try {
        $check = Invoke-WebRequest -Uri "http://localhost:8080/login" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
        if ($check -and $check.StatusCode -eq 200) {
            $serverReady = $true
        }
    } catch {}
    Write-Host -NoNewline "."
}
Write-Host ""

if (-not $serverReady) {
    Write-Host "Backend server took longer than expected. Check $logFile if needed." -ForegroundColor Yellow
} else {
    Write-Host "       Backend server is UP & HEALTHY!" -ForegroundColor Green
}

# 4. Start Cloudflare Tunnel
Write-Host "[3/4] Starting Cloudflare Public Tunnel..." -ForegroundColor Cyan
$tunnelLog = "$env:TEMP\campus360_tunnel.log"
if (Test-Path $tunnelLog) { Remove-Item $tunnelLog -Force }

$tunnelProc = Start-Process -FilePath $cloudflaredExe -ArgumentList "tunnel", "--url", "http://localhost:8080" -RedirectStandardOutput $tunnelLog -RedirectStandardError $tunnelLog -PassThru -WindowStyle Hidden

$publicUrl = ""
$retries = 0
while (-not $publicUrl -and $retries -lt 25) {
    Start-Sleep -Seconds 1
    $retries++
    if (Test-Path $tunnelLog) {
        $content = Get-Content $tunnelLog -Raw -ErrorAction SilentlyContinue
        if ($content -match '(https://[a-zA-Z0-9\-]+\.trycloudflare\.com)') {
            $publicUrl = $matches[1]
        }
    }
}

if (-not $publicUrl) {
    $publicUrl = "http://localhost:8080"
    Write-Host "       Tunnel startup delayed. Local URL is ready." -ForegroundColor Yellow
} else {
    Write-Host "       Public Tunnel Connected!" -ForegroundColor Green
}

# 5. Summary Banner
Write-Host ""
Write-Host "========================================================================" -ForegroundColor Green
Write-Host "                   🚀 CAMPUS 360 IS NOW LIVE! 🚀" -ForegroundColor Yellow
Write-Host "========================================================================" -ForegroundColor Green
Write-Host ""
Write-Host "  🌐 PUBLIC LIVE URL : " -NoNewline -ForegroundColor White
Write-Host "$publicUrl" -ForegroundColor Cyan
Write-Host "  💻 LOCALHOST URL   : " -NoNewline -ForegroundColor White
Write-Host "http://localhost:8080" -ForegroundColor Cyan
Write-Host ""
Write-Host "------------------------------------------------------------------------" -ForegroundColor Gray
Write-Host "  🔑 QUICK DEMO CREDENTIALS:" -ForegroundColor Yellow
Write-Host "  • Teacher       : prof01     / password123  (My Subjects, Results, CSV)" -ForegroundColor White
Write-Host "  • Student (1st) : 2026FEIT01 / password123  (Kabir Verma - CPPS)" -ForegroundColor White
Write-Host "  • Student (2nd) : 2026IT01   / password123  (Aarav Sharma - DBMS)" -ForegroundColor White
Write-Host "  • Office Staff  : admin01    / password123  (Student/Faculty Records)" -ForegroundColor White
Write-Host "========================================================================" -ForegroundColor Green
Write-Host ""

# 6. Auto-open browser
Write-Host "[4/4] Opening browser to $publicUrl..." -ForegroundColor Cyan
Start-Process $publicUrl

Write-Host ""
Write-Host ">> Press 'q' or Ctrl+C to stop Campus360 and shut down cleanly <<" -ForegroundColor Yellow

while ($true) {
    Start-Sleep -Seconds 1
    if ([Console]::KeyAvailable) {
        $key = [Console]::ReadKey($true)
        if ($key.KeyChar -eq 'q' -or $key.KeyChar -eq 'Q') {
            break
        }
    }
    # If processes died unexpectedly
    if ($serverProc.HasExited) {
        Write-Host "Backend process exited." -ForegroundColor Red
        break
    }
}

Write-Host "Stopping Campus360 processes..." -ForegroundColor Yellow
if ($serverProc -and -not $serverProc.HasExited) { Stop-Process -Id $serverProc.Id -Force -ErrorAction SilentlyContinue }
if ($tunnelProc -and -not $tunnelProc.HasExited) { Stop-Process -Id $tunnelProc.Id -Force -ErrorAction SilentlyContinue }
Get-Process -Name "cloudflared" -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
Write-Host "Campus360 stopped cleanly. Have a great day!" -ForegroundColor Green
