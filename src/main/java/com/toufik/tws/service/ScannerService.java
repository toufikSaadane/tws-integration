package com.toufik.tws.service;

import com.toufik.tws.scanner.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScannerService {

    private final TwsConnectionService twsConnectionService;
    private final TopPercGainScanner topPercGainScanner;
    private final TopPercLoseScanner topPercLoseScanner;
    private final MostActiveScanner mostActiveScanner;
    private final HotByVolumeScanner hotByVolumeScanner;
    private final HotByPriceScanner hotByPriceScanner;
    private final TopTradeCountScanner topTradeCountScanner;

    public List<String> runAllScannersAndGetTopStocks() {
        if (!twsConnectionService.isConnected()) {
            log.error("Cannot run scanners: TWS is not connected");
            return Collections.emptyList();
        }

        log.info("Running all scanners to find top stocks...");
        List<String> allResults = new ArrayList<>();

        // Run all scanners and collect results
        allResults.addAll(topPercGainScanner.scan(
            twsConnectionService.getClientSocket(), 
            twsConnectionService.getWrapper()
        ));
        
        allResults.addAll(topPercLoseScanner.scan(
            twsConnectionService.getClientSocket(), 
            twsConnectionService.getWrapper()
        ));
        
        allResults.addAll(mostActiveScanner.scan(
            twsConnectionService.getClientSocket(), 
            twsConnectionService.getWrapper()
        ));
        
        allResults.addAll(hotByVolumeScanner.scan(
            twsConnectionService.getClientSocket(), 
            twsConnectionService.getWrapper()
        ));
        
        allResults.addAll(hotByPriceScanner.scan(
            twsConnectionService.getClientSocket(), 
            twsConnectionService.getWrapper()
        ));
        
        allResults.addAll(topTradeCountScanner.scan(
            twsConnectionService.getClientSocket(), 
            twsConnectionService.getWrapper()
        ));

        log.info("Total results collected: {}", allResults.size());

        // Deduplicate into a set
        Set<String> uniqueStocks = new LinkedHashSet<>(allResults);
        log.info("Unique stocks after deduplication: {}", uniqueStocks.size());

        // Return up to 3 stocks
        List<String> topStocks = uniqueStocks.stream()
            .limit(3)
            .collect(Collectors.toList());

        log.info("Returning top {} stocks: {}", topStocks.size(), topStocks);
        return topStocks;
    }
}
