#!/bin/bash
# Artemis Proxy Service - Full-Featured Linux Startup Script

PROXY_DIR="/media/storage/Users/freeman/artemis-kiosk/Artemis Bridge Stations/artemis-proxy"
JAR_FILE="target/artemis-proxy-1.0-SNAPSHOT-jar-with-dependencies.jar"
CONFIG_FILE="proxy-config.properties"

# Default values
DEFAULT_SERVER="192.168.1.209"
DEFAULT_SERVER_PORT="2010"
DEFAULT_LISTEN_PORT="2010"
DEFAULT_SHIP="0"
DEFAULT_UDP_TARGETS="192.168.1.209"
DEFAULT_UDP_PORT="50200"
DEFAULT_WEB_PORT="81"
DEFAULT_JSON_FILE="artemis_proxy_status.json"
DEFAULT_LOG_FILE="proxy.log"
DEFAULT_PID_FILE="proxy.pid"

# Current values
SERVER_IP="$DEFAULT_SERVER"
SERVER_PORT="$DEFAULT_SERVER_PORT"
LISTEN_PORT="$DEFAULT_LISTEN_PORT"
SHIP_NUMBER="$DEFAULT_SHIP"
UDP_TARGETS="$DEFAULT_UDP_TARGETS"
UDP_PORT="$DEFAULT_UDP_PORT"
WEB_PORT="$DEFAULT_WEB_PORT"
JSON_FILE="$DEFAULT_JSON_FILE"
LOG_FILE="$DEFAULT_LOG_FILE"
PID_FILE="$DEFAULT_PID_FILE"
VERBOSE=""

show_usage() {
    echo "Artemis Proxy - Full Configuration Script"
    echo ""
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Server Options:"
    echo "  -s, --server IP         Artemis server IP (default: $DEFAULT_SERVER)"
    echo "  -p, --port PORT         Artemis server port (default: $DEFAULT_SERVER_PORT)"
    echo "  -l, --listen PORT       Proxy listen port (default: $DEFAULT_LISTEN_PORT)"
    echo "  -n, --ship NUMBER       Target ship number 0-7 (default: $DEFAULT_SHIP)"
    echo ""
    echo "Output Options:"
    echo "  -u, --udp IPs           UDP target IPs, comma-separated (default: $DEFAULT_UDP_TARGETS)"
    echo "      --udp-port PORT     UDP broadcast port (default: $DEFAULT_UDP_PORT)"
    echo "  -j, --json FILE         JSON output filename (default: $DEFAULT_JSON_FILE)"
    echo "  -w, --web-port PORT     Web config port (default: $DEFAULT_WEB_PORT)"
    echo ""
    echo "Logging Options:"
    echo "      --log FILE          Log filename (default: $DEFAULT_LOG_FILE)"
    echo "  -v, --verbose           Enable verbose logging"
    echo ""
    echo "Other:"
    echo "  -h, --help              Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0"
    echo "    Start with all defaults"
    echo ""
    echo "  $0 -s 192.168.1.100 -n 2"
    echo "    Custom server and ship 2"
    echo ""
    echo "  $0 -s 192.168.1.100 -l 2011 -u 192.168.1.200,192.168.1.201"
    echo "    Custom server, listen on 2011, broadcast to 2 IPs"
    echo ""
    echo "  $0 -n 1 -j artemis_ship1.json --log proxy_ship1.log"
    echo "    Track ship 1 with custom output files"
    echo ""
    echo "  $0 --verbose"
    echo "    Enable debug logging"
    exit 0
}

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -s|--server) SERVER_IP="$2"; shift 2 ;;
        -p|--port) SERVER_PORT="$2"; shift 2 ;;
        -l|--listen) LISTEN_PORT="$2"; shift 2 ;;
        -n|--ship) SHIP_NUMBER="$2"; shift 2 ;;
        -u|--udp) UDP_TARGETS="$2"; shift 2 ;;
        --udp-port) UDP_PORT="$2"; shift 2 ;;
        -j|--json) JSON_FILE="$2"; shift 2 ;;
        -w|--web-port) WEB_PORT="$2"; shift 2 ;;
        --log) LOG_FILE="$2"; shift 2 ;;
        -v|--verbose) VERBOSE="true"; shift ;;
        -h|--help) show_usage ;;
        *)
            echo "Error: Unknown option: $1"
            echo "Run with --help for usage information"
            exit 1
            ;;
    esac
done

# Validate ship number
if ! [[ "$SHIP_NUMBER" =~ ^[0-7]$ ]]; then
    echo "Error: Ship number must be 0-7"
    exit 1
fi

cd "$PROXY_DIR" || exit 1

# Generate PID filename based on listen port (for multiple instances)
if [ "$LISTEN_PORT" != "$DEFAULT_LISTEN_PORT" ]; then
    PID_FILE="proxy-${LISTEN_PORT}.pid"
fi

# Check if already running
if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if ps -p "$PID" > /dev/null 2>&1; then
        echo "Error: Artemis Proxy already running on this port (PID: $PID)"
        echo "Stop it first: sudo kill $PID"
        exit 1
    fi
    rm -f "$PID_FILE"
fi

# Create UDP targets file
echo "# UDP Targets - Auto-generated" > udp_targets.txt
IFS=',' read -ra TARGETS <<< "$UDP_TARGETS"
for target in "${TARGETS[@]}"; do
    echo "$target" >> udp_targets.txt
done

# Create configuration file
cat > "$CONFIG_FILE" << EOF
# Artemis Proxy Configuration
# Generated: $(date)
upstream.host=$SERVER_IP
upstream.port=$SERVER_PORT
proxy.port=$LISTEN_PORT
target.ship=$SHIP_NUMBER
udp.port=$UDP_PORT
web.port=$WEB_PORT
json.file=$JSON_FILE
EOF

# Display configuration
echo "╔════════════════════════════════════════════════════╗"
echo "║   Artemis Proxy - Starting Configuration          ║"
echo "╠════════════════════════════════════════════════════╣"
echo "║  Server:      $SERVER_IP:$SERVER_PORT"
echo "║  Listen Port: $LISTEN_PORT"
echo "║  Ship:        $SHIP_NUMBER"
echo "║  UDP Targets: $UDP_TARGETS"
echo "║  UDP Port:    $UDP_PORT"
echo "║  JSON File:   $JSON_FILE"
echo "║  Web Port:    $WEB_PORT"
echo "║  Log File:    $LOG_FILE"
if [ "$VERBOSE" = "true" ]; then
echo "║  Verbose:     ENABLED"
fi
echo "╚════════════════════════════════════════════════════╝"
echo ""

# Build Java command
JAVA_OPTS="-Dconfig.file=$CONFIG_FILE"
if [ "$VERBOSE" = "true" ]; then
    JAVA_OPTS="$JAVA_OPTS -Dorg.slf4j.simpleLogger.defaultLogLevel=debug"
fi

# Start the proxy
nohup java $JAVA_OPTS -jar "$JAR_FILE" > "$LOG_FILE" 2>&1 &
PID=$!
echo $PID > "$PID_FILE"

# Verify startup
sleep 2
if ps -p "$PID" > /dev/null 2>&1; then
    echo "✓ Artemis Proxy started successfully"
    echo ""
    echo "  PID:         $PID"
    echo "  Log:         tail -f $PROXY_DIR/$LOG_FILE"
    echo "  Stop:        sudo kill $PID"
    echo "  Connect to:  <proxy-ip>:$LISTEN_PORT"
    echo ""
else
    echo "✗ Failed to start Artemis Proxy"
    echo "  Check log: $LOG_FILE"
    rm -f "$PID_FILE"
    exit 1
fi
