package com.toufik.tws.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionStatusResponse {
    private Boolean isConnected;
    private String status;
    private TwsErrorInfo error;
    private TwsConnectionConfig config;
}
