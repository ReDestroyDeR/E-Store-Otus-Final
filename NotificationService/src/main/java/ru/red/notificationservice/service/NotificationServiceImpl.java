package ru.red.notificationservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.red.notificationservice.domain.Notification;
import ru.red.notificationservice.dto.NotificationDTO;
import ru.red.notificationservice.mapper.NotificationMapper;
import ru.red.notificationservice.repository.NotificationRepository;

@Service
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository repository;
    private final NotificationMapper mapper;

    @Autowired
    public NotificationServiceImpl(NotificationRepository repository,
                                   NotificationMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<Notification> createNotification(NotificationDTO dto) {
        return repository
                // JavaMailSender logic
                .save(mapper.notificationDTOToNotification(dto))
                .switchIfEmpty(Mono.error(new UnknownError("Failed creating notification")));
    }

    @Override
    public Flux<Notification> fetchNotificationsByUserId(Long userId) {
        return repository.findAllByUserId(userId);
    }

    @Override
    public Flux<Notification> fetchNotificationsContents(String contents) {
        return repository.findAllByContentsContaining(contents);
    }

    @Override
    public Mono<Notification> fetchNotificationById(String id) {
        return repository.findById(id);
    }
}
