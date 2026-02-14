package com.ussveritas.artemis.proxy.publisher;
import com.ussveritas.artemis.proxy.model.GameObjectInfo;

import com.ussveritas.artemis.proxy.config.*;
import com.ussveritas.artemis.proxy.aggregator.*;
import com.ussveritas.artemis.proxy.model.*;
import com.ussveritas.artemis.proxy.observer.*;
import com.fasterxml.jackson.databind.*;
import org.slf4j.*;

import java.io.*;
import java.util.*;

public class JsonFilePublisher {
    private static final Logger log = LoggerFactory.getLogger(JsonFilePublisher.class);
    
    private final ConfigurationManager configManager;
    private final SnapshotAggregator aggregator;
    private final ObjectMapper mapper;
    private volatile Map<GhostType, GhostStatus> ghostStatus;
    
    public JsonFilePublisher(ConfigurationManager configManager, SnapshotAggregator aggregator) {
        this.configManager = configManager;
        this.aggregator = aggregator;
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.ghostStatus = new EnumMap<>(GhostType.class);
    }
    
    public void setGhostStatus(Map<GhostType, GhostStatus> status) {
        this.ghostStatus = status;
    }
    
    public void publish() {
        ProxyConfiguration config = configManager.get();
        
        try {
            Map<String, Object> message = buildMessage();
            File outputFile = new File(config.jsonOutputFile());
            mapper.writeValue(outputFile, message);
        } catch (Exception e) {
            log.error("Failed to write JSON file", e);
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
}
