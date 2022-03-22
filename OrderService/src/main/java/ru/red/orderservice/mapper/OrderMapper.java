package ru.red.orderservice.mapper;

import org.springframework.stereotype.Component;
import ru.red.orderservice.domain.Order;
import ru.red.orderservice.dto.OrderDTO;

@Component
public class OrderMapper {
    public OrderDTO orderToOrderDTO(Order order) {
        var dto = new OrderDTO();
        dto.setUserAddress(order.getUserAddress());
        dto.setTotalPrice(order.getTotalPrice());
        return dto;
    }

    public Order orderDTOToOrder(OrderDTO dto) {
        var order = new Order();
        order.setUserAddress(dto.getUserAddress());
        order.setTotalPrice(dto.getTotalPrice());
        return order;
    }
}
