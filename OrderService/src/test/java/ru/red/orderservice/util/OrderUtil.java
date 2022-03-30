package ru.red.orderservice.util;

import ru.red.orderservice.domain.Order;
import ru.red.orderservice.dto.OrderDTO;
import ru.red.orderservice.dto.ProductInfoDTO;
import ru.red.orderservice.mapper.OrderMapper;

import java.util.Random;
import java.util.stream.Stream;

import static ru.red.orderservice.util.StringUtil.generateRandom;

public class OrderUtil {
    private static final OrderMapper ORDER_MAPPER = new OrderMapper();

    public static Order createRandom() {
        var orderDTO = new OrderDTO();
        var random = new Random();
        orderDTO.setUserId(random.nextLong(1, 1000));
        orderDTO.setItems(Stream.generate(() -> new ProductInfoDTO(generateRandom(random.nextInt(3, 10)),
                                random.nextInt(1, 10),
                                random.nextInt(100, 100_000)
                        ))
                        .limit(random.nextInt(1, 10))
                        .toList()
        );
        return ORDER_MAPPER.orderDTOToOrder(orderDTO);
    }

    public static Order createRandomWithAddress(Long userId) {
        var order = createRandom();
        order.setUserId(userId);
        return order;
    }
}
