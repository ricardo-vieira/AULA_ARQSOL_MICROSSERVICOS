[CmdletBinding()]
param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$MvnArgs
)

if (-not $env:JAVA_HOME) {
    if (Test-Path "C:\Program Files\Java\jdk-25") {
        $env:JAVA_HOME = "C:\Program Files\Java\jdk-25"
    }
}

$localMvn = Join-Path $PSScriptRoot "..\tools\apache-maven-3.9.6\bin\mvn.cmd"
if (Test-Path $localMvn) {
    & $localMvn @MvnArgs
} else {
    & mvn @MvnArgs
}
