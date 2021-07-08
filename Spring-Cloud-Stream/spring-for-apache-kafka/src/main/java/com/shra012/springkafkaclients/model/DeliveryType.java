package com.shra012.springkafkaclients.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum DeliveryType {
    HOME_DELIVERY("HOME-DELIVERY"), TAKEAWAY("TAKEAWAY");
    private final String value;

    public static DeliveryType enumOf(String string) {
        return Arrays.stream(values())
                .filter(deliveryType -> deliveryType.getValue().equals(string))
                .findFirst().orElse(null);
    }
}
