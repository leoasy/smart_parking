param(
  [string]$CondaEnv = "python39"
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot

$conda = Get-Command conda.bat -ErrorAction SilentlyContinue
if (-not $conda) {
  throw "conda.bat was not found. Install Miniconda/Anaconda or add conda to PATH."
}

Write-Host "Installing AI dependencies into conda env: $CondaEnv"
Set-Location (Join-Path $Root "ai-service")
conda run -n $CondaEnv python -m pip install -r requirements.txt

Write-Host "Installing frontend dependencies"
Set-Location (Join-Path $Root "frontend")
npm.cmd install

Write-Host "Packaging backend"
Set-Location (Join-Path $Root "backend")
mvn.cmd -pl ruoyi-admin -am package -DskipTests

Write-Host "Local dependencies are ready."
