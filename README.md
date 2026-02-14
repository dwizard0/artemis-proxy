# Artemis Proxy Service

Complete TCP proxy, ghost observer system, and data aggregation service for Artemis Spaceship Bridge Simulator.

## Features

✅ **TCP Proxy Layer** - Forward Artemis client connections transparently  
✅ **Ghost Observers** - Engineering, Weapons, and Communications console observers  
✅ **Dual Output** - Both UDP (port 50200) and JSON file output  
✅ **Web Configuration** - Browser-based config interface on port 81  
✅ **Auto-Reconnect** - Ghosts automatically reconnect on disconnect  
✅ **Thread-Safe** - Proper locking for concurrent data access  
✅ **Cross-Platform** - Runs on both Ubuntu and Windows with Java 17+

## Architecture

```
ArtemisProxyService (Main Orchestrator)
├── ProxyServer (TCP forwarding on port 2011)
│   └── ProxyConnection (bidirectional forwarding)
├── GhostObserverManager
│   ├── Ghost: Engineering (Console.ENGINEERING)
│   ├── Ghost: Weapons (Console.WEAPONS)
│   └── Ghost: Communications (Console.COMMUNICATIONS)
├── SnapshotAggregator (Thread-safe data merging)
├── Publishers (200ms / 5Hz)
│   ├── UdpPublisher → localhost:50200
│   └── JsonFilePublisher → artemis_proxy_status.json
└── ConfigWebServer (HTTP on port 81)
```

## Installation

### Step 1: Transfer Files

Copy the two provided files to your server:
- `artemis-proxy-sources.tar.gz`
- `install_proxy.sh`

Place them in: `/media/storage/Users/freeman/artemis-kiosk/Artemis Bridge Stations/artemis-proxy/`

```bash
cd "/media/storage/Users/freeman/artemis-kiosk/Artemis Bridge Stations/artemis-proxy"
# Upload artemis-proxy-sources.tar.gz and install_proxy.sh here
```

### Step 2: Run Installation Script

```bash
chmod +x install_proxy.sh
./install_proxy.sh
```

This will:
- Extract all source files
- Place them in the correct package structure
- Verify the installation

### Step 3: Build with Maven

```bash
mvn clean package
```

This creates: `target/artemis-proxy-1.0-SNAPSHOT-jar-with-dependencies.jar`

### Step 4: Run the Service

```bash
java -jar target/artemis-proxy-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Configuration

### Web Interface

Open browser to: `http://localhost:81`

Configure:
- **Upstream Server**: IP and port of Artemis server (default: 192.168.1.209:2010)
- **Proxy Listen Port**: Port for clients to connect through (default: 2011)
- **Target Ship**: Which ship (0-7) the ghosts observe
- **Ghost Observers**: Enable/disable Engineering, Weapons, Comms
- **Output Settings**: JSON file path, UDP port, web port

### Default Configuration

```
Upstream: 192.168.1.209:2010
Proxy Port: 2011
Target Ship: 0 (Artemis)
UDP Port: 50200
JSON File: artemis_proxy_status.json
Web Port: 81
All ghosts: ENABLED
```

## Usage

### For Artemis Clients

Instead of connecting directly to `192.168.1.209:2010`, connect to:

```
<proxy-host>:2011
```

The proxy transparently forwards all traffic while ghosts observe in the background.

### For AutoHotkey Scripts

#### UDP Mode (Recommended)

```autohotkey
; Listen on UDP port 50200
socket := UDPSocket()
socket.Bind("127.0.0.1", 50200)

Loop {
    data := socket.Receive()
    snapshot := JSON.Parse(data)
    
    ; Access data
    targetShip := snapshot["targetShip"]
    hull := snapshot["ship"]["hull"]
    shields := snapshot["ship"]["shields"]["fore"]
    tubes := snapshot["ship"]["weapons"]["tubes"]
}
```

#### JSON File Mode (Legacy Compatible)

```autohotkey
; Read from JSON file (same format as ArtemisWeaponsBridge)
statusFile := "artemis_proxy_status.json"
jsonText := FileRead(statusFile)
status := JSON.Parse(jsonText)

; Access data (compatible with existing scripts)
connected := status["connected"]
tubes := status["tubes"]
shieldsFront := status["shields_front"]
```

## Data Format

### UDP JSON Schema

```json
{
  "type": "snapshot",
  "ts_ms": 1707748123456,
  "server": {
    "host": "192.168.1.209",
    "port": 2010,
    "connected": true
  },
  "targetShip": 0,
  "ghosts": {
    "engineering": {"connected": true, "ship": 0},
    "weapons": {"connected": true, "ship": 0},
    "comms": {"connected": true, "ship": 0}
  },
  "ship": {
    "name": "Artemis",
    "hull": 0.95,
    "shields": {
      "fore": 0.8,
      "aft": 0.7,
      "port": null,
      "starboard": null
    },
    "weapons": {
      "tubes": [
        {"idx": 0, "loaded": true, "type": "HOMING", "countdown": 0.0},
        {"idx": 1, "loaded": false, "type": null, "countdown": 5.2}
      ],
      "beamFreq": {"frequency": 5}
    },
    "systems": {
      "reactor": 950.0,
      "engines": 0.8,
      "weapons": null,
      "sensors": null
    }
  }
}
```

### JSON File Format (Legacy Compatible)

```json
{
  "connected": true,
  "ship_number": 0,
  "server_ip": "192.168.1.209",
  "server_port": 2010,
  "tubes": [
    {"loaded": true, "type": 0, "state": "LOADED", "countdown": 0.0}
  ],
  "shields_up": true,
  "shields_front": 80.0,
  "shields_rear": 70.0,
  "energy": 950.0,
  "max_energy": 1000.0,
  "auto_beams": false,
  "beam_frequency": 5,
  "selected_torp_type": 0,
  "forward_arc_active": false,
  "aft_arc_active": false
}
```

## Logging

Logs output to console with format:
```
HH:mm:ss.SSS [thread] LEVEL  class - message
```

Adjust log level in `src/main/resources/logback.xml`:
```xml
<root level="INFO">  <!-- Change to DEBUG for verbose output -->
```

## Troubleshooting

### Ghosts Not Connecting
- Check upstream server is reachable: `telnet 192.168.1.209 2010`
- Verify target ship number (0-7)
- Check web interface ghost status

### UDP Not Receiving
- Verify port 50200 is not blocked
- Check firewall rules
- Ensure AutoHotkey is listening on correct port

### Port Already in Use
- Change proxy port in web interface
- Or kill existing process: `lsof -i :2011`

### Permission Denied
```bash
sudo chown -R $USER:$USER "/media/storage/Users/freeman/artemis-kiosk/Artemis Bridge Stations/artemis-proxy"
```

## Development

### Project Structure

```
artemis-proxy/
├── pom.xml
├── src/main/
│   ├── java/com/ussveritas/artemis/proxy/
│   │   ├── ArtemisProxyService.java        # Main entry point
│   │   ├── config/
│   │   │   ├── ProxyConfiguration.java     # Config model
│   │   │   └── ConfigurationManager.java   # Thread-safe config
│   │   ├── model/
│   │   │   ├── ShipSnapshot.java           # Unified ship state
│   │   │   ├── ShieldData.java
│   │   │   ├── TubeData.java
│   │   │   ├── WeaponData.java
│   │   │   ├── BeamFrequency.java
│   │   │   └── SystemPowerData.java
│   │   ├── observer/
│   │   │   ├── GhostType.java              # Enum: ENG/WEAP/COMMS
│   │   │   ├── GhostStatus.java
│   │   │   ├── GhostObserver.java          # Individual ghost
│   │   │   └── GhostObserverManager.java   # Manages all ghosts
│   │   ├── aggregator/
│   │   │   └── SnapshotAggregator.java     # Merges ghost data
│   │   ├── publisher/
│   │   │   ├── UdpPublisher.java           # UDP broadcaster
│   │   │   └── JsonFilePublisher.java      # File writer
│   │   ├── proxy/
│   │   │   ├── ProxyServer.java            # TCP listener
│   │   │   └── ProxyConnection.java        # Bidirectional forwarder
│   │   └── web/
│   │       └── ConfigWebServer.java        # HTTP config UI
│   └── resources/
│       └── logback.xml                      # Logging config
└── target/
    └── artemis-proxy-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### Dependencies

- **IAN 3.5.1**: Artemis network interface
- **Jackson 2.15.2**: JSON serialization
- **SLF4J + Logback**: Logging
- **JDK 17+**: Required for records and enhanced switch

### Building from Source

```bash
# Clean build
mvn clean package

# Skip tests (if any)
mvn package -DskipTests

# Run directly with Maven
mvn exec:java -Dexec.mainClass="com.ussveritas.artemis.proxy.ArtemisProxyService"
```

## License

Created for USS Veritas Bridge Simulator Events

## Support

For issues or questions, check:
1. Web interface status: `http://localhost:81`
2. Console logs for error messages
3. Verify all ghosts are connected
4. Test upstream connection independently

---

**Ready for USS Veritas Bridge Operations!** 🚀
