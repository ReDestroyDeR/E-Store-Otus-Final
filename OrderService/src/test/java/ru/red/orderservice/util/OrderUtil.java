package ru.red.orderservice.util;

import ru.red.orderservice.domain.Order;

import java.util.Random;

public class OrderUtil {
    public static Order createRandom() {
        var order = new Order();
        var random = new Random();
        order.setUserAddress(EmailUtil.generateRandomEmail());
        order.setTotalPrice(random.nextInt(0, (int) Math.pow(10, 5)));
        return order;
    }

    public static Order createRandomWithAddress(String address) {
        var order = createRandom();
        order.setUserAddress(address);
        return order;
    }
}
