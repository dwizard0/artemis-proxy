package com.ussveritas.artemis.proxy.observer;

import com.walkertribe.ian.iface.*;
import com.walkertribe.ian.protocol.core.setup.*;
import com.walkertribe.ian.enums.*;
import com.ussveritas.artemis.proxy.aggregator.*;
import org.slf4j.*;

import java.io.IOException;

public class GhostObserver implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(GhostObserver.class);
    private static final int RECONNECT_DELAY_MS = 5000;
    private static final int UPDATE_INTERVAL_MS = 100;
    
    private final GhostType type;
    private final String host;
    private final int port;
    private final SnapshotAggregator aggregator;
    
    private volatile int targetShip;
    private volatile boolean enabled;
    private volatile boolean running;
    private volatile boolean connected;
    
    private ArtemisNetworkInterface connection;
    private Thread thread;
    
    public GhostObserver(GhostType type, String host, int port, int targetShip, SnapshotAggregator aggregator) {
        this.type = type;
        this.host = host;
        this.port = port;
        this.targetShip = targetShip;
        this.aggregator = aggregator;
        this.enabled = true;
    }
    
    public void start() {
        if (running) return;
        running = true;
        thread = new Thread(this, "Ghost-" + type);
        thread.setDaemon(true);
        thread.start();
    }
    
    public void stop() {
        running = false;
        disconnect();
        if (thread != null) {
            thread.interrupt();
        }
    }
    
    public void setTargetShip(int ship) {
        if (this.targetShip != ship) {
            this.targetShip = ship;
            reconnect();
        }
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            disconnect();
        }
    }
    
    @Override
    public void run() {
        log.info("{} ghost observer started", type);
        
        while (running) {
            if (!enabled) {
                sleep(1000);
                continue;
            }
            
            try {
                connect();
                processPackets();
            } catch (Exception e) {
                log.warn("{} ghost disconnected: {}", type, e.getMessage());
                connected = false;
            }
            
            if (running && enabled) {
                sleep(RECONNECT_DELAY_MS);
            }
        }
        
        log.info("{} ghost observer stopped", type);
    }
    
    private void connect() throws IOException {
        if (connection != null) {
            disconnect();
        }
        
        log.info("{} ghost connecting to {}:{}...", type, host, port);
        
        connection = new ThreadedArtemisNetworkInterface(host, port);
        
        // Add World as listener
        connection.addListener(aggregator.getWorldFor(type));
        
        connection.start();
        
        // Wait for connection to establish
        sleep(500);
        
        // Send ship selection
        SetShipPacket setShip = new SetShipPacket(targetShip);
        connection.send(setShip);
        
        // Send console selection
        SetConsolePacket setConsole = new SetConsolePacket(type.getConsole(), true);
        connection.send(setConsole);
        
        connected = true;
        log.info("{} ghost connected to ship {}", type, targetShip);
    }
    
    private void disconnect() {
        if (connection != null) {
            try {
                connection.stop();
            } catch (Exception e) {
                log.debug("Error disconnecting: {}", e.getMessage());
            }
            connection = null;
        }
        connected = false;
    }
    
    private void reconnect() {
        disconnect();
    }
    
    private void processPackets() {
        while (connected && running && enabled) {
            // Let World process packets via listener
            // Periodically trigger snapshot updates
            sleep(UPDATE_INTERVAL_MS);
            aggregator.updateSnapshot();
        }
    }
    
    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    public int getCurrentShip() {
        return targetShip;
    }
}
