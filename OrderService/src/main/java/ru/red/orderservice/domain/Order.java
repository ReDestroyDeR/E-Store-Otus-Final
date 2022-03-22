package ru.red.orderservice.domain;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
@Document(collection = "order-service")
public class Order {
    @MongoId(FieldType.OBJECT_ID)
    private String id;

    private String userAddress;
    // private Map<Item, Integer> contents;
    private Integer totalPrice;
}
