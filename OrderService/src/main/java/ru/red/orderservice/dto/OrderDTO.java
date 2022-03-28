package ru.red.orderservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderDTO {
    private String userAddress;
    private List<ProductInfoDTO> items;

    public Integer getTotalPrice() {
        if (items == null || items.isEmpty()) {
            return 0;
        }

        return items.stream().mapToInt(ProductInfoDTO::getTotalPrice).sum();
    }
}
