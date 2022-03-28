package ru.red.orderservice.domain;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;
import ru.red.order.avro.ProductInfo;

import java.util.HashMap;
import java.util.Map;

@Data
@Document(collection = "order-service")
public class Order {
    @MongoId(FieldType.OBJECT_ID)
    private String id;

    private String userAddress;
    private Map<CharSequence, ProductInfo> items;
    private Integer totalPrice;

    private void setTotalPrice(Integer totalPrice) {
        this.totalPrice = totalPrice;
    }

    public int size() {
        return getItems().size();
    }

    public ProductInfo get(CharSequence key) {
        return getItems().get(key);
    }

    public ProductInfo put(CharSequence key, ProductInfo value) {
        setTotalPrice(getTotalPrice() + value.getTotalPrice());
        return getItems().put(key, value);
    }

    public ProductInfo remove(CharSequence key) {
        setTotalPrice(getTotalPrice() - get(key).getTotalPrice());
        return getItems().remove(key);
    }

    public boolean containsKey(CharSequence key) {
        return getItems().containsKey(key);
    }

    // Items getter and setter are settings and returning copies of maps, so changes can't be made out of context
    public Map<CharSequence, ProductInfo> getItems() {
        return new HashMap<>(items);
    }

    public void setItems(Map<CharSequence, ProductInfo> items) {
        setTotalPrice(items.values().stream().mapToInt(ProductInfo::getTotalPrice).sum());
        this.items = new HashMap<>(items);
    }
}
