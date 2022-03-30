package ru.red.orderservice.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import ru.red.orderservice.containers.ConfluentKafkaContainer;
import ru.red.orderservice.containers.MongoContainer;
import ru.red.orderservice.domain.Order;
import ru.red.orderservice.dto.OrderDTO;
import ru.red.orderservice.mapper.OrderMapper;
import ru.red.orderservice.util.OrderUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers
@SpringBootTest
@ActiveProfiles({"test", "kafka"})
class OrderServiceImplTest {
    @Container
    public final static ConfluentKafkaContainer kafkaContainer = ConfluentKafkaContainer.getInstance();

    @Container
    public final static MongoDBContainer mongoDBContainer = MongoContainer.getInstance();

    @Autowired
    OrderService service;

    @Autowired
    OrderMapper mapper;

    @DynamicPropertySource
    static void mongoDbProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.host", mongoDBContainer::getHost);
        registry.add("spring.data.mongodb.port", () -> mongoDBContainer.getMappedPort(27017));
        registry.add("kafka.bootstrapAddress", kafkaContainer::getBootstrapServers);
    }

    @Test
    void createOrder() {
        var order = OrderUtil.createRandom();
        var dto = mapper.orderToOrderDTO(order);
        StepVerifier.create(service.createOrder(dto))
                .expectNextMatches(o -> o.getUserId().equals(order.getUserId()) &&
                        o.getTotalPrice().equals(order.getTotalPrice()))
                .expectComplete()
                .verify();
    }

    private Flux<Order> createOrders(int count) {
        return Flux.generate(sink -> sink.next(mapper.orderToOrderDTO(OrderUtil.createRandom())))
                .take(count)
                .cast(OrderDTO.class)
                .flatMap(service::createOrder);
    }

    private Flux<Order> createOrders(int count, Long userId) {
        return Flux.generate(sink -> sink.next(
                                mapper.orderToOrderDTO(
                                        OrderUtil.createRandomWithAddress(userId)
                                )
                        )
                )
                .take(count)
                .cast(OrderDTO.class)
                .flatMap(service::createOrder);
    }

    @Test
    void fetchNotificationsByAddress() {
        var count = 10;
        var userId = 732L;
        StepVerifier.create(createOrders(count))
                .expectNextCount(count)
                .expectComplete()
                .verify();

        var orders = createOrders(count / 2, userId).cache(count);
        StepVerifier.create(orders)
                .expectNextCount(count / 2)
                .expectComplete()
                .verify();

        StepVerifier.create(service.fetchOrdersByUserId(userId))
                .expectNextMatches(n -> Boolean.TRUE.equals(
                        orders.any(notification -> notification.equals(n)).block()))
                .expectNextCount(count / 2 - 1)
                .expectComplete()
                .verify();
    }

    @Test
    void fetchOrderTotalPrice() {
        var count = 10;
        StepVerifier.create(createOrders(count))
                .expectNextCount(count)
                .expectComplete()
                .verify();

        var order = OrderUtil.createRandom();
        order = service.createOrder(mapper.orderToOrderDTO(order)).block();

        assert order != null;

        var finalOrder = order;
        StepVerifier.create(service.fetchOrdersByTotalPriceBetween(order.getTotalPrice(), order.getTotalPrice()))
                .expectNextMatches(n -> n.equals(finalOrder))
                .expectComplete()
                .verify();
    }

    @Test
    void fetchNotificationById() {
        var order = OrderUtil.createRandom();
        order = service.createOrder(mapper.orderToOrderDTO(order)).block();
        assertNotNull(order);
        var found = service.fetchOrderById(order.getId()).block();
        assertNotNull(found);
        assertEquals(found, order);
    }
}