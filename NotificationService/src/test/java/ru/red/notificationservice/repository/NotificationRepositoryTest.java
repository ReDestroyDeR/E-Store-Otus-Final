package ru.red.notificationservice.repository;

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
import ru.red.notificationservice.containers.MongoContainer;
import ru.red.notificationservice.util.NotificationUtil;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class NotificationRepositoryTest {
    @Container
    public final static MongoDBContainer mongoDBContainer = MongoContainer.getInstance();
    @Autowired
    NotificationRepository repository;

    @DynamicPropertySource
    static void mongoDbProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.host", mongoDBContainer::getHost);
        registry.add("spring.data.mongodb.port", () -> mongoDBContainer.getMappedPort(27017));
    }

    @Test
    void createNotification() {
        var notification = NotificationUtil.createRandom();
        StepVerifier.create(repository.save(notification))
                .expectNextCount(1)
                .expectComplete()
                .log()
                .verify();

        assert notification.equals(repository
                .findAllByUserAddress(notification.getUserAddress())
                .blockFirst());
    }
}