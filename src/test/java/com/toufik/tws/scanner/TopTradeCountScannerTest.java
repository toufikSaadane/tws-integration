package com.toufik.tws.scanner;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TopTradeCountScannerTest {

    @Test
    void testGetScanCode() {
        TopTradeCountScanner scanner = new TopTradeCountScanner();
        assertEquals("TOP_TRADE_COUNT", scanner.getScanCode());
    }
}
