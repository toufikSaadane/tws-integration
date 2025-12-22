package com.toufik.tws.scanner;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TopPercGainScannerTest {

    @Test
    void testGetScanCode() {
        TopPercGainScanner scanner = new TopPercGainScanner();
        assertEquals("TOP_PERC_GAIN", scanner.getScanCode());
    }
}
