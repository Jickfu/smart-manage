param(
    [string]$PsqlPath = 'psql',
    [int]$ExpectedPsqlMajor = 16,
    [string]$DbHost = 'localhost',
    [int]$DbPort = 5432,
    [string]$DbUser = 'postgres',
    [string]$DbPassword = 'postgres',
    [string]$MavenPath = 'mvn',
    [string]$NodePath = 'node',
    [string]$BusinessMigrationLocation = '',
    [string]$BusinessMinimumPlatformVersion = '2'
)

$ErrorActionPreference = 'Stop'
$verifyDatabase = 'smart_manage_verify_' + [Guid]::NewGuid().ToString('N')
$databaseCreated = $false
$verificationError = $null
$migrationDirectory = Join-Path $PSScriptRoot 'migration'
$backendPomPath = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\smart-manage-api\pom.xml'))
$migrationLocation = 'filesystem:' + $migrationDirectory.Replace('\', '/')
# 默认目录出现实际 SQL 即进入双链验证，防止二次开发 CI 只验证平台。
$businessDirectory = Join-Path $PSScriptRoot 'business'
if (!$BusinessMigrationLocation -and (Get-ChildItem -LiteralPath $businessDirectory -Filter '*.sql' -Recurse -File -ErrorAction SilentlyContinue)) {
    $BusinessMigrationLocation = 'filesystem:' + $businessDirectory.Replace('\', '/')
}
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
    $databaseCreated = $true

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
        '-Dtest=*PostgresTests'
        'test'
    )
    if ($BusinessMigrationLocation) {
        $flywayArguments += "-DsmartManage.testBusinessLocation=$BusinessMigrationLocation"
        $flywayArguments += "-DsmartManage.testBusinessMinimumPlatformVersion=$BusinessMinimumPlatformVersion"
    }
    Write-Host "Running Flyway with project: $backendPomPath"
    & $MavenPath @flywayArguments
    if ($LASTEXITCODE -ne 0) {
        throw "Flyway failed with exit code $LASTEXITCODE"
    }

    # OpenAPI 授权实体继承 BaseEntity；最后一项查询防止迁移遗漏实体自动映射的审计列。
    $baselineVerificationArguments = @('-v', 'ON_ERROR_STOP=1', '-c', "SELECT 1 / count(*) AS administrator_ready FROM t_sys_user WHERE username = 'administrator' AND enabled;", '-c', "SELECT 1 / count(*) AS attachment_cleanup_job_ready FROM t_sys_job WHERE number = 'ATTACHMENT_OBJECT_CLEANUP' AND is_system AND status = 'ENABLED' AND job_class_name = 'sm.domain.sys.scheduler.job.CleanTempFileJob';", '-c', "SELECT count(*) AS permission_count FROM t_sys_permission;", '-c', "SELECT count(*) AS menu_count FROM t_sys_menu;", '-c', "SELECT count(*) AS flyway_version_count FROM flyway_schema_history WHERE success;", '-c', "SELECT update_time, update_user FROM t_sys_openapi_grant LIMIT 0;")
    Invoke-Psql $verifyDatabase $baselineVerificationArguments
    $menuFeatureMismatchCount = & $resolvedPsqlPath -h $DbHost -p $DbPort -U $DbUser -d $verifyDatabase `
        -v ON_ERROR_STOP=1 -A -t -c 'SELECT count(*) FROM t_sys_menu menu JOIN t_sys_permission permission ON permission.id = menu.permission_id WHERE menu.feature_id <> permission.feature_id'
    if ($LASTEXITCODE -ne 0 -or [int]$menuFeatureMismatchCount -ne 0) {
        throw "menu feature consistency verification failed: $menuFeatureMismatchCount mismatches"
    }
    $featureVerificationArguments = @('-v', 'ON_ERROR_STOP=1', '-c', "SELECT 1 / count(*) AS invalid_feature_keys_removed FROM (SELECT 1 WHERE NOT EXISTS (SELECT 1 FROM t_sys_feature WHERE feature_key IN ('sys/base', 'sys/log', 'sys/scheduler', 'scm/procurement'))) verification;")
    Invoke-Psql $verifyDatabase $featureVerificationArguments
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
catch {
    $verificationError = $_
    throw
}
finally {
    # 只有收到本次建库成功结果才拥有清理权；创建失败或结果不确定时不得删除同名库。
    try {
        if ($databaseCreated) {
            Invoke-Psql 'postgres' @('-v', 'ON_ERROR_STOP=1', '-c', "DROP DATABASE $verifyDatabase WITH (FORCE)")
        }
    } catch {
        if ($null -eq $verificationError) { throw }
        # 清理失败单独报告，不能覆盖导致验证失败的原始异常。
        Write-Warning "验证库清理失败，请核实后清理 ${verifyDatabase}: $_"
    } finally {
        Remove-Item -LiteralPath $permissionCatalogFile -Force -ErrorAction SilentlyContinue
        Remove-Item -LiteralPath $menuPermissionCatalogFile -Force -ErrorAction SilentlyContinue
        Remove-Item -LiteralPath $featureCatalogFile -Force -ErrorAction SilentlyContinue
    }
}
