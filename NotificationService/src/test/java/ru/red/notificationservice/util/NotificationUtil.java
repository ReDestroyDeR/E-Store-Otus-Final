package ru.red.notificationservice.util;

import ru.red.notificationservice.domain.Notification;

import java.time.Instant;
import java.util.Date;
import java.util.Random;

public class NotificationUtil {
    public static Notification createRandom() {
        var notification = new Notification();
        var random = new Random();
        notification.setTimestamp(Date.from(Instant.ofEpochSecond(random.nextInt(0, (int) Math.pow(2, 32)))));
        var length = random.nextInt(128, 1024);
        var contents = random
                .ints(length, ' ', 'z')
                .mapToObj(i -> (char) i)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
        notification.setContents(contents);
        notification.setUserId(random.nextLong(1, 1000));
        return notification;
    }

    public static Notification createRandomWithAddress(Long userId) {
        var notification = createRandom();
        notification.setUserId(userId);
        return notification;
    }
}
