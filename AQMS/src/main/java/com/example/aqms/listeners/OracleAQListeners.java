package com.example.aqms.listeners;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;

@Slf4j
public class OracleAQListeners {

    public static final String QUEUE = "my_queue";
    public static final String EXCEPTION_OCCURRED_WHILE_PROCESSING_MESSAGE_IN_QUEUE = "Exception occurred while processing message in queue {}";

    // Listener for the queue in Database 1
    @JmsListener(destination = QUEUE, containerFactory = "oracle1JmsListenerContainerFactory")
    public void onMessageFromDb1(Message message) {
        try {
            if (message instanceof TextMessage) {
                String text = ((TextMessage) message).getText();
                System.out.println("Oracle 1: Processing message: " + text + " by thread: " + Thread.currentThread().getName());
            }
        } catch (JMSException e) {
            log.error(EXCEPTION_OCCURRED_WHILE_PROCESSING_MESSAGE_IN_QUEUE, QUEUE, e);
        }
    }

    // Listener for the queue in Database 2
    @JmsListener(destination = QUEUE, containerFactory = "oracle2JmsListenerContainerFactory")
    public void onMessageFromDb2(Message message) {
        try {
            if (message instanceof TextMessage) {
                String text = ((TextMessage) message).getText();
                System.out.println("Oracle 2: Processing message: " + text + " by thread: " + Thread.currentThread().getName());
            }
        } catch (JMSException e) {
            log.error(EXCEPTION_OCCURRED_WHILE_PROCESSING_MESSAGE_IN_QUEUE, QUEUE, e);
        }
    }
}
