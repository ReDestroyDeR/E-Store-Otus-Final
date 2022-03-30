package ru.red.deliveryservice.dto;

import lombok.Data;
import ru.red.delivery.avro.DeliveryStatus;

import java.util.List;

@Data
public class DeliveryDTO {
    private String orderId;
    private Long userId;
    private AddressDTO address;
    private List<ProductInfoDTO> products;
    private DeliveryStatus status;
}
