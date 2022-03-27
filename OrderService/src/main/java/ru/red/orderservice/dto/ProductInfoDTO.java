package ru.red.orderservice.dto;

import lombok.Data;

@Data
public class ProductInfoDTO {
    private String name;
    private Integer quantity;
    private Integer pricePerUnit;

    public ProductInfoDTO() {
    }

    public ProductInfoDTO(String name, Integer quantity, Integer pricePerUnit) {
        this.name = name;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setPricePerUnit(Integer pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    public Integer getTotalPrice() {
        return this.quantity * this.pricePerUnit;
    }
}
