[CmdletBinding()]
param(
    [ValidateSet('stable', 'current')]
    [string]$Profile = 'stable',

    [ValidateSet('2101.7.2-build.368', '2101.7.2-build.370')]
    [string]$KubeJSVersion = '2101.7.2-build.368',

    [switch]$IncludeNegative,

    [switch]$ExportSchemas,

    [switch]$PrepareOnly,

    [ValidateRange(30, 600)]
    [int]$StartupTimeoutSeconds = 180,

    [ValidateRange(30, 300)]
    [int]$ReloadTimeoutSeconds = 90
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version 3.0

if ($IncludeNegative -and $Profile -ne 'stable') {
    throw 'Negative fixtures include the stable-only pressure check and may only be run with -Profile stable.'
}

$repoRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..\..')).Path
$runDirectory = Join-Path $repoRoot 'run'
$serverScriptsDirectory = Join-Path $runDirectory 'kubejs\server_scripts'
$latestLog = Join-Path $runDirectory 'logs\latest.log'
$positiveDirectory = Join-Path $repoRoot 'tests\fixtures\kubejs\server_scripts\positive'
$currentDirectory = Join-Path $repoRoot 'tests\fixtures\kubejs\server_scripts\positive-current'
$negativeDirectory = Join-Path $repoRoot 'tests\fixtures\kubejs\server_scripts\negative'
$resultsDirectory = Join-Path $runDirectory 'runtime-results'
$gradleWrapper = Join-Path $repoRoot 'gradlew.bat'
$gradleProperties = Join-Path $repoRoot 'gradle.properties'

foreach ($requiredPath in @($gradleWrapper, $gradleProperties, $positiveDirectory, $currentDirectory, $negativeDirectory)) {
    if (-not (Test-Path -LiteralPath $requiredPath)) {
        throw "Required runtime-test path does not exist: $requiredPath"
    }
}

$neoForgeProperty = Get-Content -Encoding UTF8 -LiteralPath $gradleProperties |
    Where-Object { $_ -match '^neo_version=' } |
    Select-Object -First 1
if (-not $neoForgeProperty) {
    throw "neo_version is missing from $gradleProperties"
}
$neoForgeVersion = ($neoForgeProperty -split '=', 2)[1].Trim()
if (-not $neoForgeVersion) {
    throw "neo_version is empty in $gradleProperties"
}

New-Item -ItemType Directory -Force -Path $serverScriptsDirectory | Out-Null
New-Item -ItemType Directory -Force -Path $resultsDirectory | Out-Null

$resolvedScriptsDirectory = (Resolve-Path -LiteralPath $serverScriptsDirectory).Path
if (-not $resolvedScriptsDirectory.StartsWith($repoRoot, [System.StringComparison]::OrdinalIgnoreCase)) {
    throw "Refusing to modify server scripts outside the repository: $resolvedScriptsDirectory"
}

Get-ChildItem -LiteralPath $serverScriptsDirectory -File | Remove-Item -Force
Get-ChildItem -LiteralPath $positiveDirectory -File -Filter '*.js' |
    Copy-Item -Destination $serverScriptsDirectory

$expectedRecipeCount = 8
if ($Profile -eq 'current') {
    Copy-Item -LiteralPath (Join-Path $currentDirectory '09_vat_pressure.js') -Destination $serverScriptsDirectory
    $expectedRecipeCount = 9
}

if ($PrepareOnly) {
    Write-Output "FIXTURES_READY profile=$Profile recipes=$expectedRecipeCount directory=$serverScriptsDirectory"
    return
}

if (Test-Path -LiteralPath $latestLog) {
    Remove-Item -LiteralPath $latestLog -Force
}

function Get-LatestLogText {
    if (-not (Test-Path -LiteralPath $latestLog)) {
        return ''
    }

    try {
        $stream = [System.IO.FileStream]::new(
            $latestLog,
            [System.IO.FileMode]::Open,
            [System.IO.FileAccess]::Read,
            [System.IO.FileShare]::ReadWrite
        )
        try {
            $reader = [System.IO.StreamReader]::new($stream)
            try {
                return $reader.ReadToEnd()
            } finally {
                $reader.Dispose()
            }
        } finally {
            $stream.Dispose()
        }
    } catch [System.IO.IOException] {
        return ''
    }
}

function Get-TextTail {
    param(
        [Parameter(Mandatory)]
        [string]$Text,

        [Parameter(Mandatory)]
        [int]$Offset
    )

    if ($Text.Length -le $Offset) {
        return ''
    }

    return $Text.Substring($Offset)
}

$process = [System.Diagnostics.Process]::new()
$process.StartInfo = [System.Diagnostics.ProcessStartInfo]::new()
$process.StartInfo.FileName = $env:ComSpec
$process.StartInfo.Arguments = "/d /s /c `"gradlew.bat --no-daemon runServer -PtfmgProfile=$Profile -Pkubejs_version=$KubeJSVersion`""
$process.StartInfo.WorkingDirectory = $repoRoot
$process.StartInfo.UseShellExecute = $false
$process.StartInfo.CreateNoWindow = $true
$process.StartInfo.RedirectStandardInput = $true
$process.StartInfo.RedirectStandardOutput = $true
$process.StartInfo.RedirectStandardError = $true

if (-not $process.Start()) {
    throw 'Failed to start Gradle runServer process.'
}

$stdoutTask = $process.StandardOutput.ReadToEndAsync()
$stderrTask = $process.StandardError.ReadToEndAsync()
$stoppedCleanly = $false
$explicitReloadPassed = $false
$failure = $null
$negativeResults = [System.Collections.Generic.List[object]]::new()
$schemaExportResults = [System.Collections.Generic.List[string]]::new()

function Wait-ForCondition {
    param(
        [Parameter(Mandatory)]
        [scriptblock]$Condition,

        [Parameter(Mandatory)]
        [string]$Description,

        [Parameter(Mandatory)]
        [int]$TimeoutSeconds
    )

    $deadline = [DateTime]::UtcNow.AddSeconds($TimeoutSeconds)
    while ([DateTime]::UtcNow -lt $deadline) {
        if ($process.HasExited) {
            throw "runServer exited before $Description (exit code $($process.ExitCode))."
        }

        if (& $Condition) {
            return
        }

        Start-Sleep -Milliseconds 500
    }

    throw "Timed out after $TimeoutSeconds seconds waiting for $Description."
}

function Send-ServerCommand {
    param(
        [Parameter(Mandatory)]
        [string]$Command
    )

    $process.StandardInput.WriteLine($Command)
    $process.StandardInput.Flush()
}

function Invoke-CleanReload {
    param(
        [Parameter(Mandatory)]
        [int]$RecipeCount
    )

    $offset = (Get-LatestLogText).Length
    Send-ServerCommand -Command 'reload'

    Wait-ForCondition -Description "clean reload with $RecipeCount recipes" -TimeoutSeconds $ReloadTimeoutSeconds -Condition {
        $tail = Get-TextTail -Text (Get-LatestLogText) -Offset $offset
        return $tail -match "Added $RecipeCount recipes.*with 0 failed recipes" -and
            $tail.Contains('Server resource reload complete!')
    }
}

$negativeExpectations = [ordered]@{
    '01_casting_too_many_outputs.js.disabled' = [pscustomobject]@{ Message = "TFMG recipe 'casting' requires 1 item outputs, got 2"; FailedCounter = $true }
    '02_coking_missing_fluid_output.js.disabled' = [pscustomobject]@{ Message = "TFMG recipe 'coking' requires 2 fluid outputs, got 1"; FailedCounter = $true }
    '03_distillation_too_many_outputs.js.disabled' = [pscustomobject]@{ Message = "TFMG recipe 'distillation' requires 1..6 fluid outputs, got 7"; FailedCounter = $true }
    '04_industrial_blasting_too_many_inputs.js.disabled' = [pscustomobject]@{ Message = "TFMG recipe 'industrial_blasting' requires 1..2 item inputs, got 3"; FailedCounter = $true }
    '05_polarizing_legacy_energy.js.disabled' = [pscustomobject]@{ Message = 'Constructor for tfmg:polarizing with 3 arguments not found!'; FailedCounter = $true }
    '06_winding_missing_input.js.disabled' = [pscustomobject]@{ Message = "TFMG recipe 'winding' requires 2 item inputs, got 1"; FailedCounter = $true }
    '07_hot_blast_wrong_input_type.js.disabled' = [pscustomobject]@{ Message = "TFMG recipe 'hot_blast' requires 0 item inputs, got 1"; FailedCounter = $true }
    '08_vat_empty.js.disabled' = [pscustomobject]@{ Message = "TFMG recipe 'vat_machine_recipe' requires at least one input"; FailedCounter = $true }
    '09_vat_pressure_on_stable.js.disabled' = [pscustomobject]@{ Message = 'TFMG vat pressure requires TFMG 1.2.2 or newer'; FailedCounter = $false }
    '10_non_positive_time.js.disabled' = [pscustomobject]@{ Message = 'TFMG processing time must be greater than zero'; FailedCounter = $true }
    '11_unsupported_heat.js.disabled' = [pscustomobject]@{ Message = "TFMG recipe 'casting' does not support .heated() or .superheated()"; FailedCounter = $false }
}

try {
    Wait-ForCondition -Description 'dedicated server startup' -TimeoutSeconds $StartupTimeoutSeconds -Condition {
        $text = Get-LatestLogText
        return $text.Contains('Done (') -and
            $text -match "Added $expectedRecipeCount recipes.*with 0 failed recipes"
    }

    Invoke-CleanReload -RecipeCount $expectedRecipeCount
    $explicitReloadPassed = $true

    if ($ExportSchemas) {
        $schemaIds = @(
            'tfmg:casting',
            'tfmg:coking',
            'tfmg:distillation',
            'tfmg:industrial_blasting',
            'tfmg:polarizing',
            'tfmg:winding',
            'tfmg:hot_blast',
            'tfmg:vat_machine_recipe'
        )

        foreach ($schemaId in $schemaIds) {
            $offset = (Get-LatestLogText).Length
            Send-ServerCommand -Command "kubejs export recipe-schema-json $schemaId"

            Wait-ForCondition -Description "schema export for $schemaId" -TimeoutSeconds $ReloadTimeoutSeconds -Condition {
                $tail = Get-TextTail -Text (Get-LatestLogText) -Offset $offset
                return $tail.Contains("JSON of ${schemaId}:") -and
                    -not $tail.Contains("Failed to generate JSON of $schemaId")
            }

            $schemaExportResults.Add($schemaId)
        }
    }

    if ($IncludeNegative) {
        foreach ($entry in $negativeExpectations.GetEnumerator()) {
            $source = Join-Path $negativeDirectory $entry.Key
            $destinationName = [System.IO.Path]::GetFileNameWithoutExtension($entry.Key)
            $destination = Join-Path $serverScriptsDirectory $destinationName

            Copy-Item -LiteralPath $source -Destination $destination -Force
            $offset = (Get-LatestLogText).Length
            Send-ServerCommand -Command 'reload'

            Wait-ForCondition -Description "expected rejection for $($entry.Key)" -TimeoutSeconds $ReloadTimeoutSeconds -Condition {
                $tail = Get-TextTail -Text (Get-LatestLogText) -Offset $offset
                $counterMatched = if ($entry.Value.FailedCounter) {
                    $tail -match 'with 1 failed recipes'
                } else {
                    $tail -match 'with 0 failed recipes'
                }
                return $tail.Contains($entry.Value.Message) -and
                    $counterMatched -and
                    $tail.Contains('Server resource reload complete!')
            }

            $negativeResults.Add([pscustomobject]@{
                fixture = $entry.Key
                expected = $entry.Value.Message
                failedCounter = $entry.Value.FailedCounter
                passed = $true
            })

            Remove-Item -LiteralPath $destination -Force
            Invoke-CleanReload -RecipeCount $expectedRecipeCount
        }
    }

    Send-ServerCommand -Command 'stop'
    if (-not $process.WaitForExit(60000)) {
        throw 'Server did not stop within 60 seconds after the stop command.'
    }

    $stoppedCleanly = $true
    if ($process.ExitCode -ne 0) {
        throw "Gradle runServer exited with code $($process.ExitCode)."
    }
} catch {
    $failure = $_
} finally {
    if (-not $process.HasExited) {
        try {
            Send-ServerCommand -Command 'stop'
            if (-not $process.WaitForExit(30000)) {
                $process.Kill()
                $process.WaitForExit(10000) | Out-Null
            }
        } catch {
            if (-not $process.HasExited) {
                $process.Kill()
            }
        }
    }

    $stdout = $stdoutTask.GetAwaiter().GetResult()
    $stderr = $stderrTask.GetAwaiter().GetResult()
    $resultBaseName = "neoforge-$($neoForgeVersion.Replace('.', '_'))-$Profile-$($KubeJSVersion.Replace('.', '_'))"
    [System.IO.File]::WriteAllText((Join-Path $resultsDirectory "$resultBaseName-gradle.log"), $stdout + [Environment]::NewLine + $stderr)

    if (Test-Path -LiteralPath $latestLog) {
        Copy-Item -LiteralPath $latestLog -Destination (Join-Path $resultsDirectory "$resultBaseName-latest.log") -Force
    }

    $summary = [ordered]@{
        profile = $Profile
        neoforge = $neoForgeVersion
        kubejs = $KubeJSVersion
        expectedRecipes = $expectedRecipeCount
        explicitReload = $explicitReloadPassed
        schemaExports = @($schemaExportResults)
        negativeFixtures = @($negativeResults)
        stoppedCleanly = $stoppedCleanly
        exitCode = if ($process.HasExited) { $process.ExitCode } else { $null }
        passed = $failure -eq $null
        failure = if ($failure) { $failure.Exception.Message } else { $null }
    }
    $summary | ConvertTo-Json -Depth 5 | Set-Content -Encoding UTF8 -LiteralPath (Join-Path $resultsDirectory "$resultBaseName-summary.json")
}

if ($failure) {
    throw $failure
}

Write-Output "RUNTIME_OK neoforge=$neoForgeVersion profile=$Profile kubejs=$KubeJSVersion recipes=$expectedRecipeCount schemas=$($schemaExportResults.Count) negatives=$($negativeResults.Count)"
