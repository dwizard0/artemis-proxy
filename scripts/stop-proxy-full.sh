#!/bin/bash
# Artemis Proxy Service - Enhanced Stop Script (supports multiple instances)

PROXY_DIR="/media/storage/Users/freeman/artemis-kiosk/Artemis Bridge Stations/artemis-proxy"

# Parse arguments
PORT=""
ALL=false

show_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -p, --port PORT    Stop proxy on specific port"
    echo "  -a, --all          Stop all proxy instances"
    echo "  -h, --help         Show this help"
    echo ""
    echo "Examples:"
    echo "  $0                 # Stop default proxy (port 2010)"
    echo "  $0 -p 2011         # Stop proxy on port 2011"
    echo "  $0 --all           # Stop all proxies"
    exit 0
}

while [[ $# -gt 0 ]]; do
    case $1 in
        -p|--port) PORT="$2"; shift 2 ;;
        -a|--all) ALL=true; shift ;;
        -h|--help) show_usage ;;
        *) echo "Unknown option: $1"; show_usage ;;
    esac
done

cd "$PROXY_DIR" || exit 1

if [ "$ALL" = true ]; then
    echo "Stopping all Artemis Proxy instances..."
    pkill -f "artemis-proxy.*jar"
    rm -f proxy*.pid
    echo "✓ All proxy instances stopped"
    exit 0
fi

# Determine PID file
if [ -n "$PORT" ]; then
    PID_FILE="proxy-${PORT}.pid"
else
    PID_FILE="proxy.pid"
fi

if [ ! -f "$PID_FILE" ]; then
    echo "No proxy running (no PID file: $PID_FILE)"
    # Try to kill by name anyway
    pkill -f "artemis-proxy.*jar" && echo "Killed proxy by process name"
    exit 0
fi

PID=$(cat "$PID_FILE")

if ps -p "$PID" > /dev/null 2>&1; then
    echo "Stopping Artemis Proxy (PID: $PID)..."
    kill "$PID"
    sleep 2
    
    if ps -p "$PID" > /dev/null 2>&1; then
        echo "Force stopping..."
        kill -9 "$PID"
    fi
    
    rm -f "$PID_FILE"
    echo "✓ Artemis Proxy stopped"
else
    echo "Proxy was not running"
    rm -f "$PID_FILE"
fi
