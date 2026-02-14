#!/bin/bash
# Artemis Proxy Service Installation Script
# Run this script from the artemis-proxy directory

set -e

PROXY_DIR="/media/storage/Users/freeman/artemis-kiosk/Artemis Bridge Stations/artemis-proxy"
SRC_BASE="$PROXY_DIR/src/main/java/com/ussveritas/artemis/proxy"
RESOURCES="$PROXY_DIR/src/main/resources"

echo "========================================="
echo "Artemis Proxy Service - Installation"
echo "========================================="

# Navigate to project directory
cd "$PROXY_DIR"

# Create directory structure
echo "Creating directory structure..."
mkdir -p "$SRC_BASE"/{config,model,observer,aggregator,publisher,proxy,web}
mkdir -p "$RESOURCES"

# Extract source files (assumes tar.gz is in current directory)
if [ -f "artemis-proxy-sources.tar.gz" ]; then
    echo "Extracting source files..."
    tar -xzf artemis-proxy-sources.tar.gz
    
    # Move files to correct locations
    echo "Installing source files..."
    mv ArtemisProxyService.java "$SRC_BASE/"
    mv GhostObserver.java GhostObserverManager.java "$SRC_BASE/observer/"
    mv SnapshotAggregator.java "$SRC_BASE/aggregator/"
    mv UdpPublisher.java JsonFilePublisher.java "$SRC_BASE/publisher/"
    mv ProxyServer.java ProxyConnection.java "$SRC_BASE/proxy/"
    mv ConfigWebServer.java "$SRC_BASE/web/"
    mv logback.xml "$RESOURCES/"
else
    echo "ERROR: artemis-proxy-sources.tar.gz not found!"
    echo "Please place the tarball in $PROXY_DIR first"
    exit 1
fi

# Create model files (already exist from earlier)
echo "Verifying model files..."
ls "$SRC_BASE/model/"*.java > /dev/null 2>&1 || echo "WARNING: Model files may be missing"

# Create config files (already exist)
echo "Verifying config files..."
ls "$SRC_BASE/config/"*.java > /dev/null 2>&1 || echo "WARNING: Config files may be missing"

echo ""
echo "========================================="
echo "Installation complete!"
echo "========================================="
echo ""
echo "Next steps:"
echo "  1. cd \"$PROXY_DIR\""
echo "  2. mvn clean package"
echo "  3. java -jar target/artemis-proxy-1.0-SNAPSHOT-jar-with-dependencies.jar"
echo ""
echo "Web interface will be at: http://localhost:81"
echo "========================================="
