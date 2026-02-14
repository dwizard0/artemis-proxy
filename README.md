# Artemis Proxy

A comprehensive proxy server for Artemis Spaceship Bridge Simulator that captures real-time game data and broadcasts it to LCARS display panels and other clients via UDP and JSON.

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Java](https://img.shields.io/badge/Java-17+-orange.svg)
![Platform](https://img.shields.io/badge/platform-Linux%20%7C%20Windows-lightgrey.svg)

## Overview

Artemis Proxy sits between Artemis clients and the game server, transparently forwarding all traffic while extracting comprehensive ship telemetry. Perfect for building custom LCARS panels, mission control displays, spectator views, and data logging.

**Data Available:**
- Ship position, movement, and navigation
- Weapons systems (torpedoes, beams, targeting)
- Engineering (all 11 systems with power/heat/coolant)
- Shields, hull, tactical status
- All visible game objects with names

## Quick Start

```bash
# Build
mvn clean package

# Configure UDP targets
echo "192.168.1.209" > udp_targets.txt

# Start proxy
sudo ./scripts/start-proxy-full.sh

# Connect Artemis clients to <proxy-ip>:2010
```

## Features

✅ Transparent proxy - no client changes needed  
✅ Comprehensive ship data via IAN library  
✅ UDP + JSON output formats  
✅ Multi-target broadcasting  
✅ Game object tracking (enemies, stations)  
✅ Multiple ship/instance support  
✅ Web configuration interface  
✅ Command-line scripts for easy deployment  

## Usage

### Basic
```bash
./scripts/start-proxy-full.sh
```

### Custom Configuration
```bash
# Different server and ship
./scripts/start-proxy-full.sh -s 192.168.1.100 -n 2

# Multiple UDP targets
./scripts/start-proxy-full.sh -u 192.168.1.200,192.168.1.201

# Multiple ships
./scripts/start-proxy-full.sh -n 0 -l 2010 -j ship0.json
./scripts/start-proxy-full.sh -n 1 -l 2011 -j ship1.json
```

### Options

| Option | Description | Default |
|--------|-------------|---------|
| `-s`, `--server` | Artemis server IP | 192.168.1.209 |
| `-n`, `--ship` | Target ship (0-7) | 0 |
| `-l`, `--listen` | Proxy listen port | 2010 |
| `-u`, `--udp` | UDP targets | 192.168.1.209 |
| `-j`, `--json` | JSON output file | artemis_proxy_status.json |
| `-v`, `--verbose` | Debug logging | (off) |

See [docs/FULL_SCRIPTS_GUIDE.md](docs/FULL_SCRIPTS_GUIDE.md) for complete reference.

## Data Format

UDP/JSON output at 5Hz (200ms):

```json
{
  "ship": {
    "identity": { "name": "Artemis", "side": 0 },
    "position": { "x": 50000.0, "bearing": 45.0, "velocity": 0.5 },
    "weapons": { "torpedoCounts": { "homing": 8, "nuke": 4 } },
    "systems": { "beams": { "power": 1.0, "heat": 0.2 } }
  },
  "objects": [
    { "id": 8736, "name": "Kralien Dreadnought", "type": "ENEMY_SHIP" }
  ]
}
```

## Use Cases

- **LCARS Bridge Stations** - Real-time displays for helm, weapons, engineering
- **Mission Control** - Observer displays and tactical overviews
- **Data Logging** - Session recording and analysis
- **Custom Integrations** - Voice assistants, hardware panels, lighting effects

## License

MIT License - see [LICENSE](LICENSE) for details.

## Credits

- **USS Veritas** - Bridge simulator project
- **Artemis** - by Thom Robertson
- **IAN Library** - by @rjwut

---

🖖 **Live long and prosper!** 🚀 **USS Veritas - All Systems Go!**
