param(
  [switch]$SkipAi,
  [switch]$SkipFrontend,
  [switch]$SkipBackend,
  [string]$CondaEnv = "python39"
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot

function Set-EnvDefault {
  param(
    [Parameter(Mandatory = $true)][string]$Name,
    [Parameter(Mandatory = $true)][string]$Value
  )
  if ([string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($Name, "Process"))) {
    [Environment]::SetEnvironmentVariable($Name, $Value, "Process")
  }
}

function Import-DotEnv {
  param([Parameter(Mandatory = $true)][string]$Path)
  if (-not (Test-Path -LiteralPath $Path)) {
    return
  }
  Get-Content -LiteralPath $Path | ForEach-Object {
    $line = $_.Trim()
    if ($line.Length -gt 0 -and -not $line.StartsWith("#") -and $line.Contains("=")) {
      $name, $value = $line.Split("=", 2)
      $name = $name.Trim()
      $value = $value.Trim().Trim('"').Trim("'")
      if ($name -and [string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($name, "Process"))) {
        [Environment]::SetEnvironmentVariable($name, $value, "Process")
      }
    }
  }
}

function Test-PortOpen {
  param(
    [Parameter(Mandatory = $true)][string]$HostName,
    [Parameter(Mandatory = $true)][int]$Port
  )
  try {
    $client = [System.Net.Sockets.TcpClient]::new()
    $task = $client.ConnectAsync($HostName, $Port)
    $ok = $task.Wait(1000) -and $client.Connected
    $client.Dispose()
    return $ok
  } catch {
    return $false
  }
}

Import-DotEnv -Path (Join-Path $Root ".env")

Set-EnvDefault -Name "MYSQL_HOST" -Value "127.0.0.1"
Set-EnvDefault -Name "MYSQL_PORT" -Value "3306"
Set-EnvDefault -Name "MYSQL_DATABASE" -Value "smart_parking"
Set-EnvDefault -Name "MYSQL_USER" -Value "Server"
Set-EnvDefault -Name "REDIS_HOST" -Value "127.0.0.1"
Set-EnvDefault -Name "REDIS_PORT" -Value "6379"
Set-EnvDefault -Name "REDIS_DATABASE" -Value "1"
Set-EnvDefault -Name "AI_FASTAPI_URL" -Value "http://127.0.0.1:8000"
Set-EnvDefault -Name "BACKEND_BASE_URL" -Value "http://127.0.0.1:8087"
Set-EnvDefault -Name "VITE_API_BASE_URL" -Value "http://127.0.0.1:8087"
Set-EnvDefault -Name "VITE_DEV_SERVER_PORT" -Value "5173"

$mysqlHost = [Environment]::GetEnvironmentVariable("MYSQL_HOST", "Process")
$mysqlPort = [int][Environment]::GetEnvironmentVariable("MYSQL_PORT", "Process")
$redisHost = [Environment]::GetEnvironmentVariable("REDIS_HOST", "Process")
$redisPort = [int][Environment]::GetEnvironmentVariable("REDIS_PORT", "Process")
$aiFastapiUrl = [Environment]::GetEnvironmentVariable("AI_FASTAPI_URL", "Process")
$aiPort = [int]([System.Uri]$aiFastapiUrl).Port
$frontendPort = [int][Environment]::GetEnvironmentVariable("VITE_DEV_SERVER_PORT", "Process")

if (-not (Test-PortOpen -HostName $mysqlHost -Port $mysqlPort)) {
  Write-Warning "MySQL is not reachable at ${mysqlHost}:${mysqlPort}. Start local MySQL and import smart_parking.sql before demo."
}
if (-not (Test-PortOpen -HostName $redisHost -Port $redisPort)) {
  Write-Warning "Redis is not reachable at ${redisHost}:${redisPort}. Start local Redis before demo."
}
if ([string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable("MYSQL_PASSWORD", "Process"))) {
  Write-Warning "MYSQL_PASSWORD is empty. Set it in .env or in the current PowerShell session if your local MySQL requires a password."
}

if (-not $SkipBackend) {
  $backendDir = Join-Path $Root "backend"
  $backendCommand = @"
Set-Location '$backendDir'
if (-not (Test-Path '.\ruoyi-admin\target\RuoyiSpringBoot3.jar')) {
  mvn.cmd -pl ruoyi-admin -am package -DskipTests
}
java -Xms256m -Xmx1024m -jar .\ruoyi-admin\target\RuoyiSpringBoot3.jar
"@
  Start-Process powershell.exe -ArgumentList @("-NoExit", "-ExecutionPolicy", "Bypass", "-Command", $backendCommand)
}

if (-not $SkipAi) {
  $aiDir = Join-Path $Root "ai-service"
  $conda = Get-Command conda.bat -ErrorAction SilentlyContinue
  if (-not $conda) {
    throw "conda.bat was not found. Install Miniconda/Anaconda or add conda to PATH."
  }
  $aiCommand = "Set-Location '$aiDir'; `$env:BACKEND_BASE_URL='$([Environment]::GetEnvironmentVariable("BACKEND_BASE_URL", "Process"))'; conda run -n $CondaEnv python -m uvicorn main_api:app --host 0.0.0.0 --port $aiPort"
  Start-Process powershell.exe -ArgumentList @("-NoExit", "-ExecutionPolicy", "Bypass", "-Command", $aiCommand)
}

if (-not $SkipFrontend) {
  $frontendDir = Join-Path $Root "frontend"
  $frontendCommand = "Set-Location '$frontendDir'; `$env:VITE_API_BASE_URL='$([Environment]::GetEnvironmentVariable("VITE_API_BASE_URL", "Process"))'; `$env:VITE_DEV_SERVER_PORT='$frontendPort'; npm.cmd run dev -- --host 127.0.0.1 --port $frontendPort"
  Start-Process powershell.exe -ArgumentList @("-NoExit", "-ExecutionPolicy", "Bypass", "-Command", $frontendCommand)
}

Write-Host "Demo startup requested."
Write-Host "Frontend: http://127.0.0.1:$frontendPort/admin/"
Write-Host "Backend:  http://127.0.0.1:8087"
Write-Host "AI:       $aiFastapiUrl"
