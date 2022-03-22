package ru.red.orderservice.domain;

import lombok.Data;
import lombok.NonNull;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;
import java.util.Date;

@Data
@Document(collection = "idempotency")
public class IdempotentOrder {
    @MongoId(FieldType.STRING)
    private String idempotencyKey;
    @NonNull
    private Order response;

    @Field
    @Indexed(name = "ttl", expireAfter = "#{@idempotentConfig.ttl}")
    private Date ttl = Date.from(Instant.now());
}
