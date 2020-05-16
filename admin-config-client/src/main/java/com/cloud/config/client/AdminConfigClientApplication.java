package com.cloud.config.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class AdminConfigClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(AdminConfigClientApplication.class, args);
	}

}
