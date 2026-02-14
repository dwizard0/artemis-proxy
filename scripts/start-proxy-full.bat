@echo off
REM Artemis Proxy Service - Full-Featured Windows Startup Script

setlocal enabledelayedexpansion

set PROXY_DIR=\\192.168.1.200\artemis\Artemis Bridge Stations\artemis-proxy
set JAR_FILE=target\artemis-proxy-1.0-SNAPSHOT-jar-with-dependencies.jar
set CONFIG_FILE=proxy-config.properties

REM Default values
set DEFAULT_SERVER=192.168.1.209
set DEFAULT_SERVER_PORT=2010
set DEFAULT_LISTEN_PORT=2010
set DEFAULT_SHIP=0
set DEFAULT_UDP_TARGETS=192.168.1.209
set DEFAULT_UDP_PORT=50200
set DEFAULT_WEB_PORT=81
set DEFAULT_JSON_FILE=artemis_proxy_status.json
set DEFAULT_LOG_FILE=proxy.log

REM Current values
set SERVER_IP=%DEFAULT_SERVER%
set SERVER_PORT=%DEFAULT_SERVER_PORT%
set LISTEN_PORT=%DEFAULT_LISTEN_PORT%
set SHIP_NUMBER=%DEFAULT_SHIP%
set UDP_TARGETS=%DEFAULT_UDP_TARGETS%
set UDP_PORT=%DEFAULT_UDP_PORT%
set WEB_PORT=%DEFAULT_WEB_PORT%
set JSON_FILE=%DEFAULT_JSON_FILE%
set LOG_FILE=%DEFAULT_LOG_FILE%
set VERBOSE=

:parse_args
if "%~1"=="" goto end_parse

if /i "%~1"=="-s" (set SERVER_IP=%~2& shift& shift& goto parse_args)
if /i "%~1"=="--server" (set SERVER_IP=%~2& shift& shift& goto parse_args)
if /i "%~1"=="-p" (set SERVER_PORT=%~2& shift& shift& goto parse_args)
if /i "%~1"=="--port" (set SERVER_PORT=%~2& shift& shift& goto parse_args)
if /i "%~1"=="-l" (set LISTEN_PORT=%~2& shift& shift& goto parse_args)
if /i "%~1"=="--listen" (set LISTEN_PORT=%~2& shift& shift& goto parse_args)
if /i "%~1"=="-n" (set SHIP_NUMBER=%~2& shift& shift& goto parse_args)
if /i "%~1"=="--ship" (set SHIP_NUMBER=%~2& shift& shift& goto parse_args)
if /i "%~1"=="-u" (set UDP_TARGETS=%~2& shift& shift& goto parse_args)
if /i "%~1"=="--udp" (set UDP_TARGETS=%~2& shift& shift& goto parse_args)
if /i "%~1"=="--udp-port" (set UDP_PORT=%~2& shift& shift& goto parse_args)
if /i "%~1"=="-j" (set JSON_FILE=%~2& shift& shift& goto parse_args)
if /i "%~1"=="--json" (set JSON_FILE=%~2& shift& shift& goto parse_args)
if /i "%~1"=="-w" (set WEB_PORT=%~2& shift& shift& goto parse_args)
if /i "%~1"=="--web-port" (set WEB_PORT=%~2& shift& shift& goto parse_args)
if /i "%~1"=="--log" (set LOG_FILE=%~2& shift& shift& goto parse_args)
if /i "%~1"=="-v" (set VERBOSE=true& shift& goto parse_args)
if /i "%~1"=="--verbose" (set VERBOSE=true& shift& goto parse_args)
if /i "%~1"=="-h" goto show_help
if /i "%~1"=="--help" goto show_help
if /i "%~1"=="/?" goto show_help

echo Error: Unknown option: %~1
echo Run with --help for usage information
pause
exit /b 1

:end_parse

REM Validate ship number
if %SHIP_NUMBER% LSS 0 goto invalid_ship
if %SHIP_NUMBER% GTR 7 goto invalid_ship
goto ship_valid

:invalid_ship
echo Error: Ship number must be 0-7
pause
exit /b 1

:ship_valid

REM Display configuration
echo.
echo ========================================================
echo    Artemis Proxy - Starting Configuration
echo ========================================================
echo   Server:      %SERVER_IP%:%SERVER_PORT%
echo   Listen Port: %LISTEN_PORT%
echo   Ship:        %SHIP_NUMBER%
echo   UDP Targets: %UDP_TARGETS%
echo   UDP Port:    %UDP_PORT%
echo   JSON File:   %JSON_FILE%
echo   Web Port:    %WEB_PORT%
echo   Log File:    %LOG_FILE%
if "%VERBOSE%"=="true" echo   Verbose:     ENABLED
echo ========================================================
echo.

REM Change to proxy directory
pushd "%PROXY_DIR%"
if errorlevel 1 (
    echo ERROR: Could not access proxy directory
    echo Please ensure network share is accessible: %PROXY_DIR%
    pause
    exit /b 1
)

REM Check Java
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java is not installed or not in PATH
    pause
    popd
    exit /b 1
)

REM Create UDP targets file
(
echo # UDP Targets - Auto-generated
for %%t in (%UDP_TARGETS:,= %) do echo %%t
) > udp_targets.txt

REM Create configuration file
(
echo # Artemis Proxy Configuration
echo # Generated: %date% %time%
echo upstream.host=%SERVER_IP%
echo upstream.port=%SERVER_PORT%
echo proxy.port=%LISTEN_PORT%
echo target.ship=%SHIP_NUMBER%
echo udp.port=%UDP_PORT%
echo web.port=%WEB_PORT%
echo json.file=%JSON_FILE%
) > "%CONFIG_FILE%"

REM Build Java options
set JAVA_OPTS=-Dconfig.file=%CONFIG_FILE%
if "%VERBOSE%"=="true" set JAVA_OPTS=%JAVA_OPTS% -Dorg.slf4j.simpleLogger.defaultLogLevel=debug

REM Start the proxy
echo Starting proxy...
start "Artemis Proxy [%LISTEN_PORT%]" /MIN java %JAVA_OPTS% -jar "%JAR_FILE%"

timeout /t 3 /nobreak >nul

echo.
echo Artemis Proxy Service started
echo ================================
echo   Connect to: 192.168.1.200:%LISTEN_PORT%
echo   Log file:   %PROXY_DIR%\%LOG_FILE%
echo   Stop:       Close "Artemis Proxy [%LISTEN_PORT%]" window
echo ================================
echo.

popd
pause
exit /b 0

:show_help
echo Artemis Proxy - Full Configuration Script
echo.
echo Usage: %~nx0 [OPTIONS]
echo.
echo Server Options:
echo   -s, --server IP         Artemis server IP (default: %DEFAULT_SERVER%)
echo   -p, --port PORT         Artemis server port (default: %DEFAULT_SERVER_PORT%)
echo   -l, --listen PORT       Proxy listen port (default: %DEFAULT_LISTEN_PORT%)
echo   -n, --ship NUMBER       Target ship number 0-7 (default: %DEFAULT_SHIP%)
echo.
echo Output Options:
echo   -u, --udp IPs           UDP target IPs, comma-separated (default: %DEFAULT_UDP_TARGETS%)
echo       --udp-port PORT     UDP broadcast port (default: %DEFAULT_UDP_PORT%)
echo   -j, --json FILE         JSON output filename (default: %DEFAULT_JSON_FILE%)
echo   -w, --web-port PORT     Web config port (default: %DEFAULT_WEB_PORT%)
echo.
echo Logging Options:
echo       --log FILE          Log filename (default: %DEFAULT_LOG_FILE%)
echo   -v, --verbose           Enable verbose logging
echo.
echo Other:
echo   -h, --help              Show this help message
echo.
echo Examples:
echo   %~nx0
echo     Start with all defaults
echo.
echo   %~nx0 -s 192.168.1.100 -n 2
echo     Custom server and ship 2
echo.
echo   %~nx0 -s 192.168.1.100 -l 2011 -u 192.168.1.200,192.168.1.201
echo     Custom server, listen on 2011, broadcast to 2 IPs
echo.
echo   %~nx0 -n 1 -j artemis_ship1.json --log proxy_ship1.log
echo     Track ship 1 with custom output files
echo.
echo   %~nx0 --verbose
echo     Enable debug logging
echo.
pause
exit /b 0
