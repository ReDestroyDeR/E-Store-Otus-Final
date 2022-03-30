package ru.red.deliveryservice.service;

import ru.red.delivery.avro.DeliveryAggregate;

import java.util.Map;

public interface DeliveryStateService {
    DeliveryAggregate getState(String orderId, Long userId);

    Map<CharSequence, DeliveryAggregate> getStates(Long userId);
}
