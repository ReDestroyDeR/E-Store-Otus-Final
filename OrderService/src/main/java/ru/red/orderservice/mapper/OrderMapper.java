package ru.red.orderservice.mapper;

import org.springframework.stereotype.Component;
import ru.red.order.avro.ProductInfo;
import ru.red.orderservice.domain.Order;
import ru.red.orderservice.dto.OrderDTO;
import ru.red.orderservice.dto.ProductInfoDTO;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OrderMapper {
    public OrderDTO orderToOrderDTO(Order order) {
        var dto = new OrderDTO();
        dto.setUserId(order.getUserId());
        dto.setItems(order.getItems()
                .entrySet()
                .stream()
                .map(entry -> new ProductInfoDTO(entry.getKey().toString(),
                        entry.getValue().getUnits(),
                        entry.getValue().getPricePerUnit()))
                .toList());
        return dto;
    }

    public Order orderDTOToOrder(OrderDTO dto) {
        var order = new Order();
        order.setUserId(dto.getUserId());
        order.setItems(new HashMap<>());
        if (dto.getItems() != null) {
            order.setItems(dto.getItems().stream().map(productInfoDTO ->
                            Map.<CharSequence, ProductInfo>entry(productInfoDTO.getName(),
                                    new ProductInfo(productInfoDTO.getQuantity(),
                                            productInfoDTO.getPricePerUnit(),
                                            productInfoDTO.getTotalPrice())))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        }
        return order;
    }
}
