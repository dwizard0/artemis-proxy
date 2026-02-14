package com.ussveritas.artemis.proxy.config;
import org.slf4j.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
public class ConfigFileReader {
    private static final Logger log = LoggerFactory.getLogger(ConfigFileReader.class);
    private static final String CONFIG_FILE = "udp_targets.txt";
    public static List<String> readUdpTargets() {
        List<String> targets = new ArrayList<>();
        Path configPath = Paths.get(CONFIG_FILE);
        if (!Files.exists(configPath)) {
            log.warn("UDP targets file not found. Using default: 192.168.1.209");
            targets.add("192.168.1.209");
            return targets;
        }
        try (BufferedReader reader = Files.newBufferedReader(configPath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                targets.add(line);
                log.info("Added UDP target: {}", line);
            }
        } catch (IOException e) {
            log.error("Error reading UDP targets file", e);
            targets.add("192.168.1.209");
        }
        if (targets.isEmpty()) {
            log.warn("No UDP targets found. Using default: 192.168.1.209");
            targets.add("192.168.1.209");
        }
        return targets;
    }
}
