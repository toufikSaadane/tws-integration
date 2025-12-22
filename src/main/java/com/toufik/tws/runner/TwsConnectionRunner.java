package com.toufik.tws.runner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.toufik.tws.dto.ConnectionStatusResponse;
import com.toufik.tws.service.TwsConnectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "tws.cli.connection.enabled", havingValue = "true")
public class TwsConnectionRunner implements CommandLineRunner {

    private final TwsConnectionService connectionService;

    @Override
    public void run(String... args) throws Exception {
        log.info("Testing TWS connection...");

        // Test connection
        ConnectionStatusResponse response = connectionService.connect();

        // Pretty print JSON response
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        String jsonResponse = mapper.writeValueAsString(response);
        System.out.println("\n=== TWS Connection Test Result ===");
        System.out.println(jsonResponse);
        System.out.println("==================================\n");

        // Disconnect
        if (response.getIsConnected()) {
            log.info("Disconnecting...");
            connectionService.disconnect();
        }

        System.exit(0);
    }
}
