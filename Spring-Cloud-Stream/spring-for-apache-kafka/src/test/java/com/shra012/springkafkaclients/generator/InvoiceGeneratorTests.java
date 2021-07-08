package com.shra012.springkafkaclients.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shra012.springkafkaclients.model.PosInvoice;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@Log4j2
@ExtendWith(MockitoExtension.class)
class InvoiceGeneratorTests {

    private final InvoiceGenerator invoiceGenerator = new InvoiceGeneratorImpl(new ObjectMapper());

    @Test
    void shouldGenerateASampleInvoice() {
        PosInvoice nextInvoice = invoiceGenerator.getNextInvoice();
        Assertions.assertNotNull(nextInvoice);
        Assertions.assertNotNull(nextInvoice.getInvoiceLineItems());
        Assertions.assertTrue(nextInvoice.getInvoiceLineItems().size() > 0);
        Assertions.assertNotNull(nextInvoice.getDeliveryAddress());
        log.info(nextInvoice);
    }
}
