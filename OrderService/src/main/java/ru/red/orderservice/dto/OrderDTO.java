package ru.red.orderservice.dto;

import lombok.Data;

@Data
public class OrderDTO {
    private String userAddress;
    private Integer totalPrice;
}
