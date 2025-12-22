package com.toufik.tws.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TwsConnectionConfig {
    private String host;
    private Integer port;
    private Integer clientId;
}
