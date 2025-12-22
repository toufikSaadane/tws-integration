package com.toufik.tws.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "tws")
public class TwsProperties {
    private String host;
    private Integer port;
    private Integer clientId;
    private Integer timeout;
}
