package com.shra012.springkafkaclients.resources;

import com.shra012.springkafkaclients.configuration.KafkaClientsConfiguration;
import com.shra012.springkafkaclients.generator.InvoiceGenerator;
import com.shra012.springkafkaclients.model.SimpleRequest;
import com.shra012.springkafkaclients.model.SimpleResponse;
import com.shra012.springkafkaclients.producer.JsonValueKafkaProducerService;
import com.shra012.springkafkaclients.producer.StringValueKafkaProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;

@RestController
@RequestMapping("producer")
public class ProduceDataResource {

    @Autowired
    private StringValueKafkaProducerService stringValueKafkaProducerService;

    @Autowired
    private JsonValueKafkaProducerService jsonValueKafkaProducerService;

    @Autowired
    private InvoiceGenerator invoiceGenerator;

    @Autowired
    private KafkaClientsConfiguration kafkaClientsConfiguration;

    private BigInteger invoiceCount = BigInteger.ONE;

    @PostMapping(path = "simple")
    public ResponseEntity<SimpleResponse> produceSimpleMessage(@RequestBody SimpleRequest simpleRequest) {
        var simpleResponse = stringValueKafkaProducerService.sendMessage(simpleRequest.getKey(), simpleRequest.getValue());
        return ResponseEntity.ok(simpleResponse);
    }

    @PutMapping(path = "invoice")
    public ResponseEntity<SimpleResponse> produceInvoiceMessage() {
        var invoiceMessage = MessageBuilder
                .withPayload(invoiceGenerator.getNextInvoice())
                .setHeader(KafkaHeaders.MESSAGE_KEY, String.format("invoice.key.%s", invoiceCount.toString()))
                .setHeader(KafkaHeaders.TOPIC, kafkaClientsConfiguration.getInvoiceTopic())
                .build();
        var simpleResponse = jsonValueKafkaProducerService.sendMessage(invoiceMessage);
        this.invoiceCount = invoiceCount.add(BigInteger.ONE);
        return ResponseEntity.ok(simpleResponse);
    }
}
