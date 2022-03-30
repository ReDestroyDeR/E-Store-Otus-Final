package ru.red.deliveryservice.service;

import reactor.core.publisher.Mono;
import ru.red.deliveryservice.dto.DeliveryDTO;

public interface DeliveryService {
    Mono<DeliveryDTO> acceptDelivery(String orderId, Long userId);

    Mono<DeliveryDTO> finishDelivery(String orderId, Long userId);
}
