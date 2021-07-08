package com.shra012.springkafkaclients.generator;

import com.shra012.springkafkaclients.model.PosInvoice;

public interface InvoiceGenerator {
    PosInvoice getNextInvoice();
}
