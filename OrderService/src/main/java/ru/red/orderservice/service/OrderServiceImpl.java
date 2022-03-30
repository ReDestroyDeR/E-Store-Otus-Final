package ru.red.orderservice.service;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.red.orderservice.domain.Order;
import ru.red.orderservice.dto.OrderDTO;
import ru.red.orderservice.mapper.OrderMapper;
import ru.red.orderservice.producer.OrderProducer;
import ru.red.orderservice.repository.OrderRepository;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository repository;
    private final ProductStateService productStateService;
    private final OrderMapper mapper;
    private final OrderProducer producer;

    @Autowired
    public OrderServiceImpl(OrderRepository repository,
                            ProductStateService productStateService,
                            OrderMapper mapper,
                            OrderProducer producer) {
        this.repository = repository;
        this.productStateService = productStateService;
        this.mapper = mapper;
        this.producer = producer;
    }

    @Override
    public Mono<Order> createOrder(OrderDTO dto) {
        if (dto.getItems() == null || dto.getItems().isEmpty())
            return Mono.error(new IllegalArgumentException("No items provided"));

        try {
            dto.setItems(dto.getItems()
                    .stream()
                    .map(productInfoDTO -> {
                        var productInfo = productStateService.getByName(productInfoDTO.getName());
                        if (productInfo.getQuantity() < productInfoDTO.getQuantity())
                            throw new IllegalArgumentException("Not enough in stock");

                        productInfo.setQuantity(productInfoDTO.getQuantity());
                        return productInfo;
                    })
                    .toList());
        } catch (Throwable e) {
            return Mono.error(e);
        }


        var order = mapper.orderDTOToOrder(dto);
        order.setId(ObjectId.get().toHexString());
        return producer.sendCreatedMessage(Flux.just(order))
                .then(repository.save(order))
                .retry(3);
    }

    @Override
    public Flux<Order> fetchOrdersByUserId(Long userId) {
        return repository.findAllByUserId(userId);
    }

    @Override
    public Flux<Order> fetchOrdersByTotalPriceBetween(Integer totalPriceStart, Integer totalPriceEnd) {
        // Expand prices to include previous and start arguments
        totalPriceStart -= 1;
        totalPriceEnd += 1;
        return repository.findAllByTotalPriceBetween(totalPriceStart, totalPriceEnd);
    }

    @Override
    public Mono<Order> fetchOrderById(String id) {
        return repository.findById(id);
    }
}
