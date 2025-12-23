package com.toufik.tws.scanner;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HotByPriceScannerTest {

    @Test
    void testGetScanCode() {
        HotByPriceScanner scanner = new HotByPriceScanner();
        assertEquals("HOT_BY_PRICE", scanner.getScanCode());
    }
}
