package com.ussveritas.artemis.proxy.observer;

import com.ussveritas.artemis.proxy.config.*;
import com.ussveritas.artemis.proxy.aggregator.*;
import org.slf4j.*;

import java.util.*;

public class GhostObserverManager {
    private static final Logger log = LoggerFactory.getLogger(GhostObserverManager.class);
    
    private final ConfigurationManager configManager;
    private final SnapshotAggregator aggregator;
    private final Map<GhostType, GhostObserver> ghosts;
    
    public GhostObserverManager(ConfigurationManager configManager, SnapshotAggregator aggregator) {
        this.configManager = configManager;
        this.aggregator = aggregator;
        this.ghosts = new EnumMap<>(GhostType.class);
    }
    
    public void start() {
        ProxyConfiguration config = configManager.get();
        
        // Set target ship in aggregator
        aggregator.setTargetShip(config.targetShip());
        
        for (GhostType type : GhostType.values()) {
            GhostObserver ghost = new GhostObserver(
                type,
                config.upstreamHost(),
                config.upstreamPort(),
                config.targetShip(),
                aggregator
            );
            ghost.setEnabled(isGhostEnabled(type, config));
            ghost.start();
            ghosts.put(type, ghost);
        }
        
        log.info("Ghost observer manager started");
    }
    
    public void stop() {
        ghosts.values().forEach(GhostObserver::stop);
        ghosts.clear();
        log.info("Ghost observer manager stopped");
    }
    
    public void reconfigure() {
        ProxyConfiguration config = configManager.get();
        
        // Update target ship in aggregator
        aggregator.setTargetShip(config.targetShip());
        
        for (Map.Entry<GhostType, GhostObserver> entry : ghosts.entrySet()) {
            GhostType type = entry.getKey();
            GhostObserver ghost = entry.getValue();
            
            ghost.setTargetShip(config.targetShip());
            ghost.setEnabled(isGhostEnabled(type, config));
        }
        
        log.info("Ghosts reconfigured");
    }
    
    private boolean isGhostEnabled(GhostType type, ProxyConfiguration config) {
        return switch (type) {
            case ENGINEERING -> config.engineeringEnabled();
            case WEAPONS -> config.weaponsEnabled();
            case COMMUNICATIONS -> config.commsEnabled();
        };
    }
    
    public Map<GhostType, GhostStatus> getStatus() {
        Map<GhostType, GhostStatus> status = new EnumMap<>(GhostType.class);
        
        for (Map.Entry<GhostType, GhostObserver> entry : ghosts.entrySet()) {
            GhostObserver ghost = entry.getValue();
            status.put(entry.getKey(), new GhostStatus(ghost.isConnected(), ghost.getCurrentShip()));
        }
        
        return status;
    }
}
