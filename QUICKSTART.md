# Artemis Proxy - Quick Start Guide

## Installation (30 seconds)

```bash
# 1. Navigate to project directory
cd "/media/storage/Users/freeman/artemis-kiosk/Artemis Bridge Stations/artemis-proxy"

# 2. Upload these 4 files:
#    - artemis-proxy-sources.tar.gz
#    - install_proxy.sh  
#    - pom.xml
#    - README.md

# 3. Replace POM file
cp pom.xml pom.xml.backup  # Backup existing
# Then upload the new pom.xml

# 4. Run installation
chmod +x install_proxy.sh
./install_proxy.sh

# 5. Build
mvn clean package

# 6. Run
java -jar target/artemis-proxy-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## What You Get

✅ **Proxy Server** on port 2011 - transparent TCP forwarding  
✅ **3 Ghost Observers** - Engineering, Weapons, Comms  
✅ **UDP Output** - localhost:50200 @ 5Hz (200ms)  
✅ **JSON File** - artemis_proxy_status.json (legacy compatible)  
✅ **Web Config** - http://localhost:81  

## Quick Test

```bash
# Terminal 1: Run proxy
java -jar target/artemis-proxy-1.0-SNAPSHOT-jar-with-dependencies.jar

# Terminal 2: Listen for UDP (requires netcat)
nc -ul 50200

# Terminal 3: Check JSON file
watch -n 0.2 cat artemis_proxy_status.json

# Browser: Open config
firefox http://localhost:81
```

## Files You Need to Upload

1. **artemis-proxy-sources.tar.gz** (8.3 KB)
   - All Java source files
   - Logback configuration

2. **install_proxy.sh** (script)
   - Extracts and organizes source files
   - Verifies installation

3. **pom.xml** (Maven config)
   - Dependencies: IAN 3.5.1, Jackson, Logback
   - Build configuration
   - Java 17 settings

4. **README.md** (documentation)
   - Full architecture
   - API documentation
   - Troubleshooting guide

## Default Ports

| Service | Port | Description |
|---------|------|-------------|
| Proxy Listen | 2011 | Where Artemis clients connect |
| Upstream Server | 2010 | Real Artemis server |
| UDP Output | 50200 | JSON snapshots broadcast |
| Web Config | 81 | Browser configuration UI |

## Verify It's Working

1. **Check logs** - Should see:
   ```
   Ghost observer started: ENGINEERING
   Ghost observer started: WEAPONS  
   Ghost observer started: COMMUNICATIONS
   Proxy server listening on port 2011
   Web config server started on port 81
   ```

2. **Check web UI** - Go to `http://localhost:81`
   - Should show green connection status
   - All 3 ghosts "connected: true"

3. **Check outputs**:
   ```bash
   # UDP test
   nc -ul 50200
   # Should see JSON every 200ms
   
   # JSON file test
   cat artemis_proxy_status.json
   # Should have ship data
   ```

## Common Issues

**"Port 2011 already in use"**
```bash
lsof -i :2011  # Find process
kill <PID>     # Stop it
```

**"Ghosts not connecting"**
- Check Artemis server is running: `telnet 192.168.1.209 2010`
- Verify target ship (0-7) in web UI
- Check firewall rules

**"Maven build fails"**
```bash
# Check Java version
java -version  # Should be 17+

# Rebuild IAN library
cd "../artemis-bridge"
mvn install:install-file -Dfile=ian-3.5.1.jar -DgroupId=com.walkertribe.ian -DartifactId=ian -Dversion=3.5.1 -Dpackaging=jar
```

## Integration with AutoHotkey

### UDP Mode (Recommended)
```autohotkey
; Listen for real-time updates
socket := UDPSocket()
socket.Bind("127.0.0.1", 50200)

Loop {
    data := socket.Receive()  ; Blocking
    snapshot := JSON.Parse(data)
    UpdateUI(snapshot)
}
```

### File Mode (Legacy)
```autohotkey
; Poll file every 100ms (compatible with existing scripts)
SetTimer ReadStatus, 100

ReadStatus() {
    json := FileRead("artemis_proxy_status.json")
    global status := JSON.Parse(json)
    UpdateUI(status)
}
```

## Stopping the Service

```bash
# Graceful shutdown
Ctrl+C

# Force kill
ps aux | grep artemis-proxy
kill <PID>
```

## Next Steps

1. Test with real Artemis server
2. Connect AutoHotkey script via UDP
3. Customize web UI (edit ConfigWebServer.java)
4. Add more ghost observers if needed
5. Adjust publish rate (change 200ms in ArtemisProxyService.java)

---

**USS Veritas - Ready for Combat Operations!** 🚀
