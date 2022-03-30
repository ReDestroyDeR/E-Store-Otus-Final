package ru.red.notificationservice.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.red.notificationservice.domain.Notification;
import ru.red.notificationservice.dto.NotificationDTO;

public interface NotificationService {
    Mono<Notification> createNotification(NotificationDTO dto);

    Flux<Notification> fetchNotificationsByUserId(Long userId);

    Flux<Notification> fetchNotificationsContents(String contents);

    Mono<Notification> fetchNotificationById(String id);

}
