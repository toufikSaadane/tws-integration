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
public class ScannerResponse {
    private List<String> topStocks;
    private Integer totalScanned;
    private Integer uniqueStocks;
}
