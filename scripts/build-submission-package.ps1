[CmdletBinding()]
param(
    [string]$OutputRoot
)

$ErrorActionPreference = 'Stop'

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
if (-not $OutputRoot) {
    $OutputRoot = Join-Path $repoRoot 'release'
}

$timestamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$packageRoot = Join-Path $OutputRoot ("final-{0}" -f $timestamp)
$stageRoot = Join-Path $packageRoot 'smart-parking'
$zipPath = Join-Path $packageRoot 'smart-parking-complete-submit.zip'
$hashPath = "$zipPath.sha256"

$excludedDirectories = @(
    '.git', '.github', '.idea', '.vscode', '.tmp', 'release', 'monitoring',
    'node_modules', 'target', 'dist', '__pycache__', '.pytest_cache',
    '.ruff_cache', 'logs'
)
$excludedFileNames = @(
    '.env', 'AGENTS.md', 'CLAUDE.md', '.cursorrules', '.cursorignore',
    '.dockerignore'
)
$excludedExtensions = @('.pyc', '.pyo', '.log', '.tmp', '.temp', '.aof', '.pid', '.rdb')

New-Item -ItemType Directory -Force -Path $stageRoot | Out-Null

Get-ChildItem -LiteralPath $repoRoot -Recurse -File -Force -ErrorAction SilentlyContinue | ForEach-Object {
    $relativePath = $_.FullName.Substring($repoRoot.Length + 1)
    $pathParts = $relativePath -split '[\\/]'

    if (($pathParts | Where-Object { $excludedDirectories -contains $_ }).Count -gt 0) {
        return
    }
    if ($excludedFileNames -contains $_.Name) {
        return
    }
    if ($excludedExtensions -contains $_.Extension.ToLowerInvariant()) {
        return
    }
    if ($relativePath -match '^ai-service[\\/]data[\\/]outputs[\\/]') {
        return
    }

    $destination = Join-Path $stageRoot $relativePath
    New-Item -ItemType Directory -Force -Path (Split-Path -Parent $destination) | Out-Null
    Copy-Item -LiteralPath $_.FullName -Destination $destination
}

$requiredFiles = @(
    'Redis\redis-server.exe',
    'Redis\redis.conf',
    'ai-service\model\parking.pt',
    'backend\pom.xml',
    'frontend\package.json',
    'smart_parking.sql'
)
foreach ($required in $requiredFiles) {
    if (-not (Test-Path -LiteralPath (Join-Path $stageRoot $required))) {
        throw "Submission package is missing required file: $required"
    }
}

tar.exe -a -c -f $zipPath -C $packageRoot 'smart-parking'
if ($LASTEXITCODE -ne 0) {
    throw 'Failed to create submission ZIP.'
}

$hash = (Get-FileHash -LiteralPath $zipPath -Algorithm SHA256).Hash
Set-Content -LiteralPath $hashPath -Encoding ASCII -Value ("{0}  {1}" -f $hash, (Split-Path -Leaf $zipPath))

Write-Host "Created package: $zipPath"
Write-Host "SHA256: $hash"
