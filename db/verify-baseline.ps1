param(
    [string]$PsqlPath = 'D:\Program Files\PostgreSQL\16\bin\psql.exe',
    [string]$DbHost = 'localhost',
    [int]$DbPort = 5432,
    [string]$DbUser = 'postgres',
    [string]$DbPassword = 'postgres',
    [string]$MavenPath = 'mvn',
    [string]$NodePath = 'node'
)

$ErrorActionPreference = 'Stop'
$verifyDatabase = 'smart_manage_verify_' + (Get-Date -Format 'yyyyMMddHHmmss')
$migrationDirectory = Join-Path $PSScriptRoot 'migration'
$backendPomPath = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\smart-manage-api\pom.xml'))
$migrationLocation = 'filesystem:' + $migrationDirectory.Replace('\', '/')
$permissionCatalogFile = [System.IO.Path]::GetTempFileName()
$menuPermissionCatalogFile = [System.IO.Path]::GetTempFileName()
$env:PGPASSWORD = $DbPassword
$env:PGCLIENTENCODING = 'UTF8'

function Invoke-Psql([string]$database, [string[]]$arguments) {
    & $PsqlPath -h $DbHost -p $DbPort -U $DbUser -d $database @arguments
    if ($LASTEXITCODE -ne 0) {
        throw "psql failed with exit code $LASTEXITCODE"
    }
}

try {
    Invoke-Psql 'postgres' @('-v', 'ON_ERROR_STOP=1', '-c', "CREATE DATABASE $verifyDatabase")

    # Flyway itself must execute migrations so versions, names, checksums, and schema history are verified.
    $flywayArguments = @(
        "--file=$backendPomPath"
        '--batch-mode'
        '--no-transfer-progress'
        'flyway:migrate'
        "-Dflyway.url=jdbc:postgresql://${DbHost}:${DbPort}/${verifyDatabase}"
        "-Dflyway.user=$DbUser"
        "-Dflyway.password=$DbPassword"
        "-Dflyway.locations=$migrationLocation"
    )
    Write-Host "Running Flyway with project: $backendPomPath"
    & $MavenPath @flywayArguments
    if ($LASTEXITCODE -ne 0) {
        throw "Flyway failed with exit code $LASTEXITCODE"
    }

    Invoke-Psql $verifyDatabase @(
        '-v', 'ON_ERROR_STOP=1',
        '-c', "SELECT 1 / count(*) AS administrator_ready FROM t_sys_user WHERE username = 'administrator' AND enabled;",
        '-c', "SELECT count(*) AS permission_count FROM t_sys_permission;",
        '-c', "SELECT count(*) AS menu_count FROM t_sys_menu;",
        '-c', "SELECT count(*) AS flyway_version_count FROM flyway_schema_history WHERE success;"
    )
    $permissionNumbers = & $PsqlPath -h $DbHost -p $DbPort -U $DbUser -d $verifyDatabase `
        -v ON_ERROR_STOP=1 -A -t -c 'SELECT number FROM t_sys_permission ORDER BY number'
    if ($LASTEXITCODE -ne 0) {
        throw "permission catalog query failed with exit code $LASTEXITCODE"
    }
    [System.IO.File]::WriteAllLines($permissionCatalogFile, [string[]]$permissionNumbers)
    $menuPermissionNumbers = & $PsqlPath -h $DbHost -p $DbPort -U $DbUser -d $verifyDatabase `
        -v ON_ERROR_STOP=1 -A -t -c 'SELECT DISTINCT permission.number FROM t_sys_permission permission JOIN t_sys_menu menu ON menu.permission_id = permission.id ORDER BY permission.number'
    if ($LASTEXITCODE -ne 0) {
        throw "menu permission catalog query failed with exit code $LASTEXITCODE"
    }
    [System.IO.File]::WriteAllLines($menuPermissionCatalogFile, [string[]]$menuPermissionNumbers)
    $permissionVerifier = Join-Path $PSScriptRoot '..\smart-manage-web\scripts\verify-permissions.mjs'
    & $NodePath $permissionVerifier "--catalog-file=$permissionCatalogFile" "--menu-catalog-file=$menuPermissionCatalogFile"
    if ($LASTEXITCODE -ne 0) {
        throw "permission catalog verification failed with exit code $LASTEXITCODE"
    }
    Write-Host 'Flyway migration verification passed.'
}
finally {
    # The database name is generated internally, so cleanup cannot target a caller-supplied database.
    & $PsqlPath -h $DbHost -p $DbPort -U $DbUser -d postgres -v ON_ERROR_STOP=1 `
        -c "DROP DATABASE IF EXISTS $verifyDatabase WITH (FORCE)"
    Remove-Item -LiteralPath $permissionCatalogFile -Force -ErrorAction SilentlyContinue
    Remove-Item -LiteralPath $menuPermissionCatalogFile -Force -ErrorAction SilentlyContinue
}
