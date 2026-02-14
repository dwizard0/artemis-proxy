package com.ussveritas.artemis.proxy.publisher;
import com.ussveritas.artemis.proxy.model.GameObjectInfo;

import com.ussveritas.artemis.proxy.config.*;
import com.ussveritas.artemis.proxy.aggregator.*;
import com.ussveritas.artemis.proxy.model.*;
import com.ussveritas.artemis.proxy.observer.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.*;

import java.io.Closeable;
import java.net.*;
import java.util.*;

public class UdpPublisher implements Closeable {
    private static final Logger log = LoggerFactory.getLogger(UdpPublisher.class);
    
    private final ConfigurationManager configManager;
    private final SnapshotAggregator aggregator;
    private final ObjectMapper mapper;
    private final DatagramSocket socket;
    private final List<String> targetIPs;
    private volatile Map<GhostType, GhostStatus> ghostStatus;
    
    public UdpPublisher(ConfigurationManager configManager, SnapshotAggregator aggregator) {
        this.configManager = configManager;
        this.aggregator = aggregator;
        this.mapper = new ObjectMapper();
        this.ghostStatus = new EnumMap<>(GhostType.class);
        
        try {
            this.socket = new DatagramSocket();
            this.targetIPs = ConfigFileReader.readUdpTargets();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize UDP publisher", e);
        }
    }
    
    public void setGhostStatus(Map<GhostType, GhostStatus> status) {
        this.ghostStatus = status;
    }
    
    public void publish() {
        try {
            Map<String, Object> message = buildMessage();
            String json = mapper.writeValueAsString(message);
            byte[] data = json.getBytes("UTF-8");
            
            ProxyConfiguration config = configManager.get();
            
            for (String targetIP : targetIPs) {
                DatagramPacket packet = new DatagramPacket(data, data.length, 
                    InetAddress.getByName(targetIP), config.udpPort());
                socket.send(packet);
            }
        } catch (Exception e) {
            log.error("Failed to publish UDP snapshot", e);
        }
    }
    
    private Map<String, Object> buildMessage() {
        ProxyConfiguration config = configManager.get();
        ShipSnapshot ship = aggregator.getCurrentSnapshot();
        List<GameObjectInfo> objects = aggregator.getVisibleObjects();
        
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("type", "snapshot");
        message.put("ts_ms", System.currentTimeMillis());
        
        Map<String, Object> server = new LinkedHashMap<>();
        server.put("host", config.upstreamHost());
        server.put("port", config.upstreamPort());
        server.put("connected", true);
        message.put("server", server);
        
        message.put("targetShip", config.targetShip());
        
        Map<String, Object> ghosts = new LinkedHashMap<>();
        for (GhostType type : GhostType.values()) {
            GhostStatus status = ghostStatus.getOrDefault(type, new GhostStatus(false, 0));
            Map<String, Object> ghostInfo = new LinkedHashMap<>();
            ghostInfo.put("connected", status.connected());
            ghostInfo.put("ship", status.ship());
            ghosts.put(type.name().toLowerCase(), ghostInfo);
        }
        message.put("ghosts", ghosts);
        
        message.put("ship", ship);
        message.put("objects", objects);
        
        return message;
    }
    
    @Override
    public void close() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}
