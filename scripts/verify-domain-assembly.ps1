<#
.SYNOPSIS
校验当前后端领域聚合、前端领域目录和迁移身份保持一致。
#>
[CmdletBinding()]
param(
    [string]$RepositoryRoot
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

if ([string]::IsNullOrWhiteSpace($RepositoryRoot)) {
    $scriptDirectory = Split-Path -Parent $MyInvocation.MyCommand.Path
    $RepositoryRoot = Split-Path -Parent $scriptDirectory
}
$resolvedRoot = (Resolve-Path -LiteralPath $RepositoryRoot).Path
$backendDomainsRoot = Join-Path $resolvedRoot 'smart-manage-server/domains'
$frontendDomainsRoot = Join-Path $resolvedRoot 'smart-manage-web/src/domain'
$violations = [System.Collections.Generic.List[string]]::new()

function Add-Violation([string]$Message) {
    $violations.Add($Message)
}

function Assert-SameValues([string]$Label, [string[]]$Expected, [string[]]$Actual) {
    $difference = @(Compare-Object -ReferenceObject @($Expected | Sort-Object) -DifferenceObject @($Actual | Sort-Object))
    if ($difference.Count -gt 0) {
        Add-Violation "$Label 不一致；期望 [$($Expected -join ',')]，实际 [$($Actual -join ',')]"
    }
}

[xml]$aggregatorPom = Get-Content -LiteralPath (Join-Path $backendDomainsRoot 'pom.xml') -Raw
$currentProfiles = @($aggregatorPom.project.profiles.profile | Where-Object { $_.id -eq 'current-domains' })
$platformOnlyProfiles = @($aggregatorPom.project.profiles.profile | Where-Object { $_.id -eq 'platform-only' })
if ($currentProfiles.Count -ne 1) { Add-Violation 'domains/pom.xml 必须声明唯一 current-domains profile' }
if ($platformOnlyProfiles.Count -ne 1) { Add-Violation 'domains/pom.xml 必须声明唯一 platform-only profile' }

$backendDomainNames = @(Get-ChildItem -LiteralPath $backendDomainsRoot -Directory | ForEach-Object { $_.Name } | Sort-Object)
if ($currentProfiles.Count -eq 1) {
    $profile = $currentProfiles[0]
    $modules = if ($null -eq $profile.modules) { @() } else {
        @($profile.modules.module | ForEach-Object { [string]$_ })
    }
    $dependencies = if ($null -eq $profile.dependencies) { @() } else {
        @($profile.dependencies.dependency)
    }
    $dependencyNames = @($dependencies |
        Where-Object { $_.groupId -eq 'top.chekfu' } |
        ForEach-Object { ([string]$_.artifactId) -replace '^smart-manage-domain-', '' })
    Assert-SameValues '后端领域目录与 Maven modules' $backendDomainNames $modules
    Assert-SameValues '后端领域目录与聚合依赖' $backendDomainNames $dependencyNames
}

$frontendDomainNames = @(Get-ChildItem -LiteralPath $frontendDomainsRoot -Directory |
    Where-Object { $_.Name -notin @('sys', 'common') } |
    ForEach-Object { $_.Name } | Sort-Object)
Assert-SameValues '前后端业务领域' $backendDomainNames $frontendDomainNames

foreach ($domainName in $backendDomainNames) {
    $domainPom = Join-Path $backendDomainsRoot "$domainName/pom.xml"
    if (-not (Test-Path -LiteralPath $domainPom -PathType Leaf)) {
        Add-Violation "后端领域缺少 pom.xml: $domainName"
    }
    $migrationDeclaration = Join-Path $backendDomainsRoot "$domainName/src/main/resources/META-INF/smart-manage/migration.properties"
    if (-not (Test-Path -LiteralPath $migrationDeclaration -PathType Leaf)) {
        Add-Violation "后端领域缺少迁移声明: $domainName"
    } else {
        $properties = Get-Content -LiteralPath $migrationDeclaration -Raw | ConvertFrom-StringData
        if ($properties.id -ne $domainName) {
            Add-Violation "领域迁移 id 必须与目录名一致: $domainName"
        }
    }
    $frontendHome = Join-Path $frontendDomainsRoot "$domainName/applicationHomes.ts"
    if (-not (Test-Path -LiteralPath $frontendHome -PathType Leaf)) {
        Add-Violation "前端领域缺少 applicationHomes.ts: $domainName"
    }
}

[xml]$rootPom = Get-Content -LiteralPath (Join-Path $resolvedRoot 'smart-manage-server/pom.xml') -Raw
$rootModules = @($rootPom.project.modules.module | ForEach-Object { [string]$_ })
if ('domains' -notin $rootModules) { Add-Violation '后端父 POM 必须默认装配 domains 聚合模块' }
if ('app' -notin $rootModules) { Add-Violation '后端父 POM 必须装配唯一可运行的 app 模块' }
if ('bootstrap' -in $rootModules) { Add-Violation '后端父 POM 不得保留旧 bootstrap 模块' }

[xml]$appPom = Get-Content -LiteralPath (Join-Path $resolvedRoot 'smart-manage-server/app/pom.xml') -Raw
$appArtifactId = [string]$appPom.project.artifactId
if ($appArtifactId -ne 'smart-manage-app') { Add-Violation 'app artifactId 必须为 smart-manage-app' }
if ([string]$appPom.project.build.finalName -ne 'smart-manage-server') {
    Add-Violation 'app 最终可执行 JAR 必须固定命名为 smart-manage-server.jar'
}
$appArtifacts = @($appPom.project.dependencies.dependency | ForEach-Object { [string]$_.artifactId })
if ('smart-manage-domains' -notin $appArtifacts) {
    Add-Violation 'app 必须依赖 smart-manage-domains 聚合模块'
}
$directDomainDependencies = @($appArtifacts | Where-Object { $_ -like 'smart-manage-domain-*' })
if ($directDomainDependencies.Count -gt 0) {
    Add-Violation "app 不得逐个依赖业务领域: $($directDomainDependencies -join ',')"
}

if ($violations.Count -gt 0) {
    Write-Host "Domain assembly verification failed with $($violations.Count) violation(s):" -ForegroundColor Red
    foreach ($violation in $violations) { Write-Host "- $violation" -ForegroundColor Red }
    exit 1
}

Write-Host "Domain assembly verification passed: sys + [$($backendDomainNames -join ',')]." -ForegroundColor Green
