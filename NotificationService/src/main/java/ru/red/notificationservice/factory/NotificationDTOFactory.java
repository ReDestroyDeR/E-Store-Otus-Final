package ru.red.notificationservice.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import ru.red.notificationservice.dto.NotificationDTO;
import streamprocessing.avro.OrderAckStatus;
import streamprocessing.avro.ValueOrderAcknowledgment;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Instant;

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

    public NotificationDTO createDto(String address, ValueOrderAcknowledgment value) {
        var dto = new NotificationDTO();
        dto.setUserAddress(address);
        var username = address.split("@")[0];
        dto.setContents(value.getEvent() == OrderAckStatus.ACK
                        ? ackHtml.formatted(
                        address,
                        value.getOrderId(),
                        value.getOrderTotalPrice(),
                        value.getUserBalance()
                )
                        : nackHtml.formatted(
                        address,
                        value.getOrderId(),
                        value.getOrderTotalPrice(),
                        value.getUserBalance(),
                        Math.abs(value.getUserBalance() - value.getOrderTotalPrice())
                )
        );
        dto.setTimestamp(Date.from(Instant.now()));
        return dto;
    }
}
