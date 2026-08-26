param(
    [string]$PsqlPath = 'psql',
    [int]$ExpectedPsqlMajor = 16,
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
$env:PGPASSWORD = $DbPassword
$env:PGCLIENTENCODING = 'UTF8'

try {
    $psqlCommand = Get-Command -Name $PsqlPath -CommandType Application -ErrorAction Stop |
        Select-Object -First 1
} catch {
    throw "PostgreSQL Client 未安装或不在 PATH 中，可通过 -PsqlPath 显式指定 psql 路径: $PsqlPath"
}
$resolvedPsqlPath = $psqlCommand.Source
$psqlVersion = & $resolvedPsqlPath --version
if ($LASTEXITCODE -ne 0) {
    throw "psql --version failed with exit code $LASTEXITCODE"
}
Write-Host "Using PostgreSQL Client: $psqlVersion ($resolvedPsqlPath)"
if ($psqlVersion -notmatch 'PostgreSQL\)\s+(\d+)' -or [int]$Matches[1] -ne $ExpectedPsqlMajor) {
    throw "PostgreSQL Client 主版本必须为 $ExpectedPsqlMajor，实际版本: $psqlVersion"
}
$permissionCatalogFile = [System.IO.Path]::GetTempFileName()
$menuPermissionCatalogFile = [System.IO.Path]::GetTempFileName()
$featureCatalogFile = [System.IO.Path]::GetTempFileName()

function Invoke-Psql([string]$database, [string[]]$arguments) {
    & $resolvedPsqlPath -h $DbHost -p $DbPort -U $DbUser -d $database @arguments
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
        '-DsmartManage.postgresIntegration=true'
        "-DsmartManage.testDbUrl=jdbc:postgresql://${DbHost}:${DbPort}/${verifyDatabase}"
        "-DsmartManage.testDbUser=$DbUser"
        "-DsmartManage.testDbPassword=$DbPassword"
        '-Dtest=RoleDataScopeMapperPostgresTests,MonitorCatalogAccessorPostgresTests,MonitorPersistencePostgresTests,MonitorAlertConcurrencyPostgresTests'
        'test'
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
    $menuFeatureMismatchCount = & $resolvedPsqlPath -h $DbHost -p $DbPort -U $DbUser -d $verifyDatabase `
        -v ON_ERROR_STOP=1 -A -t -c 'SELECT count(*) FROM t_sys_menu menu JOIN t_sys_permission permission ON permission.id = menu.permission_id WHERE menu.feature_id <> permission.feature_id'
    if ($LASTEXITCODE -ne 0 -or [int]$menuFeatureMismatchCount -ne 0) {
        throw "menu feature consistency verification failed: $menuFeatureMismatchCount mismatches"
    }
    Invoke-Psql $verifyDatabase @(
        '-v', 'ON_ERROR_STOP=1',
        '-c', "SELECT 1 / count(*) AS invalid_feature_keys_removed FROM (SELECT 1 WHERE NOT EXISTS (SELECT 1 FROM t_sys_feature WHERE feature_key IN ('sys/base', 'sys/log', 'sys/scheduler', 'scm/procurement'))) verification;"
    )
    $permissionNumbers = & $resolvedPsqlPath -h $DbHost -p $DbPort -U $DbUser -d $verifyDatabase `
        -v ON_ERROR_STOP=1 -A -t -c 'SELECT number FROM t_sys_permission ORDER BY number'
    if ($LASTEXITCODE -ne 0) {
        throw "permission catalog query failed with exit code $LASTEXITCODE"
    }
    [System.IO.File]::WriteAllLines($permissionCatalogFile, [string[]]$permissionNumbers)
    $menuPermissionNumbers = & $resolvedPsqlPath -h $DbHost -p $DbPort -U $DbUser -d $verifyDatabase `
        -v ON_ERROR_STOP=1 -A -t -c 'SELECT DISTINCT permission.number FROM t_sys_permission permission JOIN t_sys_menu menu ON menu.permission_id = permission.id ORDER BY permission.number'
    if ($LASTEXITCODE -ne 0) {
        throw "menu permission catalog query failed with exit code $LASTEXITCODE"
    }
    [System.IO.File]::WriteAllLines($menuPermissionCatalogFile, [string[]]$menuPermissionNumbers)
    $featureKeys = & $resolvedPsqlPath -h $DbHost -p $DbPort -U $DbUser -d $verifyDatabase `
        -v ON_ERROR_STOP=1 -A -t -c 'SELECT feature_key FROM t_sys_feature ORDER BY feature_key'
    if ($LASTEXITCODE -ne 0) {
        throw "feature catalog query failed with exit code $LASTEXITCODE"
    }
    [System.IO.File]::WriteAllLines($featureCatalogFile, [string[]]$featureKeys)
    $permissionVerifier = Join-Path $PSScriptRoot '..\smart-manage-web\scripts\verify-permissions.mjs'
    & $NodePath $permissionVerifier "--catalog-file=$permissionCatalogFile" "--menu-catalog-file=$menuPermissionCatalogFile" "--feature-catalog-file=$featureCatalogFile"
    if ($LASTEXITCODE -ne 0) {
        throw "permission catalog verification failed with exit code $LASTEXITCODE"
    }
    Write-Host 'Flyway migration verification passed.'
}
finally {
    # The database name is generated internally, so cleanup cannot target a caller-supplied database.
    & $resolvedPsqlPath -h $DbHost -p $DbPort -U $DbUser -d postgres -v ON_ERROR_STOP=1 `
        -c "DROP DATABASE IF EXISTS $verifyDatabase WITH (FORCE)"
    Remove-Item -LiteralPath $permissionCatalogFile -Force -ErrorAction SilentlyContinue
    Remove-Item -LiteralPath $menuPermissionCatalogFile -Force -ErrorAction SilentlyContinue
    Remove-Item -LiteralPath $featureCatalogFile -Force -ErrorAction SilentlyContinue
}
