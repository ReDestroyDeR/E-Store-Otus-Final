package ru.red.orderservice.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.red.orderservice.controller.exceptions.BadRequestException;
import ru.red.orderservice.controller.exceptions.NotFoundException;
import ru.red.orderservice.domain.IdempotentOrder;
import ru.red.orderservice.dto.OrderDTO;
import ru.red.orderservice.mapper.OrderMapper;
import ru.red.orderservice.service.OrderOperationService;
import ru.red.orderservice.service.OrderService;

@Log4j2
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService service;
    private final OrderOperationService idempotency;
    private final OrderMapper orderMapper;

    @Autowired
    public OrderController(OrderService service, OrderOperationService idempotency, OrderMapper orderMapper) {
        this.service = service;
        this.idempotency = idempotency;
        this.orderMapper = orderMapper;
    }

    @PostMapping
    public Mono<OrderDTO> createOrder(@RequestBody OrderDTO dto,
                                      @RequestHeader("Idempotency-Key") String idempotencyKey) {
        return idempotency.getByIdempotencyKey(idempotencyKey)
                .switchIfEmpty(service.createOrder(dto)
                        .flatMap(response -> idempotency.commit(idempotencyKey, response))
                        .map(IdempotentOrder::getResponse))
                .onErrorMap(e -> new BadRequestException(e.getMessage(), e))
                .as(this::validation)
                .map(orderMapper::orderToOrderDTO);
    }

    @GetMapping("/id/{id}")
    public Mono<OrderDTO> fetchOrderById(@PathVariable("id") String id) {
        return service.fetchOrderById(id)
                .as(this::validation)
                .map(orderMapper::orderToOrderDTO);
    }

    @GetMapping("/email/{email}")
    public Flux<OrderDTO> fetchOrdersByEmail(@PathVariable("email") String address) {
        return service.fetchOrdersByEmail(address)
                .as(this::validation)
                .map(orderMapper::orderToOrderDTO);
    }

    @GetMapping("/price")
    public Flux<OrderDTO> fetchOrdersByTotalPrice(
            @RequestParam(value = "start", required = false, defaultValue = "0") Integer totalPriceStart,
            @RequestParam(value = "end", required = false, defaultValue = "" + Integer.MAX_VALUE) Integer totalPriceEnd) {
        return service.fetchOrdersByTotalPriceBetween(totalPriceStart, totalPriceEnd)
                .as(this::validation)
                .map(orderMapper::orderToOrderDTO);
    }

    private <T> Mono<T> validation(Mono<T> publisher) {
        return publisher.switchIfEmpty(Mono.error(new NotFoundException()));
    }

    private <T> Flux<T> validation(Flux<T> publisher) {
        return publisher.switchIfEmpty(Flux.error(new NotFoundException()));
    }
}
