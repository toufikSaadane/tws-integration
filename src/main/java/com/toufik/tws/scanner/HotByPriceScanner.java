package com.toufik.tws.scanner;

import org.springframework.stereotype.Component;

@Component
public class HotByPriceScanner extends BaseScanner {

    @Override
    protected String getScanCode() {
        return "HOT_BY_PRICE";
    }
}
