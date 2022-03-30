package ru.red.deliveryservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.red.deliveryservice.dto.DeliveryDTO;
import ru.red.deliveryservice.exception.BadRequestException;
import ru.red.deliveryservice.mapper.DeliveryMapper;
import ru.red.deliveryservice.service.DeliveryService;
import ru.red.deliveryservice.service.DeliveryStateService;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {
    private final DeliveryStateService stateService;
    private final DeliveryService service;
    private final DeliveryMapper deliveryMapper;

    @Autowired
    public DeliveryController(DeliveryStateService stateService,
                              DeliveryService service,
                              DeliveryMapper deliveryMapper) {
        this.stateService = stateService;
        this.service = service;
        this.deliveryMapper = deliveryMapper;
    }

    @PostMapping("/start")
    public Mono<DeliveryDTO> startDelivery(@RequestParam String orderId, @RequestParam Long userId) {
        return service.acceptDelivery(orderId, userId)
                .onErrorMap(IllegalStateException.class, BadRequestException::new);
    }

    @PostMapping("/end")
    public Mono<DeliveryDTO> endDelivery(@RequestParam String orderId, @RequestParam Long userId) {
        return service.finishDelivery(orderId, userId)
                .onErrorMap(IllegalStateException.class, BadRequestException::new);
    }

    @GetMapping()
    public Mono<DeliveryDTO> getStatus(@RequestParam String orderId, @RequestParam Long userId) {
        return Mono.just(stateService.getState(orderId, userId))
                .map(aggregate -> deliveryMapper.keyAndAggregateToDTO(orderId, userId, aggregate));
    }

    @GetMapping("/all")
    public Flux<DeliveryDTO> getStatus(@RequestParam Long userId) {
        return Mono.just(stateService.getStates(userId))
                .flatMapMany(aggregateMap -> Flux.fromStream(aggregateMap.entrySet()
                        .stream()
                        .map(entry -> deliveryMapper.keyAndAggregateToDTO(entry.getKey(), userId, entry.getValue()))
                ));
    }
}
