package com.shra012.springkafkaclients;

import com.shra012.springkafkaclients.configuration.ApplicationConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SpringKafkaClientsApplicationTests {
	@Autowired
	ApplicationConfiguration applicationConfiguration;
	@Test
	void contextLoads() {
		Assertions.assertNotNull(applicationConfiguration);
	}

}
