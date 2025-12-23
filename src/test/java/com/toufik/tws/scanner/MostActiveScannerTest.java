package com.toufik.tws.scanner;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MostActiveScannerTest {

    @Test
    void testGetScanCode() {
        MostActiveScanner scanner = new MostActiveScanner();
        assertEquals("MOST_ACTIVE", scanner.getScanCode());
    }
}
