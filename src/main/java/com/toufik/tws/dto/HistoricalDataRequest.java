package com.toufik.tws.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoricalDataRequest {
    private String ticker;
    private String period;       // Duration: "1D", "5D", "1M", "1Y"
    private String interval;     // Bar size: "1 day", "1 hour", "5 mins"
    private String secType;
    private String currency;
    private String exchange;
    private String primaryExch;
    private String endDateTime;
    private String whatToShow;
    private Boolean useRth;

    // Getters with defaults
    public String getSecType() {
        return secType != null ? secType : "STK";
    }

    public String getCurrency() {
        return currency != null ? currency : "USD";
    }

    public String getExchange() {
        return exchange != null ? exchange : "SMART";
    }

    public String getWhatToShow() {
        return whatToShow != null ? whatToShow : "TRADES";
    }

    public Boolean getUseRth() {
        return useRth != null ? useRth : true;
    }
}
