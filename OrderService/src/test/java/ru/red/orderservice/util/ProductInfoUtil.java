package ru.red.orderservice.util;

import ru.red.order.avro.ProductInfo;

import java.util.Random;

public class ProductInfoUtil {
    public static ProductInfo generateRandom() {
        var random = new Random();
        return new ProductInfo();
    }
}
