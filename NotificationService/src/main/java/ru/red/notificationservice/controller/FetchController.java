package ru.red.notificationservice.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.red.notificationservice.controller.exceptions.NotFoundException;
import ru.red.notificationservice.domain.Notification;
import ru.red.notificationservice.service.NotificationService;

@Log4j2
@RestController
@RequestMapping("/api/notifications")
public class FetchController {
    private final NotificationService service;

    @Autowired
    public FetchController(NotificationService service) {
        this.service = service;
    }

    @GetMapping("/id/{id}")
    public Mono<Notification> fetchNotificationById(@PathVariable("id") String id) {
        return service.fetchNotificationById(id).as(this::validation);
    }

    @GetMapping("/email/{email}")
    public Flux<Notification> fetchNotificationsByAddress(@PathVariable("email") String address) {
        return service.fetchNotificationsByAddress(address).as(this::validation);
    }

    @GetMapping("/contents/{contents}")
    public Flux<Notification> fetchNotificationsByContents(@PathVariable("contents") String contents) {
        return service.fetchNotificationsContents(contents).as(this::validation);
    }

    private <T> Mono<T> validation(Mono<T> publisher) {
        return publisher.switchIfEmpty(Mono.error(new NotFoundException()));
    }

    private <T> Flux<T> validation(Flux<T> publisher) {
        return publisher.switchIfEmpty(Flux.error(new NotFoundException()));
    }
}
