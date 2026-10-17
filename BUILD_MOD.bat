@echo off
setlocal
cd /d "%~dp0"
title Void Harbinger Mod Build

echo ========================================================
echo   Building Void Harbinger Mod for Fabric 1.20.1
echo ========================================================
echo.

:: 1. Check if Java is in PATH
where java >nul 2>nul
if %ERRORLEVEL% equ 0 goto start_build

:: 2. Search common Java 17 locations
echo [INFO] Searching for Java 17 on your system...
if exist "%ProgramFiles%\Eclipse Adoptium" (
    for /d %%D in ("%ProgramFiles%\Eclipse Adoptium\jdk-17*") do if exist "%%D\bin\java.exe" set "PATH=%%D\bin;%PATH%"
)
if exist "%ProgramFiles%\Microsoft" (
    for /d %%D in ("%ProgramFiles%\Microsoft\jdk-17*") do if exist "%%D\bin\java.exe" set "PATH=%%D\bin;%PATH%"
)
if exist "%ProgramFiles%\Java" (
    for /d %%D in ("%ProgramFiles%\Java\jdk-17*") do if exist "%%D\bin\java.exe" set "PATH=%%D\bin;%PATH%"
)
if exist "%LOCALAPPDATA%\Programs\Eclipse Adoptium" (
    for /d %%D in ("%LOCALAPPDATA%\Programs\Eclipse Adoptium\jdk-17*") do if exist "%%D\bin\java.exe" set "PATH=%%D\bin;%PATH%"
)

where java >nul 2>nul
if %ERRORLEVEL% equ 0 goto start_build

echo [ERROR] Java 17 was not found in PATH or standard directories!
echo Please install JDK 17 from: https://adoptium.net/temurin/releases/?version=17
echo.
pause
exit /b 1

:start_build
echo [OK] Java detected:
java -version
echo.

if not exist "%~dp0gradle\wrapper\gradle-wrapper.jar" (
    echo [ERROR] gradle-wrapper.jar is missing!
    echo Please make sure you extracted the entire zip file.
    pause
    exit /b 1
)

echo ========================================================
echo [1/2] Compiling mod with Gradle Loom...
echo Downloading Fabric 1.20.1 mappings and GeckoLib (1-3 min)...
echo ========================================================
echo.

call "%~dp0gradlew.bat" build --stacktrace

if %ERRORLEVEL% neq 0 (
    echo.
    echo ========================================================
    echo [ERROR] Build failed with exit code %ERRORLEVEL%.
    echo Please ensure you have an active internet connection.
    echo ========================================================
    echo.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo ========================================================
echo [2/2] Checking build output...
echo ========================================================

if exist "%~dp0build\libs\void_harbinger-1.0.0.jar" (
    copy /y "%~dp0build\libs\void_harbinger-1.0.0.jar" "%~dp0void_harbinger-1.0.0-READY.jar" >nul
    echo.
    echo ========================================================
    echo [SUCCESS] Mod built successfully!
    echo Created file: void_harbinger-1.0.0-READY.jar
    echo.
    echo Copy void_harbinger-1.0.0-READY.jar into your PrismLauncher mods folder.
    echo ========================================================
) else (
    echo [WARNING] Please check build\libs folder for compiled jar.
)

echo.
pause
