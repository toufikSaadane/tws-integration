package com.toufik.tws.runner;

import com.toufik.tws.dto.HistoricalBar;
import com.toufik.tws.dto.HistoricalDataRequest;
import com.toufik.tws.dto.HistoricalDataResponse;
import com.toufik.tws.service.HistoricalDataService;
import com.toufik.tws.util.CsvExporter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "tws.cli.historical.enabled", havingValue = "true")
public class HistoricalDataRunner implements ApplicationRunner {

    private final HistoricalDataService historicalDataService;
    private final CsvExporter csvExporter;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("=== Historical Data CLI ===");

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        // Interactive prompts
        System.out.println("\n=== Historical Data Fetcher ===\n");

        System.out.print("Ticker: ");
        String ticker = reader.readLine().trim().toUpperCase();

        System.out.print("Period (e.g., 1D, 5D, 1M, 1Y): ");
        String period = reader.readLine().trim();

        System.out.print("Interval (e.g., 1d, 1m, 5m): ");
        String interval = reader.readLine().trim();

        // Build request
        HistoricalDataRequest request = HistoricalDataRequest.builder()
            .ticker(ticker)
            .period(period)
            .interval(interval)
            .whatToShow("TRADES")
            .useRth(true)
            .build();

        System.out.println("\n=== Fetching Historical Data ===");
        System.out.println("Ticker:      " + ticker);
        System.out.println("Period:      " + period);
        System.out.println("Interval:    " + interval);
        System.out.println("================================\n");

        // Fetch data
        HistoricalDataResponse response = historicalDataService.getHistoricalData(request);
        // Display data
        displayData(response);
        // Ask to save CSV
        promptAndSaveCsv(response, reader);
        System.out.println("\nDone!");
        System.exit(0);
    }

    private void displayData(HistoricalDataResponse response) {
        System.out.println("\n=== Historical Data Retrieved ===");
        System.out.println("Total Bars: " + response.getTotalBars());
        System.out.println("Start Date: " + response.getStartDate());
        System.out.println("End Date:   " + response.getEndDate());
        System.out.println("\n");

        // Display table header
        System.out.printf("%-12s %-12s %-10s %-10s %-10s %-10s %-12s %-8s %-10s%n",
            "Date", "Time", "Open", "High", "Low", "Close", "Volume", "Count", "WAP");
        System.out.println("=".repeat(110));

        // Display first 10 and last 10 rows (or all if less than 20)
        int total = response.getBars().size();
        int displayCount = Math.min(10, total);

        for (int i = 0; i < displayCount; i++) {
            displayBar(response.getBars().get(i));
        }

        if (total > 20) {
            System.out.println("... (" + (total - 20) + " rows omitted) ...");

            for (int i = total - 10; i < total; i++) {
                displayBar(response.getBars().get(i));
            }
        } else if (total > 10) {
            for (int i = 10; i < total; i++) {
                displayBar(response.getBars().get(i));
            }
        }

        System.out.println("=".repeat(110));
    }

    private void displayBar(HistoricalBar bar) {
        System.out.printf("%-12s %-12s %-10.2f %-10.2f %-10.2f %-10.2f %-12d %-8d %-10.2f%n",
            bar.getDate(),
            bar.getTime(),
            bar.getOpen(),
            bar.getHigh(),
            bar.getLow(),
            bar.getClose(),
            bar.getVolume(),
            bar.getCount(),
            bar.getWap()
        );
    }

    private void promptAndSaveCsv(HistoricalDataResponse response, BufferedReader reader) {
        try {
            System.out.print("\nSave to CSV? (y/n): ");
            String answer = reader.readLine().trim().toLowerCase();

            if ("y".equals(answer) || "yes".equals(answer)) {
                // Create data directory if it doesn't exist
                new File("./data").mkdirs();

                // Generate default filename
                String defaultFilename = csvExporter.exportToCsv(response, null);

                System.out.println("\n✓ CSV saved to: " + defaultFilename);
            } else {
                System.out.println("\nCSV export skipped.");
            }
        } catch (Exception e) {
            log.error("Error during CSV save prompt", e);
        }
    }
}
