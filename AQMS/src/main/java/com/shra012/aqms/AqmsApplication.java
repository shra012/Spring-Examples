package com.shra012.aqms;

import com.shra012.aqms.configuration.AQMSProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(AQMSProperties.class)
public class AqmsApplication {

	public static void main(String[] args) {
		SpringApplication.run(AqmsApplication.class, args);
	}
}
