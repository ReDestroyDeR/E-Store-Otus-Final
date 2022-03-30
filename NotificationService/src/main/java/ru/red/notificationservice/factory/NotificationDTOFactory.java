package ru.red.notificationservice.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import ru.red.billing.avro.OrderAcknowledged;
import ru.red.billing.avro.OrderAcknowledgmentKey;
import ru.red.billing.avro.OrderNotAcknowledged;
import ru.red.notificationservice.dto.NotificationDTO;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static java.sql.Date.from;

@Component
public class NotificationDTOFactory {
    private final String ackHtml;
    private final String nackHtml;

    @Autowired
    public NotificationDTOFactory(ResourceLoader resourceLoader) throws IOException {
        ackHtml = loadHtmlFromResource(resourceLoader.getResource("classpath:templates/email/ack.html"));
        nackHtml = loadHtmlFromResource(resourceLoader.getResource("classpath:templates/email/nack.html"));
    }

    private String loadHtmlFromResource(Resource resource) throws IOException {
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8)
                .replaceAll("(\n *)|( *\n)", "\n");
    }

    public NotificationDTO createDto(OrderAcknowledgmentKey key, OrderAcknowledged value) {
        var dto = new NotificationDTO();
        dto.setUserId(key.getUserId());
        dto.setContents(ackHtml.formatted(
                        key.getUserId(),
                        key.getOrderId(),
                        value.getTotalPrice(),
                        value.getUserBalance(),
                        value.getItems().toString()
                )
        );
        dto.setTimestamp(from(Instant.now()));
        return dto;
    }

    public NotificationDTO createDto(OrderAcknowledgmentKey key, OrderNotAcknowledged value) {
        var dto = new NotificationDTO();
        dto.setUserId(key.getUserId());
        dto.setContents(nackHtml.formatted(
                        key.getUserId(),
                        key.getOrderId(),
                        value.getTotalPrice(),
                        value.getUserBalance(),
                        value.getTotalPrice() - value.getUserBalance(),
                        value.getItems().toString()
                )
        );
        dto.setTimestamp(from(Instant.now()));
        return dto;
    }
}
