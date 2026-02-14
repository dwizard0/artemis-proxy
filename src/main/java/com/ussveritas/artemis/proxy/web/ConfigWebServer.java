package com.ussveritas.artemis.proxy.web;

import com.ussveritas.artemis.proxy.*;
import com.ussveritas.artemis.proxy.config.*;
import com.sun.net.httpserver.*;
import com.fasterxml.jackson.databind.*;
import org.slf4j.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class ConfigWebServer {
    private static final Logger log = LoggerFactory.getLogger(ConfigWebServer.class);
    
    private final ArtemisProxyService service;
    private final ConfigurationManager configManager;
    private final ObjectMapper mapper;
    
    private HttpServer httpServer;
    
    public ConfigWebServer(ArtemisProxyService service, ConfigurationManager configManager) {
        this.service = service;
        this.configManager = configManager;
        this.mapper = new ObjectMapper();
    }
    
    public void start(int port) throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        
        httpServer.createContext("/", this::handleIndex);
        httpServer.createContext("/api/config", this::handleConfig);
        httpServer.createContext("/api/restart", this::handleRestart);
        
        httpServer.setExecutor(null);
        httpServer.start();
        
        log.info("Web config server started on port {}", port);
    }
    
    public void stop() {
        if (httpServer != null) {
            httpServer.stop(0);
        }
    }
    
    private void handleIndex(HttpExchange exchange) throws IOException {
        String html = generateConfigPage();
        sendResponse(exchange, 200, html, "text/html");
    }
    
    private void handleConfig(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            String json = mapper.writeValueAsString(configManager.get());
            sendResponse(exchange, 200, json, "application/json");
            
        } else if ("POST".equals(exchange.getRequestMethod())) {
            String body = new String(exchange.getRequestBody().readAllBytes());
            ProxyConfiguration newConfig = mapper.readValue(body, ProxyConfiguration.class);
            
            service.applyConfiguration(newConfig);
            
            sendResponse(exchange, 200, "{\"status\":\"ok\"}", "application/json");
        }
    }
    
    private void handleRestart(HttpExchange exchange) throws IOException {
        new Thread(() -> {
            try {
                service.stop();
                Thread.sleep(1000);
                service.start();
            } catch (Exception e) {
                log.error("Restart failed", e);
            }
        }).start();
        
        sendResponse(exchange, 200, "{\"status\":\"restarting\"}", "application/json");
    }
    
    private void sendResponse(HttpExchange exchange, int code, String response, String contentType) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(code, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
    
    private String generateConfigPage() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Artemis Proxy Config</title>
                <style>
                    body{font-family:Arial;margin:40px;background:#000;color:#ff9900;}
                    h1{color:#ff9900;border-bottom:3px solid #ff9900;padding-bottom:10px;}
                    label{display:block;margin:15px 0 5px;color:#cc99ff;font-weight:bold;}
                    input,select{width:300px;padding:8px;background:#222;color:#fff;border:2px solid #ff9900;border-radius:5px;}
                    input[type=checkbox]{width:auto;margin-right:10px;}
                    button{margin:20px 10px 0 0;padding:12px 24px;background:#ff9900;color:#000;border:none;border-radius:5px;cursor:pointer;font-weight:bold;}
                    button:hover{background:#ffaa33;}
                    .status{margin-top:30px;padding:15px;background:#222;border-left:4px solid #00ff00;color:#00ff00;}
                </style>
            </head>
            <body>
                <h1>ARTEMIS PROXY CONFIGURATION</h1>
                <form id="configForm">
                    <label>Upstream Host:</label>
                    <input type="text" id="upstreamHost" value="192.168.1.209">
                    
                    <label>Upstream Port:</label>
                    <input type="number" id="upstreamPort" value="2010">
                    
                    <label>Proxy Listen Port:</label>
                    <input type="number" id="proxyListenPort" value="2011">
                    
                    <label>Target Ship:</label>
                    <select id="targetShip">
                        <option value="0">Artemis (0)</option>
                        <option value="1">Intrepid (1)</option>
                        <option value="2">Aegis (2)</option>
                        <option value="3">Horatio (3)</option>
                        <option value="4">Excalibur (4)</option>
                        <option value="5">Hera (5)</option>
                        <option value="6">Ceres (6)</option>
                        <option value="7">Diana (7)</option>
                    </select>
                    
                    <label><input type="checkbox" id="engineeringEnabled" checked> Engineering Ghost</label>
                    <label><input type="checkbox" id="weaponsEnabled" checked> Weapons Ghost</label>
                    <label><input type="checkbox" id="commsEnabled" checked> Comms Ghost</label>
                    
                    <label>JSON Output File:</label>
                    <input type="text" id="jsonOutputFile" value="artemis_proxy_status.json">
                    
                    <label>UDP Port:</label>
                    <input type="number" id="udpPort" value="50200">
                    
                    <label>Web Port:</label>
                    <input type="number" id="webPort" value="81">
                    
                    <div>
                        <button type="submit">Apply Configuration</button>
                        <button type="button" onclick="restart()">Restart Service</button>
                    </div>
                </form>
                
                <div id="status" class="status" style="display:none;"></div>
                
                <script>
                    fetch('/api/config')
                        .then(r=>r.json())
                        .then(c=>{
                            document.getElementById('upstreamHost').value=c.upstreamHost;
                            document.getElementById('upstreamPort').value=c.upstreamPort;
                            document.getElementById('proxyListenPort').value=c.proxyListenPort;
                            document.getElementById('targetShip').value=c.targetShip;
                            document.getElementById('engineeringEnabled').checked=c.engineeringEnabled;
                            document.getElementById('weaponsEnabled').checked=c.weaponsEnabled;
                            document.getElementById('commsEnabled').checked=c.commsEnabled;
                            document.getElementById('jsonOutputFile').value=c.jsonOutputFile;
                            document.getElementById('udpPort').value=c.udpPort;
                            document.getElementById('webPort').value=c.webPort;
                        });
                    
                    document.getElementById('configForm').onsubmit=function(e){
                        e.preventDefault();
                        const config={
                            upstreamHost:document.getElementById('upstreamHost').value,
                            upstreamPort:parseInt(document.getElementById('upstreamPort').value),
                            proxyListenPort:parseInt(document.getElementById('proxyListenPort').value),
                            targetShip:parseInt(document.getElementById('targetShip').value),
                            engineeringEnabled:document.getElementById('engineeringEnabled').checked,
                            weaponsEnabled:document.getElementById('weaponsEnabled').checked,
                            commsEnabled:document.getElementById('commsEnabled').checked,
                            jsonOutputFile:document.getElementById('jsonOutputFile').value,
                            udpPort:parseInt(document.getElementById('udpPort').value),
                            webPort:parseInt(document.getElementById('webPort').value)
                        };
                        fetch('/api/config',{
                            method:'POST',
                            headers:{'Content-Type':'application/json'},
                            body:JSON.stringify(config)
                        }).then(()=>{
                            const s=document.getElementById('status');
                            s.textContent='Configuration applied successfully!';
                            s.style.display='block';
                            setTimeout(()=>s.style.display='none',3000);
                        });
                    };
                    
                    function restart(){
                        fetch('/api/restart',{method:'POST'})
                            .then(()=>alert('Service restarting...'));
                    }
                </script>
            </body>
            </html>
            """;
    }
}
