package com.ussveritas.artemis.proxy;

import com.ussveritas.artemis.proxy.config.*;
import com.ussveritas.artemis.proxy.proxy.*;
import com.ussveritas.artemis.proxy.aggregator.*;
import com.ussveritas.artemis.proxy.publisher.*;
import com.ussveritas.artemis.proxy.web.*;

import java.util.concurrent.*;
import org.slf4j.*;

public class ArtemisProxyService {
    private static final Logger log = LoggerFactory.getLogger(ArtemisProxyService.class);
    
    private final ConfigurationManager configManager;
    private final IanProxyServer proxyServer;
    private final SnapshotAggregator aggregator;
    private final UdpPublisher udpPublisher;
    private final JsonFilePublisher jsonPublisher;
    private final ConfigWebServer webServer;
    
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> publisherTask;
    
    public ArtemisProxyService() {
        this.configManager = new ConfigurationManager();
        this.aggregator = new SnapshotAggregator();
        this.proxyServer = new IanProxyServer(configManager, aggregator);
        this.udpPublisher = new UdpPublisher(configManager, aggregator);
        this.jsonPublisher = new JsonFilePublisher(configManager, aggregator);
        this.webServer = new ConfigWebServer(this, configManager);
        this.scheduler = Executors.newScheduledThreadPool(2);
    }
    
    public void start() throws Exception {
        log.info("Starting Artemis Proxy Service (IAN-based)...");
        
        ProxyConfiguration config = configManager.get();
        
        // Start IAN proxy server (no ghost observers needed!)
        proxyServer.start();
        
        // Start publishers (200ms interval = 5Hz)
        publisherTask = scheduler.scheduleAtFixedRate(
            this::publishAll,
            0,
            200,
            TimeUnit.MILLISECONDS
        );
        
        // Start web config interface
        webServer.start(config.webPort());
        
        log.info("=================================================");
        log.info("Artemis Proxy Service started successfully");
        log.info("=================================================");
        log.info("Mode: IAN Traffic Observer (no console blocking)");
        log.info("Proxy listening on port: {}", config.proxyListenPort());
        log.info("Upstream server: {}:{}", config.upstreamHost(), config.upstreamPort());
        log.info("Target ship: {}", config.targetShip());
        log.info("UDP publishing to: localhost:{}", config.udpPort());
        log.info("JSON output file: {}", config.jsonOutputFile());
        log.info("Web config: http://localhost:{}", config.webPort());
        log.info("=================================================");
        log.info("IMPORTANT: Players connect to THIS proxy, not directly to Artemis");
        log.info("Client connection: <proxy-host>:{}", config.proxyListenPort());
        log.info("=================================================");
    }
    
    public void stop() {
        log.info("Stopping Artemis Proxy Service...");
        
        if (publisherTask != null) {
            publisherTask.cancel(false);
        }
        
        webServer.stop();
        udpPublisher.close();
        proxyServer.stop();
        scheduler.shutdown();
        
        log.info("Service stopped");
    }
    
    private void publishAll() {
        try {
            // No ghost status to track - proxy observes traffic directly
            
            // Publish to both outputs
            udpPublisher.publish();
            jsonPublisher.publish();
        } catch (Exception e) {
            log.error("Error in publisher task", e);
        }
    }
    
    public void applyConfiguration(ProxyConfiguration newConfig) {
        configManager.update(newConfig);
        aggregator.setTargetShip(newConfig.targetShip());
        log.info("Configuration updated and applied");
    }
    
    public static void main(String[] args) {
        ArtemisProxyService service = new ArtemisProxyService();
        
        Runtime.getRuntime().addShutdownHook(new Thread(service::stop));
        
        try {
            service.start();
            Thread.currentThread().join(); // Keep alive
        } catch (Exception e) {
            log.error("Fatal error", e);
            System.exit(1);
        }
    }
}
