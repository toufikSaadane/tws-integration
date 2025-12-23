package com.toufik.tws.scanner;

import org.springframework.stereotype.Component;

@Component
public class MostActiveScanner extends BaseScanner {

    @Override
    protected String getScanCode() {
        return "MOST_ACTIVE";
    }
}
