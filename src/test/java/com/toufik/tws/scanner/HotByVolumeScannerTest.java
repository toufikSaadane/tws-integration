package com.toufik.tws.scanner;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HotByVolumeScannerTest {

    @Test
    void testGetScanCode() {
        HotByVolumeScanner scanner = new HotByVolumeScanner();
        assertEquals("HOT_BY_VOLUME", scanner.getScanCode());
    }
}
