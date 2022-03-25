package ru.red.productservice.service;

import ru.red.productservice.domain.Product;

public interface ProductsStateService {
    Product getByNameWithoutComments(String name);
}
