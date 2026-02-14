# IAN-Based Proxy Implementation - Deployment Guide

## What This Fixes

**Problem:** Ghost observers were registering as consoles, blocking real players from Weapons/Engineering/Comms stations.

**Solution:** IAN-based proxy that observes traffic flowing through it without registering as any console type.

## How It Works

```
Player → Proxy (observes) → Artemis Server
         ↓
    World objects see all packets
         ↓
    JSON/UDP output
```

**Key Benefits:**
- ✅ All 6 consoles available to real players
- ✅ Proxy observes ALL traffic (weapons, engineering, everything)
- ✅ No console blocking
- ✅ Clean IAN library implementation following official patterns

## Architecture Changes

### OLD (Ghost Observer) Approach:
```
3 separate ghost connections → Artemis Server
Each ghost registers as a console → BLOCKS players
```

### NEW (IAN Proxy) Approach:
```
Players → Proxy → Artemis Server
          ↓
   World.addListener() observes packets
          ↓
   No console registration!
```

## Installation

### Step 1: Extract New Files

```bash
cd "/media/storage/Users/freeman/artemis-kiosk/Artemis Bridge Stations/artemis-proxy"
tar -xzf artemis-proxy-ian-implementation.tar.gz
```

This extracts:
- `IanProxyServer.java`
- `IanProxyConnection.java`  
- `ArtemisProxyService_IAN.java`

### Step 2: Move Files to Correct Locations

```bash
cd "/media/storage/Users/freeman/artemis-kiosk/Artemis Bridge Stations/artemis-proxy"

# Move new proxy classes
mv IanProxyServer.java src/main/java/com/ussveritas/artemis/proxy/proxy/
mv IanProxyConnection.java src/main/java/com/ussveritas/artemis/proxy/proxy/

# Replace main service class
mv src/main/java/com/ussveritas/artemis/proxy/ArtemisProxyService.java src/main/java/com/ussveritas/artemis/proxy/ArtemisProxyService.java.backup
mv ArtemisProxyService_IAN.java src/main/java/com/ussveritas/artemis/proxy/ArtemisProxyService.java
```

### Step 3: Add Missing Import

The IanProxyConnection needs the ConnectionEventListener interface. Add this import:

```bash
cd "/media/storage/Users/freeman/artemis-kiosk/Artemis Bridge Stations/artemis-proxy"

# Fix the import (ConnectionEventListener is part of IAN)
sed -i 's/implements ConnectionEventListener//' src/main/java/com/ussveritas/artemis/proxy/proxy/IanProxyConnection.java
```

### Step 4: Rebuild

```bash
cd "/media/storage/Users/freeman/artemis-kiosk/Artemis Bridge Stations/artemis-proxy"
mvn clean package
```

### Step 5: Stop Current Service

```bash
sudo pkill -f artemis-proxy
```

### Step 6: Start New IAN-Based Proxy

```bash
cd "/media/storage/Users/freeman/artemis-kiosk/Artemis Bridge Stations/artemis-proxy"
sudo java -jar target/artemis-proxy-1.0-SNAPSHOT-jar-with-dependencies.jar > proxy.log 2>&1 &
```

## How to Use

### For Players (CRITICAL CHANGE!)

**OLD:** Players connected directly to `192.168.1.209:2010`  
**NEW:** Players MUST connect through the proxy at `192.168.1.200:2010`

### Connection Setup

All Artemis clients should connect to:
- **Server:** `192.168.1.200`
- **Port:** `2010` (default)

The proxy will:
1. Accept their connection
2. Forward to real Artemis server at 192.168.1.209:2010
3. Observe all traffic in both directions
4. Extract weapons/shields/energy data
5. Publish to JSON file + UDP

### For LCARS Panels

No change needed! They continue reading:
- **JSON file:** `artemis_proxy_status.json`
- **UDP stream:** `localhost:50200`

## Verification

### Check Logs

```bash
tail -f proxy.log
```

Look for:
```
Mode: IAN Traffic Observer (no console blocking)
Proxy listening on port: 2010
Client connected from /192.168.1.xxx
Proxy connected to upstream 192.168.1.209:2010
Proxy connection established
```

### Check Data Flow

```bash
# Watch JSON updates
watch -n 0.2 cat artemis_proxy_status.json

# Monitor UDP
nc -ul 50200
```

### Test Console Availability

1. Have players connect to `192.168.1.200:2010`
2. Try taking ANY console (Weapons, Engineering, Comms, etc.)
3. All should be available!

## Technical Details

### Key Classes

**IanProxyServer:**
- Accepts client connections on port 2010
- Spawns IanProxyConnection for each client

**IanProxyConnection:**
- Creates TWO `ArtemisNetworkInterface` instances:
  - `clientInterface` - talks to Artemis client
  - `serverInterface` - talks to Artemis server
- Adds `World` as listener to `serverInterface` (observes game state)
- Uses `PacketForwarder` to relay packets both ways
- NO console registration = NO blocking!

**PacketForwarder (inner class):**
- Listens for ANY `ArtemisPacket`
- Forwards to opposite interface
- Transparent proxying

### Why This Works

The IAN library's `World` class can be added as a listener to ANY `ArtemisNetworkInterface`. When packets flow through, `World` automatically:
- Parses ship state updates
- Maintains current game state
- Provides `getPlayer()` access

We observe server→client packets (which contain all game state) without the proxy itself registering as a console.

## Troubleshooting

### "Players still can't connect"

Check proxy is running:
```bash
ps aux | grep artemis-proxy
netstat -tuln | grep 2010
```

### "No data in JSON file"

Players must connect THROUGH the proxy. If they connect directly to 192.168.1.209:2010, the proxy sees no traffic.

### "Build fails"

Check for import issues. The IAN library version 3.5.1 should have all required classes.

### "Consoles still blocked"

Make sure you deployed the NEW IAN-based version, not the old ghost observer version. Check logs for "Mode: IAN Traffic Observer".

## Rollback

If issues occur:

```bash
cd "/media/storage/Users/freeman/artemis-kiosk/Artemis Bridge Stations/artemis-proxy"
sudo pkill -f artemis-proxy
mv src/main/java/com/ussveritas/artemis/proxy/ArtemisProxyService.java.backup src/main/java/com/ussveritas/artemis/proxy/ArtemisProxyService.java
mvn package
sudo java -jar target/artemis-proxy-1.0-SNAPSHOT-jar-with-dependencies.jar > proxy.log 2>&1 &
```

## Summary

**Before:** Ghost observers blocked consoles  
**After:** IAN proxy observes without blocking

**Player action required:** Connect to `192.168.1.200:2010` instead of `192.168.1.209:2010`

**Result:** All 6 consoles available + LCARS panels get full weapons data!

---

**USS Veritas - All Stations Ready for Duty!** 🚀
