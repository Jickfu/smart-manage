# 使用当前进程函数替身验证危险清理路径，不连接任何真实数据库。
$ErrorActionPreference = 'Stop'
$createdNames = [System.Collections.Generic.HashSet[string]]::new()
$verifyScript = Join-Path $PSScriptRoot 'verify-baseline.ps1'

function Get-Command {
    param($Name, $CommandType, $ErrorAction)
    [pscustomobject]@{ Source = 'Invoke-TestPsql' }
}
function Invoke-TestPsql {
    $global:LASTEXITCODE = 0
    $commandText = $args -join ' '
    if ($commandText -eq '--version') { return 'psql (PostgreSQL) 16.0' }
    if ($commandText -match 'CREATE DATABASE (\w+)') {
        $testState.createCount++
        if (!$createdNames.Add($Matches[1])) { throw '验证库名称重复' }
        if ($Matches[1] -notmatch '^smart_manage_verify_[a-f0-9]{32}$') { throw '验证库名不是随机 UUID' }
        if ($testState.scenario -eq 'create-failure') { $global:LASTEXITCODE = 31 }
    } elseif ($commandText -match 'DROP DATABASE') {
        $testState.dropCount++
        if ($testState.scenario -in @('double-failure', 'cleanup-failure')) { $global:LASTEXITCODE = 43 }
    } elseif ($commandText -match 'SELECT count\(\*\) FROM t_sys_menu') { return '0' }
    else { return 'test-catalog-entry' }
}
function Invoke-TestMaven {
    if ($testState.scenario -like 'with-domains*' -and $args -contains 'test' -and
        $args -notcontains '-Pwith-domains') {
        throw '可选领域未传入真实 PostgreSQL 测试'
    }
    $global:LASTEXITCODE = if ($testState.scenario -in @('migration-failure', 'double-failure')) { 37 } else { 0 }
}
function Get-Content {
    param($LiteralPath, [switch]$Raw)
    if ($LiteralPath -notmatch 'bootstrap[\\/]pom.xml$') { throw "非预期配置读取：$LiteralPath" }
    # 用两个其他业务领域验证通用装配，不让测试依赖上游当前的 Demo 清单。
    $expectedDomains = if ($testState.scenario -eq 'with-domains-empty') { 'sys' } else { 'sys,orders,invoices' }
    return "<project><profiles><profile><id>with-domains</id><properties><smartManage.expectedDomains>$expectedDomains</smartManage.expectedDomains></properties></profile></profiles></project>"
}
function Invoke-TestNode {
    $expectedFrontend = if ($testState.scenario -like 'with-domains*') { 'all' } else { 'sys' }
    $expectedBackend = if ($testState.scenario -eq 'with-domains') { 'sys,orders,invoices' } else { 'sys' }
    if ($env:SMART_MANAGE_DOMAINS -ne $expectedFrontend -or $args -notcontains "--backend-domains=$expectedBackend") {
        throw '权限校验的前后端选择不正确'
    }
    $global:LASTEXITCODE = 0
}

foreach ($scenario in @('create-failure', 'migration-failure', 'double-failure', 'cleanup-failure', 'success', 'success', 'with-domains', 'with-domains-empty')) {
    $testState = @{ createCount = 0; dropCount = 0; scenario = $scenario }

    $failure = $null
    try {
        $businessArguments = @{}
        if ($scenario -like 'with-domains*') {
            $businessArguments = @{ WithDomains = $true }
        }
        & $verifyScript -PsqlPath 'test-psql' -MavenPath 'Invoke-TestMaven' -NodePath 'Invoke-TestNode' -DbPassword 'unused' @businessArguments
    } catch { $failure = $_.Exception.Message }
    if ($testState.createCount -ne 1) { throw "${scenario}: 未执行预期建库" }
    $expectedDrops = if ($scenario -eq 'create-failure') { 0 } else { 1 }
    if ($testState.dropCount -ne $expectedDrops) { throw "${scenario}: 清理次数不符合建库所有权" }
    $expectedError = switch ($scenario) {
        'create-failure' { 'exit code 31' }
        'migration-failure' { 'exit code 37' }
        'double-failure' { 'exit code 37' }
        'cleanup-failure' { 'exit code 43' }
        default { $null }
    }
    if (($null -eq $expectedError -and $null -ne $failure) -or
        ($null -ne $expectedError -and $failure -notlike "*$expectedError*")) {
        throw "${scenario}: 原始失败未保留，实际 $failure"
    }
}
Write-Host 'Baseline cleanup safety and domain assembly arguments: 8 scenarios passed.'
