# Artemis Proxy - Full-Featured Scripts Guide

## Overview

These scripts provide complete control over Artemis Proxy configuration with command-line arguments for all settings.

## Features

✅ **Server Configuration** - IP, port, ship selection  
✅ **Multiple Instances** - Run several proxies on different ports  
✅ **UDP Broadcasting** - Multiple target IPs, custom port  
✅ **Custom Output** - JSON filename, log files  
✅ **Debug Mode** - Verbose logging  
✅ **Web Interface** - Custom port  

---

## Quick Start

### Basic Usage (Defaults)
```bash
# Linux
sudo ./start-proxy-full.sh

# Windows
start-proxy-full.bat
```

**Defaults:**
- Server: 192.168.1.209:2010
- Listen: 2010
- Ship: 0
- UDP: 192.168.1.209:50200
- JSON: artemis_proxy_status.json
- Web: 81

---

## All Command-Line Options

### Server Options

| Option | Description | Default | Example |
|--------|-------------|---------|---------|
| `-s`, `--server` | Artemis server IP | 192.168.1.209 | `-s 10.0.0.5` |
| `-p`, `--port` | Artemis server port | 2010 | `-p 2010` |
| `-l`, `--listen` | Proxy listen port | 2010 | `-l 2011` |
| `-n`, `--ship` | Ship to track (0-7) | 0 | `-n 2` |

### Output Options

| Option | Description | Default | Example |
|--------|-------------|---------|---------|
| `-u`, `--udp` | UDP target IPs (comma-separated) | 192.168.1.209 | `-u 192.168.1.200,192.168.1.201` |
| `--udp-port` | UDP broadcast port | 50200 | `--udp-port 50201` |
| `-j`, `--json` | JSON output filename | artemis_proxy_status.json | `-j ship1.json` |
| `-w`, `--web-port` | Web config interface port | 81 | `-w 82` |

### Logging Options

| Option | Description | Default | Example |
|--------|-------------|---------|---------|
| `--log` | Log filename | proxy.log | `--log proxy_ship2.log` |
| `-v`, `--verbose` | Enable debug logging | (off) | `-v` |

---

## Common Usage Examples

### 1. Different Server
```bash
./start-proxy-full.sh -s 192.168.1.100
```

### 2. Track Ship 2
```bash
./start-proxy-full.sh -n 2
```

### 3. Multiple Proxies (Different Ships)
```bash
# Terminal 1 - Ship 0 on port 2010
./start-proxy-full.sh -n 0 -l 2010 -j ship0.json --log ship0.log

# Terminal 2 - Ship 1 on port 2011
./start-proxy-full.sh -n 1 -l 2011 -j ship1.json --log ship1.log

# Terminal 3 - Ship 2 on port 2012
./start-proxy-full.sh -n 2 -l 2012 -j ship2.json --log ship2.log
```

### 4. Broadcast to Multiple LCARS Panels
```bash
./start-proxy-full.sh -u 192.168.1.200,192.168.1.201,192.168.1.202
```

### 5. Full Custom Configuration
```bash
./start-proxy-full.sh \
  -s 10.0.0.100 \
  -p 2010 \
  -l 2015 \
  -n 3 \
  -u 10.0.0.200,10.0.0.201 \
  --udp-port 50300 \
  -j artemis_custom.json \
  -w 8080 \
  --log custom.log \
  --verbose
```

### 6. Debug Mode
```bash
./start-proxy-full.sh -v
# View logs: tail -f proxy.log
```

---

## Stopping Proxies

### Stop Default Instance
```bash
# Linux
sudo ./stop-proxy-full.sh

# Windows
stop-proxy-full.bat
```

### Stop Specific Port
```bash
# Linux
sudo ./stop-proxy-full.sh -p 2011

# Windows
stop-proxy-full.bat -p 2011
```

### Stop All Instances
```bash
# Linux
sudo ./stop-proxy-full.sh --all

# Windows
stop-proxy-full.bat --all
```

---

## Advanced Scenarios

### USS Veritas Bridge Setup (6 Stations)

**Scenario:** One proxy tracks ship 0, broadcasts to 6 LCARS panels

```bash
./start-proxy-full.sh \
  -s 192.168.1.209 \
  -n 0 \
  -u 192.168.1.101,192.168.1.102,192.168.1.103,192.168.1.104,192.168.1.105,192.168.1.106
```

**LCARS Panels:**
- 192.168.1.101 - Helm
- 192.168.1.102 - Weapons
- 192.168.1.103 - Engineering
- 192.168.1.104 - Science
- 192.168.1.105 - Communications
- 192.168.1.106 - Captain's Display

### Multi-Ship Fleet Training

**Scenario:** 3 ships, each with their own proxy

```bash
# Artemis (Ship 0)
./start-proxy-full.sh -n 0 -l 2010 -u 192.168.1.101 -j artemis.json --log artemis.log

# Intrepid (Ship 1)
./start-proxy-full.sh -n 1 -l 2011 -u 192.168.1.102 -j intrepid.json --log intrepid.log

# Phoenix (Ship 2)
./start-proxy-full.sh -n 2 -l 2012 -u 192.168.1.103 -j phoenix.json --log phoenix.log
```

Players connect to:
- Artemis crew → 192.168.1.200:2010
- Intrepid crew → 192.168.1.200:2011
- Phoenix crew → 192.168.1.200:2012

### LAN Party with Different Server
```bash
./start-proxy-full.sh -s 172.16.0.100 -u 172.16.0.200,172.16.0.201,172.16.0.202
```

---

## Desktop Shortcuts (Windows)

Create shortcuts with pre-configured settings:

### Ship 0 - Artemis
```
Target: C:\path\to\start-proxy-full.bat -n 0 -j artemis.json
Name: Artemis Proxy - Ship 0
```

### Ship 1 - Intrepid
```
Target: C:\path\to\start-proxy-full.bat -n 1 -l 2011 -j intrepid.json
Name: Artemis Proxy - Ship 1
```

### Debug Mode
```
Target: C:\path\to\start-proxy-full.bat --verbose
Name: Artemis Proxy - Debug
```

---

## Configuration Files Generated

The scripts auto-generate these files:

### proxy-config.properties
```properties
upstream.host=192.168.1.209
upstream.port=2010
proxy.port=2010
target.ship=0
udp.port=50200
web.port=81
json.file=artemis_proxy_status.json
```

### udp_targets.txt
```
# UDP Targets - Auto-generated
192.168.1.209
192.168.1.210
```

---

## Troubleshooting

### "Port already in use"
Use different listen port: `-l 2011`

### Multiple instances won't start
Each needs unique:
- Listen port (`-l`)
- JSON file (`-j`)
- Log file (`--log`)

### UDP not reaching panels
Check firewall and verify IPs: `-u 192.168.1.200,192.168.1.201`

### Can't see debug info
Enable verbose: `-v` then check log file

### Wrong ship data
Verify ship number: `-n 0` (must be 0-7)

---

## Monitoring

### View Logs
```bash
# Linux
tail -f proxy.log
tail -f ship1.log    # Custom log

# Windows
notepad proxy.log
```

### Check Running Proxies
```bash
# Linux
ps aux | grep artemis-proxy

# Windows
tasklist | findstr java
```

### View JSON Output
```bash
# Linux
cat artemis_proxy_status.json
watch -n 1 cat artemis_proxy_status.json  # Live updates

# Windows
type artemis_proxy_status.json
```

---

## Best Practices

1. **Different ships = different ports**
   - Ship 0: port 2010
   - Ship 1: port 2011
   - Ship 2: port 2012

2. **Unique log files per instance**
   - `--log ship0.log`
   - `--log ship1.log`

3. **Use verbose for troubleshooting only**
   - Generates lots of logs
   - Remove `-v` for production

4. **Keep default UDP port unless conflict**
   - 50200 works for most setups

5. **Document your configuration**
   - Keep notes on which ports/ships/IPs

---

🚀 **USS Veritas - All Systems Configured and Ready!**
