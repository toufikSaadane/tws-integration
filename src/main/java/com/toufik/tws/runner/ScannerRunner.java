package com.toufik.tws.runner;

import com.toufik.tws.service.ScannerService;
import com.toufik.tws.service.TwsConnectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "tws.cli.scanner.enabled", havingValue = "true")
public class ScannerRunner implements CommandLineRunner {

    private final TwsConnectionService connectionService;
    private final ScannerService scannerService;

    @Override
    public void run(String... args) throws Exception {
        log.info("=== Market Scanner CLI ===");

        System.out.println("\n=== Market Scanner ===");
        System.out.println("Running all scanners to find top trading opportunities...\n");

        // Ensure connection
        if (!connectionService.isConnected()) {
            System.out.println("Connecting to TWS...");
            connectionService.connect();
            
            if (!connectionService.isConnected()) {
                System.err.println("Failed to connect to TWS. Please ensure TWS/IB Gateway is running.");
                System.exit(1);
            }
            System.out.println("✓ Connected to TWS\n");
        }

        // Run all scanners
        System.out.println("Scanning markets across 6 criteria:");
        System.out.println("  - TOP_PERC_GAIN");
        System.out.println("  - TOP_PERC_LOSE");
        System.out.println("  - MOST_ACTIVE");
        System.out.println("  - HOT_BY_VOLUME");
        System.out.println("  - HOT_BY_PRICE");
        System.out.println("  - TOP_TRADE_COUNT");
        System.out.println("\nPlease wait...\n");

        List<String> topStocks = scannerService.runAllScannersAndGetTopStocks();

        // Display results
        System.out.println("=== Top Trading Opportunities ===");
        if (topStocks.isEmpty()) {
            System.out.println("No stocks found.");
        } else {
            System.out.println("Top " + topStocks.size() + " stocks:");
            for (int i = 0; i < topStocks.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + topStocks.get(i));
            }
        }
        System.out.println("=================================\n");

        // Disconnect
        log.info("Disconnecting...");
        connectionService.disconnect();

        System.out.println("Done!");
        System.exit(0);
    }
}
