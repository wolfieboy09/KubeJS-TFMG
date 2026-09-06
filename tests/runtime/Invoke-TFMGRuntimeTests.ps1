[CmdletBinding(PositionalBinding = $false)]
param(
    [string]$KubeJSVersion = '2101.7.2-build.370',
    [switch]$SkipNegative,
    [switch]$ExportSchemas,
    [switch]$PrepareOnly,
    [ValidateRange(30, 600)]
    [int]$StartupTimeoutSeconds = 180,
    [ValidateRange(30, 300)]
    [int]$ReloadTimeoutSeconds = 90
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version 3.0

if ($KubeJSVersion -notmatch '^\d+(\.\d+)*(-[A-Za-z0-9.]+)?$') {
    throw "KubeJSVersion '$KubeJSVersion' doesn't look like a version string (expected something like '2101.7.2-build.370'). " +
        "If you meant to pass a switch such as -SkipNegative or -IncludeNegative, PowerShell needs a single dash " +
        "(e.g. '-SkipNegative'), not a double dash ('--SkipNegative') - a double-dash argument doesn't match any " +
        "parameter name and was instead picked up here as the KubeJSVersion value."
}

$scriptStartedAt = Get-Date
$script:runLogPath = $null

function Write-Log {
    param(
        [Parameter(Mandatory)]
        [AllowEmptyString()]
        [string]$Message,
        [ValidateSet('INFO', 'STEP', 'WARN', 'ERROR', 'OK')]
        [string]$Level = 'INFO'
    )

    $timestamp = (Get-Date).ToString('HH:mm:ss.fff')
    $line = "[$timestamp] [$Level] $Message"

    $color = switch ($Level) {
        'STEP'  { 'Cyan' }
        'WARN'  { 'Yellow' }
        'ERROR' { 'Red' }
        'OK'    { 'Green' }
        default { 'Gray' }
    }
    Write-Host $line -ForegroundColor $color

    if ($script:runLogPath) {
        Add-Content -LiteralPath $script:runLogPath -Value $line
    }
}

Write-Log "Invoke-TFMGRuntimeTests starting (kubejs=$KubeJSVersion, negativeFixtures=$(-not $SkipNegative.IsPresent), exportSchemas=$($ExportSchemas.IsPresent), prepareOnly=$($PrepareOnly.IsPresent))" -Level STEP

$repoRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..\..')).Path
$runDirectory = Join-Path $repoRoot 'run'
$serverScriptsDirectory = Join-Path $runDirectory 'kubejs\server_scripts'
$latestLog = Join-Path $runDirectory 'logs\latest.log'
$positiveDirectory = Join-Path $repoRoot 'tests\fixtures\kubejs\server_scripts\positive'
$negativeDirectory = Join-Path $repoRoot 'tests\fixtures\kubejs\server_scripts\negative'
$resultsDirectory = Join-Path $runDirectory 'runtime-results'
$gradleWrapper = Join-Path $repoRoot 'gradlew.bat'
$gradleProperties = Join-Path $repoRoot 'gradle.properties'

if (Test-Path -LiteralPath $resultsDirectory) {
    $oldResults = Get-ChildItem -LiteralPath $resultsDirectory -File
    if ($oldResults.Count -gt 0) {
        Write-Log "Deleting $($oldResults.Count) old runtime-results file(s) from $resultsDirectory"
        $oldResults | Remove-Item -Force
    }
} else {
    New-Item -ItemType Directory -Force -Path $resultsDirectory | Out-Null
}
$script:runLogPath = Join-Path $resultsDirectory "run-$($scriptStartedAt.ToString('yyyyMMdd-HHmmss')).log"
Write-Log "Repo root resolved to $repoRoot"
Write-Log "Writing full run log to $script:runLogPath"

Write-Log "Checking required paths exist" -Level STEP
foreach ($requiredPath in @($gradleWrapper, $gradleProperties, $positiveDirectory, $negativeDirectory)) {
    if (-not (Test-Path -LiteralPath $requiredPath)) {
        Write-Log "Missing required path: $requiredPath" -Level ERROR
        throw "Required runtime-test path does not exist: $requiredPath"
    }
    Write-Log "Found: $requiredPath"
}

$neoForgeProperty = Get-Content -Encoding UTF8 -LiteralPath $gradleProperties |
    Where-Object { $_ -match '^neo_version=' } |
    Select-Object -First 1

if (-not $neoForgeProperty) {
    Write-Log "neo_version property not found in $gradleProperties" -Level ERROR
    throw "neo_version is missing from $gradleProperties"
}

$neoForgeVersion = ($neoForgeProperty -split '=', 2)[1].Trim()

if (-not $neoForgeVersion) {
    Write-Log "neo_version value is empty in $gradleProperties" -Level ERROR
    throw "neo_version is empty in $gradleProperties"
}

Write-Log "Detected neo_version=$neoForgeVersion"

New-Item -ItemType Directory -Force -Path $serverScriptsDirectory | Out-Null

$resolvedScriptsDirectory = (Resolve-Path -LiteralPath $serverScriptsDirectory).Path
if (-not $resolvedScriptsDirectory.StartsWith($repoRoot, [System.StringComparison]::OrdinalIgnoreCase)) {
    Write-Log "Refusing to modify server scripts outside the repository: $resolvedScriptsDirectory" -Level ERROR
    throw "Refusing to modify server scripts outside the repository: $resolvedScriptsDirectory"
}

Write-Log "Staging fixtures" -Level STEP
$existingScripts = Get-ChildItem -LiteralPath $serverScriptsDirectory -File
Write-Log "Clearing $($existingScripts.Count) existing file(s) from $serverScriptsDirectory"
$existingScripts | Remove-Item -Force

$positiveFixtures = Get-ChildItem -LiteralPath $positiveDirectory -File -Filter '*.js'
Write-Log "Copying $($positiveFixtures.Count) positive fixture(s) from $positiveDirectory"
$positiveFixtures | Copy-Item -Destination $serverScriptsDirectory
foreach ($fixture in $positiveFixtures) {
    Write-Log "  + $($fixture.Name)"
}

$expectedRecipeCount = $positiveFixtures.Count
Write-Log "Expecting $expectedRecipeCount recipe(s) to load" -Level OK

if ($PrepareOnly) {
    Write-Log "PrepareOnly requested - skipping server launch" -Level OK
    Write-Output "FIXTURES_READY recipes=$expectedRecipeCount directory=$serverScriptsDirectory"
    return
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
        [AllowEmptyString()]
        [string]$Text,
        [Parameter(Mandatory)]
        [int]$Offset
    )

    if ($Text.Length -le $Offset) {
        return ''
    }

    return $Text.Substring($Offset)
}

function Get-JsonBlockAfterMarker {
    param(
        [Parameter(Mandatory)]
        [AllowEmptyString()]
        [string]$Text,
        [Parameter(Mandatory)]
        [string]$Marker
    )

    $markerIndex = $Text.IndexOf($Marker)
    if ($markerIndex -lt 0) {
        return $null
    }

    $jsonStart = -1
    for ($i = $markerIndex + $Marker.Length; $i -lt $Text.Length; $i++) {
        if ($Text[$i] -eq '{' -or $Text[$i] -eq '[') {
            $jsonStart = $i
            break
        }
    }
    if ($jsonStart -lt 0) {
        return $null
    }

    $closeChar = if ($Text[$jsonStart] -eq '{') { '}' } else { ']' }
    $openChar = $Text[$jsonStart]
    $depth = 0
    $inString = $false
    $escapeNext = $false

    for ($i = $jsonStart; $i -lt $Text.Length; $i++) {
        $c = $Text[$i]

        if ($inString) {
            if ($escapeNext) {
                $escapeNext = $false
            } elseif ($c -eq '\') {
                $escapeNext = $true
            } elseif ($c -eq '"') {
                $inString = $false
            }
            continue
        }

        if ($c -eq '"') {
            $inString = $true
            continue
        }

        if ($c -eq $openChar) {
            $depth++
        } elseif ($c -eq $closeChar) {
            $depth--
            if ($depth -eq 0) {
                return $Text.Substring($jsonStart, $i - $jsonStart + 1)
            }
        }
    }

    return $null
}

$startupLogOffset = 0
if (Test-Path -LiteralPath $latestLog) {
    Write-Log "Attempting to remove stale log file at $latestLog"
    $deleted = $false
    for ($attempt = 1; $attempt -le 5 -and -not $deleted; $attempt++) {
        try {
            Remove-Item -LiteralPath $latestLog -Force -ErrorAction Stop
            $deleted = $true
        } catch {
            Write-Log "Attempt $attempt/5 to remove stale log failed (likely still locked by a leftover server process): $($_.Exception.Message)" -Level WARN
            if ($attempt -lt 5) {
                Start-Sleep -Seconds 1
            }
        }
    }

    if ($deleted) {
        Write-Log "Stale log file removed" -Level OK
    } else {
        Write-Log "Could not remove stale log file after 5 attempts - it is probably held open by a leftover Java/Forge process from a previous run. Check Task Manager for a stray java.exe and close it if this run fails to detect startup. Falling back to reading only new content appended after this point." -Level WARN
        $startupLogOffset = (Get-LatestLogText).Length
    }
}

Write-Log "Launching Gradle runServer (kubejs_version=$KubeJSVersion)" -Level STEP

$process = [System.Diagnostics.Process]::new()
$process.StartInfo = [System.Diagnostics.ProcessStartInfo]::new()
$process.StartInfo.FileName = $env:ComSpec
$process.StartInfo.Arguments = "/d /s /c `"gradlew.bat --no-daemon runServer -Pkubejs_version=$KubeJSVersion`""
$process.StartInfo.WorkingDirectory = $repoRoot
$process.StartInfo.UseShellExecute = $false
$process.StartInfo.CreateNoWindow = $true
$process.StartInfo.RedirectStandardInput = $true
$process.StartInfo.RedirectStandardOutput = $true
$process.StartInfo.RedirectStandardError = $true

if (-not $process.Start()) {
    Write-Log "Failed to start Gradle runServer process" -Level ERROR
    throw 'Failed to start Gradle runServer process.'
}

Write-Log "Gradle process started (PID $($process.Id))" -Level OK

$stdoutTask = $process.StandardOutput.ReadToEndAsync()
$stderrTask = $process.StandardError.ReadToEndAsync()

$stoppedCleanly = $false
$explicitReloadPassed = $false
$failure = $null
$negativeResults = [System.Collections.Generic.List[object]]::new()
$schemaExportResults = [System.Collections.Generic.List[object]]::new()

function Wait-ForCondition {
    param(
        [Parameter(Mandatory)]
        [scriptblock]$Condition,
        [Parameter(Mandatory)]
        [string]$Description,
        [Parameter(Mandatory)]
        [int]$TimeoutSeconds
    )

    Write-Log "Waiting for: $Description (timeout ${TimeoutSeconds}s)" -Level STEP
    $deadline = [DateTime]::UtcNow.AddSeconds($TimeoutSeconds)
    $lastTick = [DateTime]::UtcNow
    while ([DateTime]::UtcNow -lt $deadline) {
        if ($process.HasExited) {
            Write-Log "runServer exited before $Description (exit code $($process.ExitCode))" -Level ERROR
            throw "runServer exited before $Description (exit code $($process.ExitCode))."
        }

        if (& $Condition) {
            Write-Log "Condition met: $Description" -Level OK
            return
        }

        if (([DateTime]::UtcNow - $lastTick).TotalSeconds -ge 15) {
            $remaining = [int]($deadline - [DateTime]::UtcNow).TotalSeconds
            Write-Log "Still waiting for: $Description (${remaining}s remaining)"
            $lastTick = [DateTime]::UtcNow
        }

        Start-Sleep -Milliseconds 500
    }

    Write-Log "Timed out after $TimeoutSeconds seconds waiting for $Description" -Level ERROR
    throw "Timed out after $TimeoutSeconds seconds waiting for $Description."
}

function Send-ServerCommand {
    param(
        [Parameter(Mandatory)]
        [string]$Command
    )

    Write-Log "Sending server command: $Command"
    $process.StandardInput.WriteLine($Command)
    $process.StandardInput.Flush()
}

function Invoke-CleanReload {
    param(
        [Parameter(Mandatory)]
        [int]$RecipeCount
    )

    Write-Log "Triggering clean reload, expecting $RecipeCount recipe(s)" -Level STEP
    $offset = (Get-LatestLogText).Length
    Send-ServerCommand -Command 'reload'
    Wait-ForCondition -Description "clean reload with $RecipeCount recipes" -TimeoutSeconds $ReloadTimeoutSeconds -Condition {
        $tail = Get-TextTail -Text (Get-LatestLogText) -Offset $offset
        return $tail -match "Added $RecipeCount recipes.*with 0 failed recipes" -and
            $tail.Contains('Server resource reload complete!')
    }
}

$negativeExpectations = [ordered]@{
    '01_casting_too_many_outputs.js.disabled'          = [pscustomobject]@{ Message = "TFMG recipe 'casting' requires 1 item outputs, got 2"; FailedCounter = $true }
    '02_coking_missing_fluid_output.js.disabled'       = [pscustomobject]@{ Message = "TFMG recipe 'coking' requires 2 fluid outputs, got 1"; FailedCounter = $true }
    '03_distillation_too_many_outputs.js.disabled'     = [pscustomobject]@{ Message = "TFMG recipe 'distillation' requires 1..6 fluid outputs, got 7"; FailedCounter = $true }
    '04_industrial_blasting_too_many_inputs.js.disabled' = [pscustomobject]@{ Message = "TFMG recipe 'industrial_blasting' requires 1..2 item inputs, got 3"; FailedCounter = $true }
    '05_polarizing_legacy_energy.js.disabled'          = [pscustomobject]@{ Message = 'Constructor for tfmg:polarizing with 3 arguments not found!'; FailedCounter = $true }
    '06_winding_missing_input.js.disabled'             = [pscustomobject]@{ Message = "TFMG recipe 'winding' requires 2 item inputs, got 1"; FailedCounter = $true }
    '07_hot_blast_wrong_input_type.js.disabled'        = [pscustomobject]@{ Message = "TFMG recipe 'hot_blast' requires 0 item inputs, got 1"; FailedCounter = $true }
    '08_vat_empty.js.disabled'                         = [pscustomobject]@{ Message = "TFMG recipe 'vat_machine_recipe' requires at least one input"; FailedCounter = $true }
    '10_non_positive_time.js.disabled'                 = [pscustomobject]@{ Message = 'TFMG processing time must be greater than zero'; FailedCounter = $true }
    '11_unsupported_heat.js.disabled'                  = [pscustomobject]@{ Message = "TFMG recipe 'casting' does not support .heated() or .superheated()"; FailedCounter = $false }
}

try {
    Wait-ForCondition -Description 'dedicated server startup' -TimeoutSeconds $StartupTimeoutSeconds -Condition {
        $tail = Get-TextTail -Text (Get-LatestLogText) -Offset $startupLogOffset
        return $tail.Contains('Done (') -and
            $tail -match "Added $expectedRecipeCount recipes.*with 0 failed recipes"
    }
    Write-Log "Server started successfully with $expectedRecipeCount recipe(s) loaded" -Level OK

    Invoke-CleanReload -RecipeCount $expectedRecipeCount
    $explicitReloadPassed = $true
    Write-Log "Explicit clean reload passed" -Level OK

    if ($ExportSchemas) {
        Write-Log "Exporting recipe schemas" -Level STEP
        $schemaExportDirectory = Join-Path $resultsDirectory 'schemas'
        New-Item -ItemType Directory -Force -Path $schemaExportDirectory | Out-Null

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

            $tail = Get-TextTail -Text (Get-LatestLogText) -Offset $offset
            $jsonText = Get-JsonBlockAfterMarker -Text $tail -Marker "JSON of ${schemaId}:"
            $schemaFileName = "$($schemaId.Replace(':', '_')).json"
            $schemaFilePath = Join-Path $schemaExportDirectory $schemaFileName

            if ($jsonText) {
                $isInaccurate = $tail.Substring($tail.IndexOf("JSON of ${schemaId}:"), [Math]::Min(200, $tail.Length - $tail.IndexOf("JSON of ${schemaId}:"))).Contains('May be inaccurate')
                try {
                    # Validate it's real JSON without letting ConvertTo-Json mangle the formatting -
                    # it re-indents nested values to their parent's column, which is unreadable.
                    $null = $jsonText | ConvertFrom-Json -ErrorAction Stop

                    $schemaLines = $jsonText -replace "`r`n", "`n" -split "`n"
                    for ($i = 1; $i -lt $schemaLines.Count; $i++) {
                        $schemaLines[$i] = "    $($schemaLines[$i])"
                    }
                    $indentedSchema = $schemaLines -join "`n"

                    $fileContent = @"
{
    "schemaId": "$schemaId",
    "mayBeInaccurate": $($isInaccurate.ToString().ToLower()),
    "schema": $indentedSchema
}
"@
                    Set-Content -Encoding UTF8 -LiteralPath $schemaFilePath -Value $fileContent
                    Write-Log "Schema exported: $schemaId -> $schemaFilePath" -Level OK
                } catch {
                    [System.IO.File]::WriteAllText($schemaFilePath, $jsonText)
                    Write-Log "Schema exported: $schemaId -> $schemaFilePath (written raw, could not re-format: $($_.Exception.Message))" -Level WARN
                }
                $schemaExportResults.Add([pscustomobject]@{ schemaId = $schemaId; path = $schemaFilePath })
            } else {
                Write-Log "Schema command succeeded for $schemaId but no JSON block could be located in the log output - nothing written" -Level WARN
                $schemaExportResults.Add([pscustomobject]@{ schemaId = $schemaId; path = $null })
            }
        }
        Write-Log "Exported $($schemaExportResults.Count) schema(s) to $schemaExportDirectory" -Level OK
    } else {
        Write-Log "ExportSchemas not requested - skipping"
    }

    if (-not $SkipNegative) {
        Write-Log "Running $($negativeExpectations.Count) negative fixture(s)" -Level STEP
        $negativeIndex = 0
        foreach ($entry in $negativeExpectations.GetEnumerator()) {
            $negativeIndex++
            Write-Log "[$negativeIndex/$($negativeExpectations.Count)] Testing fixture: $($entry.Key)" -Level STEP

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
                fixture       = $entry.Key
                expected      = $entry.Value.Message
                failedCounter = $entry.Value.FailedCounter
                passed        = $true
            })
            Write-Log "Fixture rejected as expected: $($entry.Key)" -Level OK

            Remove-Item -LiteralPath $destination -Force
            Invoke-CleanReload -RecipeCount $expectedRecipeCount
        }
        Write-Log "All $($negativeResults.Count) negative fixture(s) passed" -Level OK
    } else {
        Write-Log "SkipNegative requested - skipping negative fixtures"
    }

    Write-Log "Sending stop command to server" -Level STEP
    Send-ServerCommand -Command 'stop'
    if (-not $process.WaitForExit(60000)) {
        Write-Log "Server did not stop within 60 seconds after the stop command" -Level ERROR
        throw 'Server did not stop within 60 seconds after the stop command.'
    }
    $stoppedCleanly = $true
    Write-Log "Server stopped cleanly (exit code $($process.ExitCode))" -Level OK

    if ($process.ExitCode -ne 0) {
        throw "Gradle runServer exited with code $($process.ExitCode)."
    }
} catch {
    $failure = $_
    Write-Log "Run failed: $($failure.Exception.Message)" -Level ERROR
} finally {
    Write-Log "Cleaning up" -Level STEP
    if (-not $process.HasExited) {
        Write-Log "Server process still running - forcing shutdown" -Level WARN
        try {
            Send-ServerCommand -Command 'stop'
            if (-not $process.WaitForExit(30000)) {
                Write-Log "Server did not respond to stop - killing process" -Level WARN
                $process.Kill()
                $process.WaitForExit(10000) | Out-Null
            }
        } catch {
            if (-not $process.HasExited) {
                Write-Log "Force-killing server process after error: $($_.Exception.Message)" -Level WARN
                $process.Kill()
            }
        }
    }

    $stdout = $stdoutTask.GetAwaiter().GetResult()
    $stderr = $stderrTask.GetAwaiter().GetResult()

    $resultBaseName = "neoforge-$($neoForgeVersion.Replace('.', '_'))-$($KubeJSVersion.Replace('.', '_'))"
    $gradleLogPath = Join-Path $resultsDirectory "$resultBaseName-gradle.log"
    [System.IO.File]::WriteAllText($gradleLogPath, $stdout + [Environment]::NewLine + $stderr)
    Write-Log "Gradle stdout/stderr written to $gradleLogPath"

    if (Test-Path -LiteralPath $latestLog) {
        $copiedLogPath = Join-Path $resultsDirectory "$resultBaseName-latest.log"
        Copy-Item -LiteralPath $latestLog -Destination $copiedLogPath -Force
        Write-Log "Server log copied to $copiedLogPath"
    }

    $elapsed = [DateTime]::Now - $scriptStartedAt

    $summary = [ordered]@{
        neoforge         = $neoForgeVersion
        kubejs           = $KubeJSVersion
        expectedRecipes  = $expectedRecipeCount
        explicitReload   = $explicitReloadPassed
        schemaExports    = @($schemaExportResults)
        negativeFixtures = @($negativeResults)
        stoppedCleanly   = $stoppedCleanly
        exitCode         = if ($process.HasExited) { $process.ExitCode } else { $null }
        elapsed          = ('{0:hh\:mm\:ss}' -f $elapsed)
        passed           = $failure -eq $null
        failure          = if ($failure) { $failure.Exception.Message } else { $null }
    }

    $summaryPath = Join-Path $resultsDirectory "$resultBaseName-summary.json"
    $summary | ConvertTo-Json -Depth 5 | Set-Content -Encoding UTF8 -LiteralPath $summaryPath
    Write-Log "Summary written to $summaryPath"

    function Write-SummaryLine {
        param([string]$Label, [string]$Value, [string]$Color = 'White')
        Write-Host ("  {0,-18} " -f $Label) -NoNewline -ForegroundColor DarkGray
        Write-Host $Value -ForegroundColor $Color
    }

    $ruleColor = if ($summary.passed) { 'Green' } else { 'Red' }
    $rule = '=' * 56

    Write-Host ''
    Write-Host $rule -ForegroundColor $ruleColor
    Write-Host '  TEST SUMMARY' -ForegroundColor $ruleColor
    Write-Host $rule -ForegroundColor $ruleColor

    Write-SummaryLine -Label 'Result' -Value $(if ($summary.passed) { 'PASSED' } else { 'FAILED' }) -Color $(if ($summary.passed) { 'Green' } else { 'Red' })
    Write-SummaryLine -Label 'NeoForge' -Value $summary.neoforge
    Write-SummaryLine -Label 'KubeJS' -Value $summary.kubejs
    Write-SummaryLine -Label 'Recipes loaded' -Value "$($summary.expectedRecipes)/$($summary.expectedRecipes)"
    Write-SummaryLine -Label 'Explicit reload' -Value $(if ($summary.explicitReload) { 'OK' } else { 'NOT RUN' }) -Color $(if ($summary.explicitReload) { 'Green' } else { 'Yellow' })
    Write-SummaryLine -Label 'Stopped cleanly' -Value "$($summary.stoppedCleanly) (exit code $($summary.exitCode))" -Color $(if ($summary.stoppedCleanly) { 'Green' } else { 'Yellow' })
    Write-SummaryLine -Label 'Total time' -Value $summary.elapsed

    if ($schemaExportResults.Count -gt 0) {
        Write-Host ''
        Write-Host "  Schema exports ($($schemaExportResults.Count)):" -ForegroundColor DarkGray
        foreach ($schemaResult in $schemaExportResults) {
            if ($schemaResult.path) {
                Write-Host "    OK  $($schemaResult.schemaId)" -ForegroundColor Green
                Write-Host "        $($schemaResult.path)" -ForegroundColor DarkGray
            } else {
                Write-Host "    !!  $($schemaResult.schemaId) (no file written - see log)" -ForegroundColor Yellow
            }
        }
    }

    if ($negativeResults.Count -gt 0) {
        Write-Host ''
        Write-Host "  Negative fixtures ($($negativeResults.Count) passed):" -ForegroundColor DarkGray
        foreach ($result in $negativeResults) {
            Write-Host "    OK  $($result.fixture)" -ForegroundColor Green
            Write-Host "        expected: $($result.expected)" -ForegroundColor DarkGray
        }
    }

    if ($summary.failure) {
        Write-Host ''
        Write-Host "  Failure: $($summary.failure)" -ForegroundColor Red
    }

    Write-Host $rule -ForegroundColor $ruleColor
    Write-Host ''
}

if ($failure) {
    Write-Log "Exiting with failure" -Level ERROR
    throw $failure
}

Write-Log "All checks passed" -Level OK
Write-Output "RUNTIME_OK neoforge=$neoForgeVersion kubejs=$KubeJSVersion recipes=$expectedRecipeCount schemas=$($schemaExportResults.Count) negatives=$($negativeResults.Count)"