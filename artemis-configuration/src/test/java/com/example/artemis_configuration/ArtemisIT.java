package com.example.artemis_configuration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ArtemisIT {

	@Autowired
	private JmsTemplate jmsTemplate;

	@Test
	void sendAndReceiveTest() throws InterruptedException {
		jmsTemplate.convertAndSend("testQueue1", "HejHej");
		Thread.sleep(5000);
		assertEquals("HejHej", Application.resultMessage);

	}

}
