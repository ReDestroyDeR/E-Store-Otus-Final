package ru.red.productservice.dto;

import lombok.Data;


@Data
public class ProductAdditionDTO {
    private String name;
    private Integer addition;
    private Integer pricePerUnit;

    public void setAddition(Integer addition) {
        if (addition < 0) {
            throw new IllegalArgumentException("Negative argument");
        }

        this.addition = addition;
    }
}
