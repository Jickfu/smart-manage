param(
    [string]$PsqlPath = 'psql',
    [int]$ExpectedPsqlMajor = 16,
    [string]$DbHost = 'localhost',
    [int]$DbPort = 5432,
    [string]$DbUser = 'postgres',
    [string]$DbPassword = 'postgres',
    [string]$MavenPath = 'mvn',
    [string]$NodePath = 'node',
    [switch]$PlatformOnly
)

$ErrorActionPreference = 'Stop'
$verifyDatabase = 'smart_manage_verify_' + [Guid]::NewGuid().ToString('N')
$databaseCreated = $false
$verificationError = $null
$repositoryRoot = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$backendRoot = Join-Path $repositoryRoot 'smart-manage-server'
$backendPomPath = Join-Path $backendRoot 'pom.xml'
$platformPomPath = Join-Path $backendRoot 'platform/pom.xml'
$migrationDirectory = Join-Path $backendRoot 'platform/src/main/resources/db/platform/migration'
$migrationLocation = 'filesystem:' + $migrationDirectory.Replace('\', '/')
$previousDomains = $env:SMART_MANAGE_DOMAINS
$previousPgPassword = $env:PGPASSWORD
$previousEncoding = $env:PGCLIENTENCODING

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
# 后端当前领域由聚合模块维护；平台隔离模式显式排除所有可选领域。
$backendDomains = 'sys'
if (!$PlatformOnly) {
    [xml]$domainsPom = Get-Content -LiteralPath (Join-Path $backendRoot 'domains/pom.xml') -Raw
    $assemblyProfile = @($domainsPom.project.profiles.profile | Where-Object { $_.id -eq 'current-domains' })
    if ($assemblyProfile.Count -ne 1) { throw '缺少唯一的 current-domains 装配配置' }
    $domainModules = if ($null -eq $assemblyProfile[0].modules) { @() } else {
        @($assemblyProfile[0].modules.module | ForEach-Object { [string]$_ })
    }
    $backendDomains = (@('sys') + $domainModules) -join ','
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

$env:SMART_MANAGE_DOMAINS = if ($PlatformOnly) { 'sys' } else { 'all' }
$env:PGPASSWORD = $DbPassword
$env:PGCLIENTENCODING = 'UTF8'

try {
    Invoke-Psql 'postgres' @('-v', 'ON_ERROR_STOP=1', '-c', "CREATE DATABASE $verifyDatabase")
    $databaseCreated = $true

    # Flyway itself must execute migrations so versions, names, checksums, and schema history are verified.
    $flywayArguments = @(
        "--file=$platformPomPath"
        '--batch-mode'
        '--no-transfer-progress'
        'flyway:migrate'
        "-Dflyway.url=jdbc:postgresql://${DbHost}:${DbPort}/${verifyDatabase}"
        "-Dflyway.user=$DbUser"
        "-Dflyway.password=$DbPassword"
        "-Dflyway.locations=$migrationLocation"
    )
    & $MavenPath @flywayArguments
    if ($LASTEXITCODE -ne 0) { throw "Flyway failed with exit code $LASTEXITCODE" }
    # Reactor 为平台、领域及装配测试提供各自正确的 classpath。
    $testArguments = @(
        "--file=$backendPomPath", '--batch-mode', '--no-transfer-progress', 'test'
        '-DsmartManage.postgresIntegration=true'
        "-DsmartManage.testDbUrl=jdbc:postgresql://${DbHost}:${DbPort}/${verifyDatabase}"
        "-DsmartManage.testDbUser=$DbUser"
        "-DsmartManage.testDbPassword=$DbPassword"
        '-Dtest=*PostgresTests', '-Dsurefire.failIfNoSpecifiedTests=false'
    )
    if ($PlatformOnly) { $testArguments += '-Pplatform-only' }
    Write-Host "Running Flyway with project: $backendPomPath"
    & $MavenPath @testArguments
    if ($LASTEXITCODE -ne 0) {
        throw "Flyway failed with exit code $LASTEXITCODE"
    }

    # OpenAPI 授权实体继承 BaseEntity；最后一项查询防止迁移遗漏实体自动映射的审计列。
    $baselineVerificationArguments = @('-v', 'ON_ERROR_STOP=1', '-c', "SELECT 1 / count(*) AS administrator_ready FROM t_sys_user WHERE username = 'administrator' AND enabled AND password = '' AND password_reset;", '-c', "SELECT 1 / count(*) AS attachment_cleanup_job_ready FROM t_sys_job WHERE number = 'ATTACHMENT_OBJECT_CLEANUP' AND is_system AND status = 'ENABLED' AND job_class_name = 'sm.domain.sys.scheduler.job.CleanTempFileJob';", '-c', "SELECT count(*) AS permission_count FROM t_sys_permission;", '-c', "SELECT count(*) AS menu_count FROM t_sys_menu;", '-c', "SELECT count(*) AS flyway_version_count FROM flyway_schema_history WHERE success;", '-c', "SELECT update_time, update_user FROM t_sys_openapi_grant LIMIT 0;")
    Invoke-Psql $verifyDatabase $baselineVerificationArguments
    # 平台默认数据保持最小集合；业务领域自己的数据断言随领域测试维护。
    if ($PlatformOnly) {
        Invoke-Psql $verifyDatabase @('-v', 'ON_ERROR_STOP=1', '-c', "SELECT 1 / (CASE WHEN count(*) = 1 AND count(*) FILTER (WHERE id = 1 AND org_type = 'COMPANY' AND parent_id IS NULL) = 1 THEN 1 ELSE 0 END) AS platform_organizations FROM t_sys_org;")
    }
    $menuFeatureMismatchCount = & $resolvedPsqlPath -h $DbHost -p $DbPort -U $DbUser -d $verifyDatabase `
        -v ON_ERROR_STOP=1 -A -t -c 'SELECT count(*) FROM t_sys_menu menu JOIN t_sys_permission permission ON permission.id = menu.permission_id WHERE menu.feature_id <> permission.feature_id'
    if ($LASTEXITCODE -ne 0 -or [int]$menuFeatureMismatchCount -ne 0) {
        throw "menu feature consistency verification failed: $menuFeatureMismatchCount mismatches"
    }
    $featureVerificationArguments = @('-v', 'ON_ERROR_STOP=1', '-c', "SELECT 1 / count(*) AS invalid_feature_keys_removed FROM (SELECT 1 WHERE NOT EXISTS (SELECT 1 FROM t_sys_feature WHERE feature_key IN ('sys/base', 'sys/log', 'sys/scheduler'))) verification;")
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
    $permissionArguments = @(
        "--catalog-file=$permissionCatalogFile"
        "--menu-catalog-file=$menuPermissionCatalogFile"
        "--feature-catalog-file=$featureCatalogFile"
        $(if ($PlatformOnly) { '--backend-domains=sys' } else { "--backend-domains=$backendDomains" })
    )
    Write-Host "Verifying permission catalog with $($permissionArguments[-1])"
    & $NodePath $permissionVerifier @permissionArguments
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
        $env:SMART_MANAGE_DOMAINS = $previousDomains
        $env:PGPASSWORD = $previousPgPassword
        $env:PGCLIENTENCODING = $previousEncoding
        Remove-Item -LiteralPath $permissionCatalogFile -Force -ErrorAction SilentlyContinue
        Remove-Item -LiteralPath $menuPermissionCatalogFile -Force -ErrorAction SilentlyContinue
        Remove-Item -LiteralPath $featureCatalogFile -Force -ErrorAction SilentlyContinue
    }
}
