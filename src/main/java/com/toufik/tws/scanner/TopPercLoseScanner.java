package com.toufik.tws.scanner;

import org.springframework.stereotype.Component;

@Component
public class TopPercLoseScanner extends BaseScanner {

    @Override
    protected String getScanCode() {
        return "TOP_PERC_LOSE";
    }
}
