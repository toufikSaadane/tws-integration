package com.toufik.tws.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoricalBar {
    private String ticker;
    private String date;
    private String time;
    private double open;
    private double high;
    private double low;
    private double close;
    private long volume;
    private int count;
    private double wap; // Weighted Average Price
}
