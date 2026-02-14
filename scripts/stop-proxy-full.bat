@echo off
REM Artemis Proxy Service - Enhanced Windows Stop Script

setlocal enabledelayedexpansion

set PORT=
set ALL=

:parse_args
if "%~1"=="" goto end_parse
if /i "%~1"=="-p" (set PORT=%~2& shift& shift& goto parse_args)
if /i "%~1"=="--port" (set PORT=%~2& shift& shift& goto parse_args)
if /i "%~1"=="-a" (set ALL=true& shift& goto parse_args)
if /i "%~1"=="--all" (set ALL=true& shift& goto parse_args)
if /i "%~1"=="-h" goto show_help
if /i "%~1"=="--help" goto show_help
if /i "%~1"=="/?" goto show_help
echo Unknown option: %~1
goto show_help

:end_parse

if "%ALL%"=="true" (
    echo Stopping all Artemis Proxy instances...
    for /f "tokens=2" %%a in ('tasklist /FI "IMAGENAME eq java.exe" /FO LIST ^| find "PID:"') do (
        wmic process where "ProcessId=%%a" get CommandLine | find "artemis-proxy" >nul
        if not errorlevel 1 (
            echo Stopping proxy (PID: %%a^)
            taskkill /PID %%a /F >nul 2>&1
        )
    )
    echo All proxy instances stopped
    pause
    exit /b 0
)

if not "%PORT%"=="" (
    echo Stopping Artemis Proxy on port %PORT%...
) else (
    echo Stopping Artemis Proxy...
)

set FOUND=0
for /f "tokens=2" %%a in ('tasklist /FI "IMAGENAME eq java.exe" /FO LIST ^| find "PID:"') do (
    wmic process where "ProcessId=%%a" get CommandLine | find "artemis-proxy" >nul
    if not errorlevel 1 (
        echo Stopping proxy process (PID: %%a^)
        taskkill /PID %%a /F >nul 2>&1
        set FOUND=1
    )
)

if %FOUND%==1 (
    echo Artemis Proxy stopped successfully
) else (
    echo No Artemis Proxy instances found
)

echo.
pause
exit /b 0

:show_help
echo Usage: %~nx0 [OPTIONS]
echo.
echo Options:
echo   -p, --port PORT    Stop proxy on specific port
echo   -a, --all          Stop all proxy instances
echo   -h, --help         Show this help
echo.
echo Examples:
echo   %~nx0              # Stop any running proxy
echo   %~nx0 -p 2011      # Stop proxy on port 2011
echo   %~nx0 --all        # Stop all proxies
echo.
pause
exit /b 0
