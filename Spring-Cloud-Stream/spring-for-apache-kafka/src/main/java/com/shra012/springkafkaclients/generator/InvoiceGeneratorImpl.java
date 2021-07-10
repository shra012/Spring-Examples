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
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.String.format;

@Log4j2
public class InvoiceGeneratorImpl implements InvoiceGenerator {

    private static final String DETAILED_MESSAGE = "Exception occurred while getting invoice details";
    private static final String GENERATED_INVOICE = "Generated Invoice %s";

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
        var invoice = posInvoices.get(random.nextInt(posInvoices.size()));
        invoice.setInvoiceNumber(Integer.toString(random.nextInt(999999) + 9999));
        invoice.setCreatedTime(LocalDate.now().toEpochSecond(LocalTime.now(), ZoneOffset.ofHoursMinutes(5, 30)));
        if (DeliveryType.HOME_DELIVERY.getValue().equals(invoice.getDeliveryType())) {
            invoice.setDeliveryAddress(randomDeliveryAddress());
        }
        var randomLineItems = randomLineItems();
        invoice.setNumberOfItems(randomLineItems.size());
        invoice.setInvoiceLineItems(randomLineItems);
        var totalTaxableAmount = randomLineItems.stream().mapToDouble(LineItem::getTotalValue).reduce(0, Double::sum);
        invoice.setTaxableAmount(totalTaxableAmount);
        invoice.setSGST(totalTaxableAmount * 0.025);
        invoice.setCGST(totalTaxableAmount * 0.025);
        invoice.setCESS(totalTaxableAmount * 0.00125);
        double totalAmount = totalTaxableAmount + invoice.getSGST() + invoice.getCGST() + invoice.getCESS();
        invoice.setTotalAmount(totalAmount);
        log.debug(format(GENERATED_INVOICE, invoice));
        return invoice;
    }

    public DeliveryAddress randomDeliveryAddress() {
        var deliveryAddressIndex = random.nextInt(deliveryAddresses.size());
        return deliveryAddresses.get(deliveryAddressIndex);
    }

    private List<LineItem> randomLineItems() {
        return IntStream
                .range(0, random.nextInt(10))
                .mapToObj(this::getLineItem)
                .collect(Collectors.toList());
    }

    private LineItem getLineItem(int index) {
        var item = lineItems.get(index);
        item.setItemQty(random.nextInt(2) + 1);
        item.setTotalValue(item.getItemPrice() * item.getItemQty());
        return item;
    }

}
