package ru.red.notificationservice.mapper;

import org.springframework.stereotype.Component;
import ru.red.notificationservice.domain.Notification;
import ru.red.notificationservice.dto.NotificationDTO;

@Component
public class NotificationMapper {
    public NotificationDTO notificationToNotificationDTO(Notification notification) {
        var dto = new NotificationDTO();
        dto.setTimestamp(notification.getTimestamp());
        dto.setUserAddress(notification.getUserAddress());
        dto.setContents(notification.getContents());
        return dto;
    }

    public Notification notificationDTOToNotification(NotificationDTO dto) {
        var notification = new Notification();
        notification.setTimestamp(dto.getTimestamp());
        notification.setUserAddress(dto.getUserAddress());
        notification.setContents(dto.getContents());
        return notification;
    }
}
