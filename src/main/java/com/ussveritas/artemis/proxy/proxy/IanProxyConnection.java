package com.ussveritas.artemis.proxy.proxy;

import com.ussveritas.artemis.proxy.aggregator.*;
import com.walkertribe.ian.iface.*;
import com.walkertribe.ian.protocol.*;
import com.walkertribe.ian.world.*;
import org.slf4j.*;

import java.io.*;
import java.net.*;

/**
 * IAN-based proxy connection that observes traffic flowing between
 * client and server without blocking any consoles.
 */
public class IanProxyConnection  {
    private static final Logger log = LoggerFactory.getLogger(IanProxyConnection.class);
    
    private final Socket clientSocket;
    private final String upstreamHost;
    private final int upstreamPort;
    private final SnapshotAggregator aggregator;
    
    private ArtemisNetworkInterface clientInterface;
    private ArtemisNetworkInterface serverInterface;
    private volatile boolean connected = false;
    
    public IanProxyConnection(Socket clientSocket, String upstreamHost, int upstreamPort, SnapshotAggregator aggregator) {
        this.clientSocket = clientSocket;
        this.upstreamHost = upstreamHost;
        this.upstreamPort = upstreamPort;
        this.aggregator = aggregator;
    }
    
    public void start() {
        try {
            // Create server connection
            Socket serverSocket = new Socket(upstreamHost, upstreamPort);
            log.info("Proxy connected to upstream {}:{}", upstreamHost, upstreamPort);
            
            // Create IAN interfaces for both sides
            clientInterface = new ThreadedArtemisNetworkInterface(clientSocket, com.walkertribe.ian.enums.Origin.CLIENT);
            serverInterface = new ThreadedArtemisNetworkInterface(serverSocket, com.walkertribe.ian.enums.Origin.SERVER);
            
            // Add World listeners to observe traffic
            // Use a shared World for all ghost types to see all data
            World observerWorld = aggregator.getWorldFor(com.ussveritas.artemis.proxy.observer.GhostType.WEAPONS);
            
            // Observe server->client traffic (this is where game state updates come from)
            serverInterface.addListener(observerWorld);
            
            // Add connection event listeners
            clientInterface.addListener(this);
            serverInterface.addListener(this);
            
            // Add packet forwarding listeners
            clientInterface.addListener(new PacketForwarder(serverInterface, "C->S"));
            serverInterface.addListener(new PacketForwarder(clientInterface, "S->C"));
            
            // Start both interfaces
            clientInterface.start();
            serverInterface.start();
            
            connected = true;
            log.info("Proxy connection established");
            
            // Keep connection alive until disconnected
            while (connected) {
                Thread.sleep(100);
                // Trigger snapshot updates
                aggregator.updateSnapshot();
            }
            
        } catch (Exception e) {
            log.error("Proxy connection error", e);
        } finally {
            close();
        }
    }
    
    
    @Listener
    public void onConnect(ConnectionEvent event) {
        log.debug("Connection event: connected");
    }
    
    
    @Listener
    public void onDisconnect(DisconnectEvent event) {
        log.info("Disconnected: {}", event.getCause());
        connected = false;
    }
    
    public void close() {
        connected = false;
        
        if (clientInterface != null) {
            try {
                clientInterface.stop();
            } catch (Exception e) {
                log.debug("Error stopping client interface", e);
            }
        }
        
        if (serverInterface != null) {
            try {
                serverInterface.stop();
            } catch (Exception e) {
                log.debug("Error stopping server interface", e);
            }
        }
        
        if (clientSocket != null && !clientSocket.isClosed()) {
            try {
                clientSocket.close();
            } catch (IOException e) {
                log.debug("Error closing client socket", e);
            }
        }
    }
    
    /**
     * Listener that forwards packets from one interface to another
     */
    public static class PacketForwarder {
        private static final Logger log = LoggerFactory.getLogger(PacketForwarder.class);
        private final ArtemisNetworkInterface destination;
        private final String direction;
        
        public PacketForwarder(ArtemisNetworkInterface destination, String direction) {
            this.destination = destination;
            this.direction = direction;
        }
        
        @Listener
        public void onPacket(ArtemisPacket packet) {
            try {
                // Forward all packets
                destination.send(packet);
                log.trace("{}: Forwarded {}", direction, packet.getClass().getSimpleName());
            } catch (Exception e) {
                log.error("{}: Error forwarding packet", direction, e);
            }
        }
    }
}
