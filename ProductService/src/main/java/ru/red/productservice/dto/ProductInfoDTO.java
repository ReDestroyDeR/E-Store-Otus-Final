package ru.red.productservice.dto;

import lombok.Data;

@Data
public class ProductInfoDTO {
    private String name;
    private Integer quantity;
    private Integer pricePerUnit;
}
