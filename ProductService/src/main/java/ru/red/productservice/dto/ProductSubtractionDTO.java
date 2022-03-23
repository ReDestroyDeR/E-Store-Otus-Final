package ru.red.productservice.dto;

import lombok.Data;

@Data
public class ProductSubtractionDTO {
    private String name;
    private Integer subtraction;

    public void setSubtraction(Integer subtraction) {
        // Subtraction can't be zero because it's only allowed to create products only by addition
        if (subtraction <= 0) {
            throw new IllegalArgumentException("Negative or zero argument");
        }

        this.subtraction = subtraction;
    }
}
