package com.toufik.tws.scanner;

import com.ib.client.ContractDetails;
import com.ib.client.EClientSocket;
import com.ib.client.ScannerSubscription;
import com.toufik.tws.wrapper.TwsWrapper;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public abstract class BaseScanner {

    private static final int SCANNER_REQUEST_ID_START = 10000;
    private static final AtomicInteger REQUEST_ID_COUNTER = new AtomicInteger(SCANNER_REQUEST_ID_START);
    private static final int TIMEOUT_SECONDS = 30;

    protected abstract String getScanCode();

    public List<String> scan(EClientSocket clientSocket, TwsWrapper wrapper) {
        int reqId = REQUEST_ID_COUNTER.incrementAndGet();
        log.info("Starting scanner: {} with reqId: {}", getScanCode(), reqId);

        // Prepare wrapper to receive scanner data
        wrapper.prepareScannerRequest(reqId);

        // Create scanner subscription
        ScannerSubscription subscription = new ScannerSubscription();
        subscription.instrument("STK");
        subscription.locationCode("STK.US");
        subscription.scanCode(getScanCode());
        subscription.numberOfRows(10);

        try {
            // Request scanner data
            clientSocket.reqScannerSubscription(reqId, subscription, null, null);

            // Wait for scanner data to complete
            CountDownLatch latch = wrapper.getScannerLatch(reqId);
            boolean completed = latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (!completed) {
                log.warn("Scanner {} timed out after {} seconds", getScanCode(), TIMEOUT_SECONDS);
            }

            // Cancel scanner subscription
            clientSocket.cancelScannerSubscription(reqId);

            // Get results
            List<ContractDetails> results = wrapper.getScannerData(reqId);
            List<String> symbols = new ArrayList<>();
            for (ContractDetails details : results) {
                if (details.contract() != null && details.contract().symbol() != null) {
                    symbols.add(details.contract().symbol());
                }
            }

            log.info("Scanner {} completed with {} results", getScanCode(), symbols.size());
            return symbols;

        } catch (Exception e) {
            log.error("Error running scanner: {}", getScanCode(), e);
            return new ArrayList<>();
        } finally {
            // Clean up
            wrapper.clearScannerData(reqId);
        }
    }
}
