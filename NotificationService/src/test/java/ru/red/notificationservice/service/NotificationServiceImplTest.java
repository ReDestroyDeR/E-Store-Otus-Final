package ru.red.notificationservice.service;

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
import ru.red.notificationservice.containers.MongoContainer;
import ru.red.notificationservice.domain.Notification;
import ru.red.notificationservice.dto.NotificationDTO;
import ru.red.notificationservice.mapper.NotificationMapper;
import ru.red.notificationservice.util.NotificationUtil;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class NotificationServiceImplTest {
    @Container
    public final static MongoDBContainer mongoDBContainer = MongoContainer.getInstance();
    @Autowired
    NotificationService service;
    @Autowired
    NotificationMapper mapper;

    @DynamicPropertySource
    static void mongoDbProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.host", mongoDBContainer::getHost);
        registry.add("spring.data.mongodb.port", () -> mongoDBContainer.getMappedPort(27017));
    }

    @Test
    void createNotification() {
        var notification = NotificationUtil.createRandom();
        var dto = mapper.notificationToNotificationDTO(notification);
        StepVerifier.create(service.createNotification(dto))
                .expectNextMatches(n -> n.getTimestamp().equals(notification.getTimestamp()) &&
                        n.getUserAddress().equals(notification.getUserAddress()) &&
                        n.getContents().equals(notification.getContents()))
                .expectComplete()
                .verify();
    }

    private Flux<Notification> createNotifications(int count) {
        return Flux.generate(sink -> sink.next(mapper.notificationToNotificationDTO(NotificationUtil.createRandom())))
                .take(count)
                .cast(NotificationDTO.class)
                .flatMap(service::createNotification);
    }

    private Flux<Notification> createNotifications(int count, String address) {
        return Flux.generate(sink -> sink.next(
                                mapper.notificationToNotificationDTO(
                                        NotificationUtil.createRandomWithAddress(address)
                                )
                        )
                )
                .take(count)
                .cast(NotificationDTO.class)
                .flatMap(service::createNotification);
    }

    @Test
    void fetchNotificationsByAddress() {
        var count = 10;
        var email = "test@example.org"; // Using .org since util uses .com
        StepVerifier.create(createNotifications(count))
                .expectNextCount(count)
                .expectComplete()
                .verify();

        var notifications = createNotifications(count / 2, email).cache(count);
        StepVerifier.create(notifications)
                .expectNextCount(count / 2)
                .expectComplete()
                .verify();

        StepVerifier.create(service.fetchNotificationsByAddress(email))
                .expectNextMatches(n -> Boolean.TRUE.equals(
                        notifications.any(notification -> notification.equals(n)).block()))
                .expectNextCount(count / 2 - 1)
                .expectComplete()
                .verify();
    }

    @Test
    void fetchNotificationsContents() {
        var count = 10;
        StepVerifier.create(createNotifications(count))
                .expectNextCount(count)
                .expectComplete()
                .verify();

        var notification = NotificationUtil.createRandom();
        notification = service.createNotification(mapper.notificationToNotificationDTO(notification)).block();

        assert notification != null;

        var finalNotification = notification;
        StepVerifier.create(service.fetchNotificationsContents(notification.getContents()))
                .expectNextMatches(n -> n.equals(finalNotification))
                .expectComplete()
                .verify();
    }

    @Test
    void fetchNotificationById() {
        var notification = NotificationUtil.createRandom();
        notification = service.createNotification(mapper.notificationToNotificationDTO(notification)).block();
        assert notification != null;
        var found = service.fetchNotificationById(notification.getId()).block();
        assert found != null;
        assert found.equals(notification);
    }
}