package com.shra012.springkafkaclients.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.shra012.springkafkaclients.util.KafkaClientsUtil;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeliveryAddress {

    @JsonProperty("AddressLine")
    private String addressLine;
    @JsonProperty("City")
    private String city;
    @JsonProperty("State")
    private String state;
    @JsonProperty("PinCode")
    private String pinCode;
    @JsonProperty("ContactNumber")
    private String contactNumber;

    @Override
    public String toString() {
        return KafkaClientsUtil.toJSONString(this);
    }

}
