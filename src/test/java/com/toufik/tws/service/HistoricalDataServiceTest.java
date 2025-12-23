package com.toufik.tws.service;

import com.ib.client.Bar;
import com.ib.client.Contract;
import com.ib.client.Decimal;
import com.ib.client.EClientSocket;
import com.toufik.tws.dto.HistoricalBar;
import com.toufik.tws.dto.HistoricalDataRequest;
import com.toufik.tws.dto.HistoricalDataResponse;
import com.toufik.tws.wrapper.TwsWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class HistoricalDataServiceTest {

    @Mock
    private TwsConnectionService connectionService;

    @Mock
    private EClientSocket clientSocket;

    @Mock
    private TwsWrapper wrapper;

    @Mock
    private CountDownLatch latch;

    private HistoricalDataService historicalDataService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        historicalDataService = new HistoricalDataService(connectionService);
    }

    @Test
    void testGetHistoricalDataWhenNotConnected() throws Exception {
        // Arrange
        HistoricalDataRequest request = HistoricalDataRequest.builder()
            .ticker("AAPL")
            .period("1D")
            .interval("1d")
            .build();

        when(connectionService.isConnected()).thenReturn(false);
        when(connectionService.getWrapper()).thenReturn(wrapper);
        when(connectionService.getClientSocket()).thenReturn(clientSocket);
        when(wrapper.getHistoricalDataLatch(anyInt())).thenReturn(latch);
        when(latch.await(anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(wrapper.getHistoricalData(anyInt())).thenReturn(new ArrayList<>());
        when(wrapper.getHistoricalDataStartDate(anyInt())).thenReturn("20231201");
        when(wrapper.getHistoricalDataEndDate(anyInt())).thenReturn("20231202");

        // Act
        HistoricalDataResponse response = historicalDataService.getHistoricalData(request);

        // Assert
        assertNotNull(response);
        verify(connectionService).connect();
        verify(wrapper).prepareHistoricalDataRequest(anyInt());
    }

    @Test
    void testGetHistoricalDataWithDefaultValues() throws Exception {
        // Arrange
        HistoricalDataRequest request = HistoricalDataRequest.builder()
            .ticker("AAPL")
            .period("1D")
            .interval("1d")
            .build();

        when(connectionService.isConnected()).thenReturn(true);
        when(connectionService.getWrapper()).thenReturn(wrapper);
        when(connectionService.getClientSocket()).thenReturn(clientSocket);
        when(wrapper.getHistoricalDataLatch(anyInt())).thenReturn(latch);
        when(latch.await(anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(wrapper.getHistoricalData(anyInt())).thenReturn(createMockBars());
        when(wrapper.getHistoricalDataStartDate(anyInt())).thenReturn("20231201");
        when(wrapper.getHistoricalDataEndDate(anyInt())).thenReturn("20231202");

        // Act
        HistoricalDataResponse response = historicalDataService.getHistoricalData(request);

        // Assert
        assertNotNull(response);
        assertEquals("AAPL", response.getTicker());
        assertEquals("1d", response.getBarSize());
        assertEquals("1D", response.getDuration());
        assertEquals("20231201", response.getStartDate());
        assertEquals("20231202", response.getEndDate());
        assertEquals(2, response.getTotalBars());
        assertEquals(2, response.getBars().size());

        // Verify contract was created with default values
        ArgumentCaptor<Contract> contractCaptor = ArgumentCaptor.forClass(Contract.class);
        verify(clientSocket).reqHistoricalData(
            anyInt(),
            contractCaptor.capture(),
            anyString(),
            anyString(),
            anyString(),
            anyString(),
            anyInt(),
            anyInt(),
            anyBoolean(),
            any()
        );

        Contract capturedContract = contractCaptor.getValue();
        assertEquals("AAPL", capturedContract.symbol());
        assertEquals("STK", capturedContract.secType().toString());
        assertEquals("USD", capturedContract.currency());
        assertEquals("SMART", capturedContract.exchange());
    }

    @Test
    void testGetHistoricalDataWithCustomValues() throws Exception {
        // Arrange
        HistoricalDataRequest request = HistoricalDataRequest.builder()
            .ticker("TSLA")
            .period("1M")
            .interval("1h")
            .secType("STK")
            .currency("USD")
            .exchange("NASDAQ")
            .primaryExch("NASDAQ")
            .whatToShow("TRADES")
            .useRth(false)
            .build();

        when(connectionService.isConnected()).thenReturn(true);
        when(connectionService.getWrapper()).thenReturn(wrapper);
        when(connectionService.getClientSocket()).thenReturn(clientSocket);
        when(wrapper.getHistoricalDataLatch(anyInt())).thenReturn(latch);
        when(latch.await(anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(wrapper.getHistoricalData(anyInt())).thenReturn(createMockBars());
        when(wrapper.getHistoricalDataStartDate(anyInt())).thenReturn("20231101");
        when(wrapper.getHistoricalDataEndDate(anyInt())).thenReturn("20231201");

        // Act
        HistoricalDataResponse response = historicalDataService.getHistoricalData(request);

        // Assert
        assertNotNull(response);
        assertEquals("TSLA", response.getTicker());
        assertEquals("1h", response.getBarSize());
        assertEquals("1M", response.getDuration());

        // Verify contract was created with custom values
        ArgumentCaptor<Contract> contractCaptor = ArgumentCaptor.forClass(Contract.class);
        verify(clientSocket).reqHistoricalData(
            anyInt(),
            contractCaptor.capture(),
            anyString(),
            eq("1 M"),
            eq("1 hour"),
            eq("TRADES"),
            eq(0), // useRth = false
            anyInt(),
            anyBoolean(),
            any()
        );

        Contract capturedContract = contractCaptor.getValue();
        assertEquals("TSLA", capturedContract.symbol());
        assertEquals("USD", capturedContract.currency());
        assertEquals("NASDAQ", capturedContract.exchange());
        assertEquals("NASDAQ", capturedContract.primaryExch());
    }

    @Test
    void testConvertIntervalFormat() throws Exception {
        // Test various interval formats
        testIntervalConversion("1d", "1 day");
        testIntervalConversion("5m", "5 mins");
        testIntervalConversion("1m", "1 min");
        testIntervalConversion("1h", "1 hour");
        testIntervalConversion("4h", "4 hours");
        testIntervalConversion("1w", "1 week");
        testIntervalConversion("1s", "1 sec");
        testIntervalConversion("5s", "5 secs");
    }

    private void testIntervalConversion(String input, String expected) throws Exception {
        // Arrange
        HistoricalDataRequest request = HistoricalDataRequest.builder()
            .ticker("AAPL")
            .period("1D")
            .interval(input)
            .build();

        when(connectionService.isConnected()).thenReturn(true);
        when(connectionService.getWrapper()).thenReturn(wrapper);
        when(connectionService.getClientSocket()).thenReturn(clientSocket);
        when(wrapper.getHistoricalDataLatch(anyInt())).thenReturn(latch);
        when(latch.await(anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(wrapper.getHistoricalData(anyInt())).thenReturn(new ArrayList<>());
        when(wrapper.getHistoricalDataStartDate(anyInt())).thenReturn("20231201");
        when(wrapper.getHistoricalDataEndDate(anyInt())).thenReturn("20231202");

        // Act
        historicalDataService.getHistoricalData(request);

        // Assert
        verify(clientSocket).reqHistoricalData(
            anyInt(),
            any(Contract.class),
            anyString(),
            anyString(),
            eq(expected),
            anyString(),
            anyInt(),
            anyInt(),
            anyBoolean(),
            any()
        );
    }

    @Test
    void testGetHistoricalDataTimeout() throws Exception {
        // Arrange
        HistoricalDataRequest request = HistoricalDataRequest.builder()
            .ticker("AAPL")
            .period("1D")
            .interval("1d")
            .build();

        when(connectionService.isConnected()).thenReturn(true);
        when(connectionService.getWrapper()).thenReturn(wrapper);
        when(connectionService.getClientSocket()).thenReturn(clientSocket);
        when(wrapper.getHistoricalDataLatch(anyInt())).thenReturn(latch);
        when(latch.await(anyLong(), any(TimeUnit.class))).thenReturn(false); // Timeout

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            historicalDataService.getHistoricalData(request);
        });

        assertTrue(exception.getMessage().contains("Timeout waiting for historical data"));
    }

    @Test
    void testGetHistoricalDataConvertsBarData() throws Exception {
        // Arrange
        HistoricalDataRequest request = HistoricalDataRequest.builder()
            .ticker("GOOGL")
            .period("5D")
            .interval("1d")
            .build();

        List<Bar> mockBars = new ArrayList<>();
        mockBars.add(createBar("20231201 16:00:00", 150.5, 155.0, 149.0, 154.5, 1000000, 100, 152.0));
        mockBars.add(createBar("20231202 16:00:00", 154.5, 160.0, 153.0, 158.0, 1200000, 120, 156.5));

        when(connectionService.isConnected()).thenReturn(true);
        when(connectionService.getWrapper()).thenReturn(wrapper);
        when(connectionService.getClientSocket()).thenReturn(clientSocket);
        when(wrapper.getHistoricalDataLatch(anyInt())).thenReturn(latch);
        when(latch.await(anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(wrapper.getHistoricalData(anyInt())).thenReturn(mockBars);
        when(wrapper.getHistoricalDataStartDate(anyInt())).thenReturn("20231201");
        when(wrapper.getHistoricalDataEndDate(anyInt())).thenReturn("20231202");

        // Act
        HistoricalDataResponse response = historicalDataService.getHistoricalData(request);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getBars().size());

        HistoricalBar bar1 = response.getBars().get(0);
        assertEquals("GOOGL", bar1.getTicker());
        assertEquals("20231201", bar1.getDate());
        assertEquals("16:00:00", bar1.getTime());
        assertEquals(150.5, bar1.getOpen());
        assertEquals(155.0, bar1.getHigh());
        assertEquals(149.0, bar1.getLow());
        assertEquals(154.5, bar1.getClose());
        assertEquals(1000000, bar1.getVolume());
        assertEquals(100, bar1.getCount());
        assertEquals(152.0, bar1.getWap());

        HistoricalBar bar2 = response.getBars().get(1);
        assertEquals("20231202", bar2.getDate());
        assertEquals("16:00:00", bar2.getTime());
    }

    @Test
    void testGetHistoricalDataCleansUpAfterExecution() throws Exception {
        // Arrange
        HistoricalDataRequest request = HistoricalDataRequest.builder()
            .ticker("AAPL")
            .period("1D")
            .interval("1d")
            .build();

        when(connectionService.isConnected()).thenReturn(true);
        when(connectionService.getWrapper()).thenReturn(wrapper);
        when(connectionService.getClientSocket()).thenReturn(clientSocket);
        when(wrapper.getHistoricalDataLatch(anyInt())).thenReturn(latch);
        when(latch.await(anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(wrapper.getHistoricalData(anyInt())).thenReturn(createMockBars());
        when(wrapper.getHistoricalDataStartDate(anyInt())).thenReturn("20231201");
        when(wrapper.getHistoricalDataEndDate(anyInt())).thenReturn("20231202");

        // Act
        historicalDataService.getHistoricalData(request);

        // Assert
        verify(wrapper).clearHistoricalData(anyInt());
    }

    @Test
    void testPeriodFormatConversion() throws Exception {
        // Test period format conversion
        testPeriodConversion("1D", "1 D");
        testPeriodConversion("5D", "5 D");
        testPeriodConversion("1M", "1 M");
        testPeriodConversion("1Y", "1 Y");
        testPeriodConversion("1 W", "1 W"); // Already has space
    }

    private void testPeriodConversion(String input, String expected) throws Exception {
        // Arrange
        HistoricalDataRequest request = HistoricalDataRequest.builder()
            .ticker("AAPL")
            .period(input)
            .interval("1d")
            .build();

        when(connectionService.isConnected()).thenReturn(true);
        when(connectionService.getWrapper()).thenReturn(wrapper);
        when(connectionService.getClientSocket()).thenReturn(clientSocket);
        when(wrapper.getHistoricalDataLatch(anyInt())).thenReturn(latch);
        when(latch.await(anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(wrapper.getHistoricalData(anyInt())).thenReturn(new ArrayList<>());
        when(wrapper.getHistoricalDataStartDate(anyInt())).thenReturn("20231201");
        when(wrapper.getHistoricalDataEndDate(anyInt())).thenReturn("20231202");

        // Act
        historicalDataService.getHistoricalData(request);

        // Assert
        verify(clientSocket).reqHistoricalData(
            anyInt(),
            any(Contract.class),
            anyString(),
            eq(expected),
            anyString(),
            anyString(),
            anyInt(),
            anyInt(),
            anyBoolean(),
            any()
        );
    }

    // Helper methods
    private List<Bar> createMockBars() {
        List<Bar> bars = new ArrayList<>();
        bars.add(createBar("20231201 16:00:00", 150.0, 152.0, 149.0, 151.0, 1000000, 100, 150.5));
        bars.add(createBar("20231202 16:00:00", 151.0, 153.0, 150.0, 152.0, 1100000, 110, 151.5));
        return bars;
    }

    private Bar createBar(String time, double open, double high, double low, double close, 
                          long volume, int count, double wap) {
        return new Bar(time, open, high, low, close, Decimal.get(volume), count, Decimal.get(wap));
    }
}
