package com.shra012.springkafkaclients.generator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shra012.springkafkaclients.exception.KafkaProducerException;
import com.shra012.springkafkaclients.model.DeliveryAddress;
import com.shra012.springkafkaclients.model.DeliveryType;
import com.shra012.springkafkaclients.model.LineItem;
import com.shra012.springkafkaclients.model.PosInvoice;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Log4j2
public class InvoiceGeneratorImpl implements InvoiceGenerator {

    private static final String DETAILED_MESSAGE = "Exception occurred while getting invoice details";
    private final List<DeliveryAddress> deliveryAddresses;
    private final List<PosInvoice> posInvoices;
    private final List<LineItem> lineItems;
    private final Random random = new Random();


    public InvoiceGeneratorImpl(ObjectMapper objectMapper) {
        var addressResource = new ClassPathResource("data/address.json");
        var invoiceResource = new ClassPathResource("data/invoice.json");
        var productsResource = new ClassPathResource("data/products.json");
        try {
            this.deliveryAddresses = objectMapper.readValue(addressResource.getInputStream(), new TypeReference<>() {
            });
            this.posInvoices = objectMapper.readValue(invoiceResource.getInputStream(), new TypeReference<>() {
            });
            this.lineItems = objectMapper.readValue(productsResource.getInputStream(), new TypeReference<>() {
            });
        } catch (IOException e) {
            log.error(DETAILED_MESSAGE, e);
            throw new KafkaProducerException(DETAILED_MESSAGE, e);
        }
    }

    @Override
    public PosInvoice getNextInvoice() {
        var deliveryAddressIndex = random.nextInt(deliveryAddresses.size());
        var posInvoiceIndex = random.nextInt(posInvoices.size());
        var posInvoice = posInvoices.get(posInvoiceIndex);
        if(DeliveryType.HOME_DELIVERY.getValue().equals(posInvoice.getDeliveryType())) {
            var deliveryAddress = deliveryAddresses.get(deliveryAddressIndex);
            posInvoice.setDeliveryAddress(deliveryAddress);
        }
        var invoiceLineItems = IntStream
                .range(0, random.nextInt(10))
                .mapToObj(lineItems::get)
                .collect(Collectors.toList());
        posInvoice.setInvoiceLineItems(invoiceLineItems);
        return posInvoice;
    }

}
