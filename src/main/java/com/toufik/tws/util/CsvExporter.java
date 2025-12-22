package com.toufik.tws.util;

import com.toufik.tws.dto.HistoricalBar;
import com.toufik.tws.dto.HistoricalDataResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@Component
public class CsvExporter {

    public String exportToCsv(HistoricalDataResponse data, String customPath) {
        String filename = customPath != null ? customPath : generateFilename(data);

        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            // Write header
            writer.println("Ticker|Date|Time|Open|High|Low|Close|Volume|Count|WAP");

            // Write data
            for (HistoricalBar bar : data.getBars()) {
                writer.printf("%s|%s|%s|%.2f|%.2f|%.2f|%.2f|%d|%d|%.2f%n",
                    bar.getTicker(),
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

            log.info("CSV exported successfully to: {}", filename);
            return filename;

        } catch (IOException e) {
            log.error("Failed to export CSV", e);
            throw new RuntimeException("Failed to export CSV: " + e.getMessage(), e);
        }
    }

    private String generateFilename(HistoricalDataResponse data) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());

        // Clean bar size and duration for filename
        String barSize = data.getBarSize().replace(" ", "").replace("/", "");
        String duration = data.getDuration().replace(" ", "");

        return String.format("./data/%s_%s_%s_%s.csv",
            data.getTicker(),
            barSize,
            duration,
            timestamp
        );
    }
}
