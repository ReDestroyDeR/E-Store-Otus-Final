package ru.red.deliveryservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.red.delivery.avro.DeliveryStatus;
import ru.red.deliveryservice.dto.DeliveryDTO;
import ru.red.deliveryservice.mapper.DeliveryMapper;
import ru.red.deliveryservice.producer.DeliveryProducer;

@Service
public class DeliveryServiceImpl implements DeliveryService {
    private final DeliveryStateService stateService;
    private final DeliveryProducer producer;
    private final DeliveryMapper mapper;

    @Autowired
    public DeliveryServiceImpl(DeliveryStateService stateService, DeliveryProducer producer, DeliveryMapper mapper) {
        this.stateService = stateService;
        this.producer = producer;
        this.mapper = mapper;
    }

    @Override
    public Mono<DeliveryDTO> acceptDelivery(String orderId, Long userId) {
        var aggregate = stateService.getState(orderId, userId);
        return aggregate.getStatus().equals(DeliveryStatus.SCHEDULED)
                ? producer.startDelivery(orderId, userId).map(result -> {
            var producerRecord = result.getProducerRecord();
            var dto = mapper.producerRecordExplodedToDeliveryDTO(producerRecord.key(),
                    aggregate.getAddress(), aggregate.getItems());
            dto.setStatus(DeliveryStatus.STARTED);
            return dto;
        })
                : Mono.error(new IllegalStateException("Delivery can't be started, current state " + aggregate.getStatus().name()));
    }

    @Override
    public Mono<DeliveryDTO> finishDelivery(String orderId, Long userId) {
        var aggregate = stateService.getState(orderId, userId);
        return aggregate.getStatus().equals(DeliveryStatus.STARTED)
                ? producer.finishDelivery(orderId, userId).map(result -> {
            var producerRecord = result.getProducerRecord();
            var dto = mapper.producerRecordExplodedToDeliveryDTO(producerRecord.key(),
                    aggregate.getAddress(), aggregate.getItems());
            dto.setStatus(DeliveryStatus.FINISHED);
            return dto;
        })
                : Mono.error(new IllegalStateException("Delivery can't be finished, current state " + aggregate.getStatus().name()));

    }
}
