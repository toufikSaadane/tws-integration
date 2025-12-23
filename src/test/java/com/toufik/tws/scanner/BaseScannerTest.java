package com.toufik.tws.scanner;

import com.ib.client.ContractDetails;
import com.ib.client.EClientSocket;
import com.toufik.tws.wrapper.TwsWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class BaseScannerTest {

    @Mock
    private EClientSocket clientSocket;

    @Mock
    private TwsWrapper wrapper;

    private BaseScanner testScanner;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testScanner = new BaseScanner() {
            @Override
            protected String getScanCode() {
                return "TEST_SCAN_CODE";
            }
        };
    }

    @Test
    void testScanReturnsEmptyListWhenNoResults() {
        // Arrange
        CountDownLatch latch = new CountDownLatch(0);
        when(wrapper.getScannerLatch(anyInt())).thenReturn(latch);
        when(wrapper.getScannerData(anyInt())).thenReturn(new ArrayList<>());

        // Act
        List<String> results = testScanner.scan(clientSocket, wrapper);

        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(wrapper).prepareScannerRequest(anyInt());
        verify(wrapper).getScannerLatch(anyInt());
        verify(wrapper).getScannerData(anyInt());
        verify(wrapper).clearScannerData(anyInt());
    }

    @Test
    void testGetScanCodeReturnsCorrectCode() {
        assertEquals("TEST_SCAN_CODE", testScanner.getScanCode());
    }
}
