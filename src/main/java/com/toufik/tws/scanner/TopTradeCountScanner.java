package com.toufik.tws.scanner;

import org.springframework.stereotype.Component;

@Component
public class TopTradeCountScanner extends BaseScanner {

    @Override
    protected String getScanCode() {
        return "TOP_TRADE_COUNT";
    }
}
