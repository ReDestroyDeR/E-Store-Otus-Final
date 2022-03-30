package ru.red.deliveryservice.dto;

import lombok.Data;

@Data
public class AddressDTO {
    private String city;
    private String street;
    private String building;
    private Integer apartment;
}
