package com.toufik.tws.scanner;

import org.springframework.stereotype.Component;

@Component
public class HotByVolumeScanner extends BaseScanner {

    @Override
    protected String getScanCode() {
        return "HOT_BY_VOLUME";
    }
}
