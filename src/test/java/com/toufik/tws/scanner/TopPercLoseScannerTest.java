package com.toufik.tws.scanner;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TopPercLoseScannerTest {

    @Test
    void testGetScanCode() {
        TopPercLoseScanner scanner = new TopPercLoseScanner();
        assertEquals("TOP_PERC_LOSE", scanner.getScanCode());
    }
}
