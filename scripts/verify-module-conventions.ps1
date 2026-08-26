<#
.SYNOPSIS
校验新增或显著扩展业务模块必须遵守的仓库级机械约束。

.DESCRIPTION
当前检查内容：
1. 模块开发指南、模式目录及模块开发 skill 等治理文件必须存在。
2. 根目录及前后端 AGENTS.md 必须正确路由到模块开发指南。
3. 模块开发 skill 必须调用本脚本。
4. 领域 TSX 文件禁止使用内联 style。
5. 前端操作反馈必须统一使用 useOperationFeedback。
6. 前端操作确认必须统一使用 useOperationConfirm。
7. Controller 权限注解必须引用权限常量，禁止直接填写字符串。
8. 页面注册文件必须存在，每个注册项必须声明非空 featureKey 和 pageType。

本脚本只负责适合源码静态扫描的确定性约束。Java 架构边界由 ArchUnit 测试校验；
业务状态、数据安全和交互语义仍需通过代码评审及风险驱动测试验证。

详细验证策略参见 docs/development/verification.md。

.PARAMETER RepositoryRoot
待校验的仓库根目录。省略时自动使用本脚本所在目录的上一级目录。

.EXAMPLE
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\verify-module-conventions.ps1

.EXAMPLE
pwsh.exe -NoProfile -Command "Get-Help .\scripts\verify-module-conventions.ps1 -Detailed"
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
$violations = [System.Collections.Generic.List[string]]::new()

function Resolve-RepositoryPath {
    param([Parameter(Mandatory)][string]$RelativePath)

    return Join-Path $resolvedRoot $RelativePath
}

function Add-Violation {
    param([Parameter(Mandatory)][string]$Message)

    $violations.Add($Message)
}

function Get-RepositoryRelativePath {
    param([Parameter(Mandatory)][string]$AbsolutePath)

    return $AbsolutePath.Substring($resolvedRoot.Length).TrimStart([char[]]@('\', '/'))
}

function Assert-RequiredFile {
    param([Parameter(Mandatory)][string]$RelativePath)

    if (-not (Test-Path -LiteralPath (Resolve-RepositoryPath $RelativePath) -PathType Leaf)) {
        Add-Violation "Required file is missing: $RelativePath"
    }
}

function Assert-FileContains {
    param(
        [Parameter(Mandatory)][string]$RelativePath,
        [Parameter(Mandatory)][string]$Pattern,
        [Parameter(Mandatory)][string]$Message
    )

    $absolutePath = Resolve-RepositoryPath $RelativePath
    if (-not (Test-Path -LiteralPath $absolutePath -PathType Leaf)) {
        return
    }
    $content = Get-Content -Raw -LiteralPath $absolutePath
    if ($content -notmatch $Pattern) {
        Add-Violation $Message
    }
}

function Assert-NoFileMatch {
    param(
        [Parameter(Mandatory)][string]$RelativeDirectory,
        [Parameter(Mandatory)][string]$Filter,
        [Parameter(Mandatory)][string]$Pattern,
        [Parameter(Mandatory)][string]$Message
    )

    $absoluteDirectory = Resolve-RepositoryPath $RelativeDirectory
    if (-not (Test-Path -LiteralPath $absoluteDirectory -PathType Container)) {
        Add-Violation "Directory to verify is missing: $RelativeDirectory"
        return
    }

    foreach ($file in Get-ChildItem -LiteralPath $absoluteDirectory -Recurse -File -Filter $Filter) {
        $content = Get-Content -Raw -LiteralPath $file.FullName
        if ($content -match $Pattern) {
            $relativeFile = Get-RepositoryRelativePath $file.FullName
            Add-Violation "${Message}: $relativeFile"
        }
    }
}

# 治理文件和代理路由必须完整，确保模块开发流程具有稳定入口。
$requiredFiles = @(
    'docs/development/module-development-guide.md',
    'docs/development/module-pattern-catalog.md',
    '.agents/skills/smart-manage-module/SKILL.md',
    '.agents/skills/smart-manage-module/agents/openai.yaml'
)
foreach ($requiredFile in $requiredFiles) {
    Assert-RequiredFile $requiredFile
}

Assert-FileContains 'AGENTS.md' 'docs/development/module-development-guide\.md' 'Root AGENTS.md does not route to the module development guide'
Assert-FileContains 'smart-manage-api/AGENTS.md' 'module-development-guide\.md' 'Backend AGENTS.md does not route to the module development guide'
Assert-FileContains 'smart-manage-web/AGENTS.md' 'module-development-guide\.md' 'Frontend AGENTS.md does not route to the module development guide'
Assert-FileContains '.agents/skills/smart-manage-module/SKILL.md' 'scripts\\verify-module-conventions\.ps1|scripts/verify-module-conventions\.ps1' 'Module skill does not invoke the convention verifier'

# 领域页面样式必须通过样式文件和既有设计体系维护，禁止散落内联样式。
Assert-NoFileMatch `
    -RelativeDirectory 'smart-manage-web/src/domain' `
    -Filter '*.tsx' `
    -Pattern '\bstyle\s*=' `
    -Message 'Domain TSX must not use inline style'

# 操作结果反馈必须经过统一封装，保持反馈级别和交互语义一致。
$frontendSourceRoot = Resolve-RepositoryPath 'smart-manage-web/src'
$operationFeedbackImplementation = Resolve-RepositoryPath 'smart-manage-web/src/domain/common/component/useOperationFeedback.tsx'
foreach ($frontendFile in Get-ChildItem -LiteralPath $frontendSourceRoot -Recurse -File -Include '*.ts', '*.tsx') {
    if ($frontendFile.FullName -eq $operationFeedbackImplementation) {
        continue
    }
    $content = Get-Content -Raw -LiteralPath $frontendFile.FullName
    if ($content -match '\bmessage\.(success|warning|error|info|open)\s*\(') {
        $relativeFile = Get-RepositoryRelativePath $frontendFile.FullName
        Add-Violation "Frontend operation feedback must use useOperationFeedback: $relativeFile"
    }
}

# 操作确认必须经过统一封装，避免各页面自行组合确认交互。
Assert-NoFileMatch `
    -RelativeDirectory 'smart-manage-web/src' `
    -Filter '*.tsx' `
    -Pattern '\bModal\.confirm\s*\(|<Popconfirm\b' `
    -Message 'Frontend operation confirmation must use useOperationConfirm'

# Controller 权限声明必须引用模块权限常量，禁止散落权限编码字面量。
Assert-NoFileMatch `
    -RelativeDirectory 'smart-manage-api/src/main/java' `
    -Filter '*Controller.java' `
    -Pattern '@SaCheckPermission\s*\(\s*["'']' `
    -Message 'Controller permission annotations must reference permission constants'

# 页面注册必须显式关联组件、功能和页面类型，不得通过命名或路径推断归属。
$registrationRoot = Resolve-RepositoryPath 'smart-manage-web/src/domain'
$registrationFiles = @(
    Get-ChildItem -LiteralPath $registrationRoot -Recurse -File |
        Where-Object { $_.Name -in @('pageRegistration.ts', 'pageRegistration.tsx') }
)
if ($registrationFiles.Count -eq 0) {
    Add-Violation 'No pageRegistration.ts or pageRegistration.tsx file was found'
}
foreach ($registrationFile in $registrationFiles) {
    $content = Get-Content -Raw -LiteralPath $registrationFile.FullName
    $componentCount = [regex]::Matches($content, '\bcomponentKey\s*:').Count
    $featureCount = [regex]::Matches($content, '\bfeatureKey\s*:').Count
    $pageTypeCount = [regex]::Matches($content, '\bpageType\s*:').Count
    $relativeFile = Get-RepositoryRelativePath $registrationFile.FullName

    if ($componentCount -eq 0) {
        Add-Violation "Page registration file has no registration entry: $relativeFile"
        continue
    }
    if ($featureCount -ne $componentCount) {
        Add-Violation "Every page registration entry must declare featureKey: $relativeFile"
    }
    if ($pageTypeCount -ne $componentCount) {
        Add-Violation "Every page registration entry must declare pageType: $relativeFile"
    }
    if ($content -match 'featureKey\s*:\s*""' -or $content -match "featureKey\s*:\s*''") {
        Add-Violation "Page registration featureKey must not be empty: $relativeFile"
    }
}

if ($violations.Count -gt 0) {
    Write-Host "Module convention verification failed with $($violations.Count) violation(s):" -ForegroundColor Red
    foreach ($violation in $violations) {
        Write-Host "- $violation" -ForegroundColor Red
    }
    exit 1
}

Write-Host "Module convention verification passed for governance routing, $($registrationFiles.Count) page registration file(s), frontend operation interactions, inline styles, and backend permission constants." -ForegroundColor Green
