package com.ussveritas.artemis.proxy.proxy;

import org.slf4j.*;

import java.io.*;
import java.net.*;

public class ProxyConnection {
    private static final Logger log = LoggerFactory.getLogger(ProxyConnection.class);
    private static final int BUFFER_SIZE = 8192;
    
    private final Socket clientSocket;
    private final String upstreamHost;
    private final int upstreamPort;
    
    private Socket serverSocket;
    
    public ProxyConnection(Socket clientSocket, String upstreamHost, int upstreamPort) {
        this.clientSocket = clientSocket;
        this.upstreamHost = upstreamHost;
        this.upstreamPort = upstreamPort;
    }
    
    public void start() {
        try {
            serverSocket = new Socket(upstreamHost, upstreamPort);
            log.info("Connected to upstream server {}:{}", upstreamHost, upstreamPort);
            
            Thread clientToServer = new Thread(() -> forward(clientSocket, serverSocket, "C->S"));
            Thread serverToClient = new Thread(() -> forward(serverSocket, clientSocket, "S->C"));
            
            clientToServer.start();
            serverToClient.start();
            
            clientToServer.join();
            serverToClient.join();
            
        } catch (Exception e) {
            log.error("Proxy connection error", e);
        } finally {
            close();
        }
    }
    
    private void forward(Socket from, Socket to, String direction) {
        try (InputStream in = from.getInputStream();
             OutputStream out = to.getOutputStream()) {
            
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                out.flush();
            }
            
        } catch (IOException e) {
            log.debug("{} stream closed: {}", direction, e.getMessage());
        }
    }
    
    public void close() {
        closeSocket(clientSocket, "client");
        closeSocket(serverSocket, "server");
    }
    
    private void closeSocket(Socket socket, String name) {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
                log.debug("Closed {} socket", name);
            } catch (IOException e) {
                log.debug("Error closing {} socket", name, e);
            }
        }
    }
}
