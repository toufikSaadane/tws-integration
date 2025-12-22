package com.toufik.tws.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoricalDataResponse {
    private String ticker;
    private String barSize;
    private String duration;
    private String startDate;
    private String endDate;
    private List<HistoricalBar> bars;
    private Integer totalBars;
}
