package com.ussveritas.artemis.proxy.proxy;

import com.ussveritas.artemis.proxy.config.*;
import org.slf4j.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ProxyServer {
    private static final Logger log = LoggerFactory.getLogger(ProxyServer.class);
    
    private final ConfigurationManager configManager;
    private final ExecutorService executor;
    private final List<ProxyConnection> connections;
    
    private ServerSocket serverSocket;
    private Thread acceptThread;
    private volatile boolean running;
    
    public ProxyServer(ConfigurationManager configManager) {
        this.configManager = configManager;
        this.executor = Executors.newCachedThreadPool();
        this.connections = new CopyOnWriteArrayList<>();
    }
    
    public void start() throws IOException {
        ProxyConfiguration config = configManager.get();
        
        serverSocket = new ServerSocket(config.proxyListenPort());
        running = true;
        
        acceptThread = new Thread(this::acceptLoop, "ProxyAcceptor");
        acceptThread.setDaemon(true);
        acceptThread.start();
        
        log.info("Proxy server listening on port {}", config.proxyListenPort());
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
        
        connections.forEach(ProxyConnection::close);
        connections.clear();
        
        executor.shutdown();
        log.info("Proxy server stopped");
    }
    
    private void acceptLoop() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                ProxyConfiguration config = configManager.get();
                
                log.info("Client connected from {}", clientSocket.getRemoteSocketAddress());
                
                ProxyConnection connection = new ProxyConnection(
                    clientSocket,
                    config.upstreamHost(),
                    config.upstreamPort()
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
