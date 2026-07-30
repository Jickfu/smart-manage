param(
    [string]$PsqlPath = 'D:\Program Files\PostgreSQL\16\bin\psql.exe',
    [string]$DbHost = 'localhost',
    [int]$DbPort = 5432,
    [string]$DbUser = 'postgres',
    [string]$DbPassword = 'postgres'
)

$ErrorActionPreference = 'Stop'
$verifyDatabase = 'smart_manage_verify_' + (Get-Date -Format 'yyyyMMddHHmmss')
$migrationDirectory = Join-Path $PSScriptRoot 'migration'
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

    # The generated database exists only while all migrations are verified from scratch.
    Get-ChildItem $migrationDirectory -Filter 'V*.sql' |
        Sort-Object { [int]([regex]::Match($_.Name, '^V(\d+)__').Groups[1].Value) } |
        ForEach-Object {
            Write-Host "Applying $($_.Name)"
            Invoke-Psql $verifyDatabase @('-v', 'ON_ERROR_STOP=1', '-f', $_.FullName)
        }

    Invoke-Psql $verifyDatabase @(
        '-v', 'ON_ERROR_STOP=1',
        '-c', "SELECT 1 / count(*) AS administrator_ready FROM t_sys_user WHERE username = 'administrator' AND enabled;",
        '-c', "SELECT count(*) AS permission_count FROM t_sys_permission;",
        '-c', "SELECT count(*) AS menu_count FROM t_sys_menu;"
    )
    Write-Host 'Flyway migration verification passed.'
}
finally {
    # The database name is generated internally, so cleanup cannot target a caller-supplied database.
    & $PsqlPath -h $DbHost -p $DbPort -U $DbUser -d postgres -v ON_ERROR_STOP=1 `
        -c "DROP DATABASE IF EXISTS $verifyDatabase WITH (FORCE)"
}
