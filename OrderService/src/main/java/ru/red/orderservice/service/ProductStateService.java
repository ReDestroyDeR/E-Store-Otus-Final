package ru.red.orderservice.service;

import ru.red.orderservice.dto.ProductInfoDTO;

public interface ProductStateService {
    ProductInfoDTO getByName(String name);
}
