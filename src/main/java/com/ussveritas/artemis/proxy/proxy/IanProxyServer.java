package com.ussveritas.artemis.proxy.proxy;

import com.ussveritas.artemis.proxy.config.*;
import com.ussveritas.artemis.proxy.aggregator.*;
import com.walkertribe.ian.iface.*;
import com.walkertribe.ian.protocol.*;
import org.slf4j.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class IanProxyServer {
    private static final Logger log = LoggerFactory.getLogger(IanProxyServer.class);
    
    private final ConfigurationManager configManager;
    private final SnapshotAggregator aggregator;
    private final ExecutorService executor;
    private final List<IanProxyConnection> connections;
    
    private ServerSocket serverSocket;
    private Thread acceptThread;
    private volatile boolean running;
    
    public IanProxyServer(ConfigurationManager configManager, SnapshotAggregator aggregator) {
        this.configManager = configManager;
        this.aggregator = aggregator;
        this.executor = Executors.newCachedThreadPool();
        this.connections = new CopyOnWriteArrayList<>();
    }
    
    public void start() throws IOException {
        ProxyConfiguration config = configManager.get();
        
        serverSocket = new ServerSocket(config.proxyListenPort());
        running = true;
        
        acceptThread = new Thread(this::acceptLoop, "IanProxyAcceptor");
        acceptThread.setDaemon(true);
        acceptThread.start();
        
        log.info("IAN Proxy server listening on port {}", config.proxyListenPort());
    }
    
    public void stop() {
        running = false;
        
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                log.debug("Error closing server socket", e);
            }
        }
        
        connections.forEach(IanProxyConnection::close);
        connections.clear();
        
        executor.shutdown();
        log.info("IAN Proxy server stopped");
    }
    
    private void acceptLoop() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                ProxyConfiguration config = configManager.get();
                
                log.info("Client connected from {}", clientSocket.getRemoteSocketAddress());
                
                IanProxyConnection connection = new IanProxyConnection(
                    clientSocket,
                    config.upstreamHost(),
                    config.upstreamPort(),
                    aggregator
                );
                
                connections.add(connection);
                executor.submit(() -> {
                    try {
                        connection.start();
                    } finally {
                        connections.remove(connection);
                    }
                });
                
            } catch (IOException e) {
                if (running) {
                    log.error("Error accepting connection", e);
                }
            }
        }
    }
}
