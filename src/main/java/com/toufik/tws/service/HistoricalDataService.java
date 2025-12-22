package com.toufik.tws.service;

import com.ib.client.Bar;
import com.ib.client.Contract;
import com.toufik.tws.dto.HistoricalBar;
import com.toufik.tws.dto.HistoricalDataRequest;
import com.toufik.tws.dto.HistoricalDataResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoricalDataService {

    private final TwsConnectionService connectionService;
    private final AtomicInteger requestIdCounter = new AtomicInteger(1000);

    public HistoricalDataResponse getHistoricalData(HistoricalDataRequest request) {
        int reqId = requestIdCounter.incrementAndGet();

        try {
            // Ensure connection
            if (!connectionService.isConnected()) {
                log.info("Not connected, establishing connection...");
                connectionService.connect();
            }

            // Create contract
            Contract contract = new Contract();
            contract.symbol(request.getTicker());
            contract.secType(request.getSecType());
            contract.currency(request.getCurrency());
            contract.exchange(request.getExchange());
            if (request.getPrimaryExch() != null && !request.getPrimaryExch().isEmpty()) {
                contract.primaryExch(request.getPrimaryExch());
            }

            // Prepare wrapper for historical data
            connectionService.getWrapper().prepareHistoricalDataRequest(reqId);

            // Get current date/time if not specified
            String endDateTime = request.getEndDateTime();
            if (endDateTime == null || endDateTime.isEmpty()) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
                endDateTime = sdf.format(new Date()) + " US/Eastern";
            }

            // Convert period format: "1M" -> "1 M", "5D" -> "5 D"
            String period = request.getPeriod();
            if (period != null && !period.contains(" ")) {
                // Add space before last character if not present
                period = period.substring(0, period.length() - 1) + " " + period.substring(period.length() - 1);
            }

            // Convert interval format: "1d" -> "1 day", "1m" -> "1 min", "5m" -> "5 mins"
            String interval = convertIntervalFormat(request.getInterval());

            log.info("Requesting historical data for {} - Interval: {}, Period: {}",
                request.getTicker(), interval, period);

            // Request historical data
            connectionService.getClientSocket().reqHistoricalData(
                reqId,
                contract,
                endDateTime,
                period,
                interval,
                request.getWhatToShow(),
                request.getUseRth() ? 1 : 0,
                1, // formatDate: 1 = yyyyMMdd HH:mm:ss
                false, // keepUpToDate
                null // chartOptions
            );

            // Wait for data
            boolean received = connectionService.getWrapper()
                .getHistoricalDataLatch(reqId)
                .await(30, TimeUnit.SECONDS);

            if (!received) {
                throw new RuntimeException("Timeout waiting for historical data");
            }

            // Get the data
            List<Bar> bars = connectionService.getWrapper().getHistoricalData(reqId);
            String startDate = connectionService.getWrapper().getHistoricalDataStartDate(reqId);
            String endDate = connectionService.getWrapper().getHistoricalDataEndDate(reqId);

            // Convert to response
            List<HistoricalBar> historicalBars = bars.stream()
                .map(bar -> convertToHistoricalBar(request.getTicker(), bar))
                .collect(Collectors.toList());

            // Clean up
            connectionService.getWrapper().clearHistoricalData(reqId);

            return HistoricalDataResponse.builder()
                .ticker(request.getTicker())
                .barSize(request.getInterval())
                .duration(request.getPeriod())
                .startDate(startDate)
                .endDate(endDate)
                .bars(historicalBars)
                .totalBars(historicalBars.size())
                .build();

        } catch (Exception e) {
            log.error("Error fetching historical data", e);
            throw new RuntimeException("Failed to fetch historical data: " + e.getMessage(), e);
        }
    }

    private HistoricalBar convertToHistoricalBar(String ticker, Bar bar) {
        String[] dateTimeParts = bar.time().split(" ");
        String date = dateTimeParts.length > 0 ? dateTimeParts[0] : bar.time();
        String time = dateTimeParts.length > 1 ? dateTimeParts[1] : "";

        return HistoricalBar.builder()
            .ticker(ticker)
            .date(date)
            .time(time)
            .open(bar.open())
            .high(bar.high())
            .low(bar.low())
            .close(bar.close())
            .volume(bar.volume().longValue())
            .count(bar.count())
            .wap(bar.wap().value().doubleValue())
            .build();
    }

    private String convertIntervalFormat(String interval) {
        if (interval == null || interval.contains(" ")) {
            return interval; // Already in correct format or null
        }

        // Extract number and unit
        String num = interval.replaceAll("[^0-9]", "");
        String unit = interval.replaceAll("[0-9]", "").toLowerCase();

        switch (unit) {
            case "d":
                return num + " day";
            case "m":
                return num.equals("1") ? num + " min" : num + " mins";
            case "h":
                return num.equals("1") ? num + " hour" : num + " hours";
            case "w":
                return num + " week";
            case "s":
                return num.equals("1") ? num + " sec" : num + " secs";
            default:
                return interval; // Return as-is if unrecognized
        }
    }
}
