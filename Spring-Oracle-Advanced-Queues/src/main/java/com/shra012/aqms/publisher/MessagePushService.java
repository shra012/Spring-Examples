package com.shra012.aqms.publisher;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class MessagePushService {

    public static final String QUEUE = "my_queue";
    private final JmsTemplate oracle1JmsTemplate;
    private final JmsTemplate oracle2JmsTemplate;
    private final Random random;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    public MessagePushService(JmsTemplate oracle1JmsTemplate, JmsTemplate oracle2JmsTemplate) {
        this.oracle1JmsTemplate = oracle1JmsTemplate;
        this.oracle2JmsTemplate = oracle2JmsTemplate;
        this.random = new Random();
    }

    @Scheduled(fixedRate = 50)
    public void random() {
        pushMessageToRandomQueue("random message at %s".formatted(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)));
    }

    public void pushMessageToRandomQueue(String message) {
        try {
            // Submit a task to push the message
            executor.submit(() -> {
                try {
                    // Randomly pick a queue (0 -> Database 1, 1 -> Database 2)
                    if (random.nextInt(2) == 0) {
                        sendMessageToOracle1(message);
                    } else {
                        sendMessageToOracle2(message);
                    }
                    log.info("Message successfully sent!");
                } catch (Exception e) {
                    log.info("Error sending message, retrying...");
                }
            });

        } catch (Exception e) {
            log.info("Message sending task timed out or failed: " + e.getMessage());
        }
    }

    // Sends a message to Database 1
    private void sendMessageToOracle1(String message) {
        oracle1JmsTemplate.convertAndSend(QUEUE, message);
        log.info("Message sent to Oracle 1 queue.");
    }

    // Sends a message to Database 2
    private void sendMessageToOracle2(String message) {
        oracle2JmsTemplate.convertAndSend(QUEUE, message);
        log.info("Message sent to Oracle 2 queue.");
    }
}
