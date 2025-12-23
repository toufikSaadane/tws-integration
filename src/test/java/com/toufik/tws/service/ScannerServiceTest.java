package com.toufik.tws.service;

import com.ib.client.EClientSocket;
import com.toufik.tws.scanner.*;
import com.toufik.tws.wrapper.TwsWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ScannerServiceTest {

    @Mock
    private TwsConnectionService twsConnectionService;

    @Mock
    private TopPercGainScanner topPercGainScanner;

    @Mock
    private TopPercLoseScanner topPercLoseScanner;

    @Mock
    private MostActiveScanner mostActiveScanner;

    @Mock
    private HotByVolumeScanner hotByVolumeScanner;

    @Mock
    private HotByPriceScanner hotByPriceScanner;

    @Mock
    private TopTradeCountScanner topTradeCountScanner;

    @Mock
    private EClientSocket clientSocket;

    @Mock
    private TwsWrapper wrapper;

    private ScannerService scannerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        scannerService = new ScannerService(
            twsConnectionService,
            topPercGainScanner,
            topPercLoseScanner,
            mostActiveScanner,
            hotByVolumeScanner,
            hotByPriceScanner,
            topTradeCountScanner
        );
    }

    @Test
    void testRunAllScannersAndGetTopStocksReturnsEmptyWhenNotConnected() {
        // Arrange
        when(twsConnectionService.isConnected()).thenReturn(false);

        // Act
        List<String> result = scannerService.runAllScannersAndGetTopStocks();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verifyNoInteractions(topPercGainScanner);
    }

    @Test
    void testRunAllScannersAndGetTopStocksReturnsUpToThreeStocks() {
        // Arrange
        when(twsConnectionService.isConnected()).thenReturn(true);
        when(twsConnectionService.getClientSocket()).thenReturn(clientSocket);
        when(twsConnectionService.getWrapper()).thenReturn(wrapper);

        when(topPercGainScanner.scan(any(), any())).thenReturn(Arrays.asList("AAPL", "GOOGL"));
        when(topPercLoseScanner.scan(any(), any())).thenReturn(Arrays.asList("TSLA"));
        when(mostActiveScanner.scan(any(), any())).thenReturn(Arrays.asList("MSFT"));
        when(hotByVolumeScanner.scan(any(), any())).thenReturn(Arrays.asList("AMZN"));
        when(hotByPriceScanner.scan(any(), any())).thenReturn(Collections.emptyList());
        when(topTradeCountScanner.scan(any(), any())).thenReturn(Collections.emptyList());

        // Act
        List<String> result = scannerService.runAllScannersAndGetTopStocks();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains("AAPL"));
        assertTrue(result.contains("GOOGL"));
        assertTrue(result.contains("TSLA"));
    }

    @Test
    void testRunAllScannersDeduplicatesStocks() {
        // Arrange
        when(twsConnectionService.isConnected()).thenReturn(true);
        when(twsConnectionService.getClientSocket()).thenReturn(clientSocket);
        when(twsConnectionService.getWrapper()).thenReturn(wrapper);

        when(topPercGainScanner.scan(any(), any())).thenReturn(Arrays.asList("AAPL", "GOOGL"));
        when(topPercLoseScanner.scan(any(), any())).thenReturn(Arrays.asList("AAPL")); // Duplicate
        when(mostActiveScanner.scan(any(), any())).thenReturn(Arrays.asList("MSFT"));
        when(hotByVolumeScanner.scan(any(), any())).thenReturn(Collections.emptyList());
        when(hotByPriceScanner.scan(any(), any())).thenReturn(Collections.emptyList());
        when(topTradeCountScanner.scan(any(), any())).thenReturn(Collections.emptyList());

        // Act
        List<String> result = scannerService.runAllScannersAndGetTopStocks();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        // Verify deduplication: AAPL appears only once
        assertEquals(1, result.stream().filter(s -> s.equals("AAPL")).count());
    }
}
