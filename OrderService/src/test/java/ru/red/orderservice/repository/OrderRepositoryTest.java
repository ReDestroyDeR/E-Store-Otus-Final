package ru.red.orderservice.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;
import ru.red.orderservice.containers.MongoContainer;
import ru.red.orderservice.util.OrderUtil;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class OrderRepositoryTest {
    @Container
    public final static MongoDBContainer mongoDBContainer = MongoContainer.getInstance();
    @Autowired
    OrderRepository repository;

    @DynamicPropertySource
    static void mongoDbProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.host", mongoDBContainer::getHost);
        registry.add("spring.data.mongodb.port", () -> mongoDBContainer.getMappedPort(27017));
    }

    @Test
    void createNotification() {
        var order = OrderUtil.createRandom();
        StepVerifier.create(repository.save(order))
                .expectNextCount(1)
                .expectComplete()
                .log()
                .verify();

        assert order.equals(repository
                .findAllByUserId(order.getUserId())
                .blockFirst());
    }
}