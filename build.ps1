# PowerShell Build Script for Void Harbinger Mod
$Host.UI.RawUI.WindowTitle = "Building Void Harbinger Mod (Fabric 1.20.1)"

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "         BUILDING THE VOID HARBINGER (FABRIC 1.20.1)" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host ""

# Check Java
$javaCmd = Get-Command java -ErrorAction SilentlyContinue
if (-not $javaCmd) {
    Write-Host "[INFO] Searching for Java 17 on your PC..." -ForegroundColor Yellow
    $searchPaths = @(
        "$env:ProgramFiles\Eclipse Adoptium",
        "$env:ProgramFiles\Microsoft",
        "$env:ProgramFiles\Java",
        "$env:LOCALAPPDATA\Programs\Eclipse Adoptium",
        "$env:APPDATA\PrismLauncher\java",
        "$env:ProgramFiles(x86)\Minecraft Launcher\runtime"
    )
    foreach ($base in $searchPaths) {
        if (Test-Path $base) {
            $found = Get-ChildItem -Path $base -Filter "java.exe" -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1
            if ($found) {
                $binDir = $found.Directory.FullName
                $env:PATH = "$binDir;$env:PATH"
                $env:JAVA_HOME = $found.Directory.Parent.FullName
                Write-Host "[FOUND] Java: $binDir" -ForegroundColor Green
                break
            }
        }
    }
}

if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    Write-Host ""
    Write-Host "[ERROR] Java 17 not found!" -ForegroundColor Red
    Write-Host "Please install JDK 17 from: https://adoptium.net" -ForegroundColor Yellow
    Write-Host ""
    Read-Host "Press Enter to exit..."
    exit 1
}

Write-Host "[OK] Java found:" -ForegroundColor Green
& java -version
Write-Host ""

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
if (-not $projectRoot) { $projectRoot = Get-Location }
Set-Location $projectRoot

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "[1/2] Compiling mod with Gradle..." -ForegroundColor Cyan
Write-Host "Downloading mappings and dependencies (first run takes 1-3 min)..." -ForegroundColor Gray
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host ""

& ".\gradlew.bat" build --stacktrace

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "======================================================================" -ForegroundColor Green
    Write-Host "[SUCCESS] Mod built successfully!" -ForegroundColor Green
    $jarPath = Join-Path $projectRoot "build\libs\void_harbinger-1.0.0.jar"
    $readyJar = Join-Path $projectRoot "void_harbinger-1.0.0-READY.jar"
    
    if (Test-Path $jarPath) {
        Copy-Item -Path $jarPath -Destination $readyJar -Force
        Write-Host ""
        Write-Host "Created mod jar:" -ForegroundColor Yellow
        Write-Host "  -> $readyJar" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "Copy void_harbinger-1.0.0-READY.jar into your PrismLauncher mods folder!" -ForegroundColor Green
    }
    Write-Host "======================================================================" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "======================================================================" -ForegroundColor Red
    Write-Host "[ERROR] Build failed with exit code $LASTEXITCODE" -ForegroundColor Red
    Write-Host "Please ensure you have an active internet connection." -ForegroundColor Yellow
    Write-Host "======================================================================" -ForegroundColor Red
}

Write-Host ""
Read-Host "Press Enter to exit..."
