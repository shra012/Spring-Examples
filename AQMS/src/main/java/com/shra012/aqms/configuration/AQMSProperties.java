package com.shra012.aqms.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = AQMSProperties.AQMS)
public class AQMSProperties {
    public static final String AQMS = "aqms";
    private final Map<String, DataSourceProperties> datasource;
}
