param(
  [string]$HostName = "127.0.0.1",
  [int]$Port = 3306,
  [string]$Database = "smart_parking",
  [string]$User = "Server",
  [Parameter(Mandatory = $true)][string]$Password,
  [string]$SqlPath
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
$BackupDir = Join-Path $Root ".tmp\db-backup"

if ([string]::IsNullOrWhiteSpace($SqlPath)) {
  $SqlPath = Join-Path $Root "smart_parking.sql"
}
if (-not (Test-Path -LiteralPath $SqlPath)) {
  throw "SQL file not found: $SqlPath"
}
if ($Database -notmatch '^[A-Za-z0-9_]+$') {
  throw "Database name contains unsupported characters: $Database"
}

New-Item -ItemType Directory -Force -Path $BackupDir | Out-Null

$backupPath = Join-Path $BackupDir ("smart_parking-backup-" + (Get-Date -Format "yyyyMMdd-HHmmss") + ".sql")
$mysql = (Get-Command mysql -ErrorAction Stop).Source
$mysqldump = (Get-Command mysqldump -ErrorAction Stop).Source
$env:MYSQL_PWD = $Password

try {
  Write-Host "Checking MySQL connection..."
  & $mysql --host=$HostName --port=$Port --user=$User --batch --skip-column-names --execute "SELECT 1" | Out-Null

  Write-Host "Creating database if it does not exist: $Database"
  & $mysql --host=$HostName --port=$Port --user=$User --execute "CREATE DATABASE IF NOT EXISTS $Database DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"

  Write-Host "Backing up current database to $backupPath"
  & $mysqldump --host=$HostName --port=$Port --user=$User --single-transaction --routines --triggers --events --no-tablespaces $Database | Set-Content -Encoding UTF8 -Path $backupPath

  Write-Host "Importing $SqlPath into $Database"
  Get-Content -LiteralPath $SqlPath -Raw | & $mysql --host=$HostName --port=$Port --user=$User --database=$Database --binary-mode=1 --init-command="SET NAMES utf8mb4"
  if ($LASTEXITCODE -ne 0) {
    throw "mysql import failed with exit code $LASTEXITCODE"
  }

  Write-Host "Core table row summary:"
  & $mysql --host=$HostName --port=$Port --user=$User --database=$Database --table --execute "SELECT table_name, table_rows FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name IN ('ai_event','biz_alarm','biz_parking_slot','dev_camera','biz_parking_roi','biz_region') ORDER BY table_name;"
} finally {
  Remove-Item Env:\MYSQL_PWD -ErrorAction SilentlyContinue
}
