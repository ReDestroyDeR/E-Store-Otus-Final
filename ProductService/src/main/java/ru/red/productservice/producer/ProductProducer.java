package ru.red.productservice.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.red.avro.ProductOpsValue;
import ru.red.productservice.dto.ProductAdditionDTO;
import ru.red.productservice.dto.ProductSubtractionDTO;

@Component("product-producer")
public class ProductProducer {
    private final KafkaTemplate<String, ProductOpsValue> kafka;
    private final String productOpsTopicName;

    @Autowired
    public ProductProducer(KafkaTemplate<String, ProductOpsValue> kafka,
                           @Value("#(@topicConfig.productOpsTopicName)") String productOpsTopicName) {
        this.kafka = kafka;
        this.productOpsTopicName = productOpsTopicName;
    }

    public Mono<SendResult<String, ProductOpsValue>> sendMessage(ProductAdditionDTO addition) {
        return sendMessage(addition.getName(), addition.getAddition(), addition.getPricePerUnit());
    }

    public Mono<SendResult<String, ProductOpsValue>> sendMessage(ProductSubtractionDTO subtraction) {
        return sendMessage(subtraction.getName(), -subtraction.getSubtraction(), null);
    }

    private Mono<SendResult<String, ProductOpsValue>> sendMessage(String name, Integer stockDelta, Integer price) {
        return Mono.fromFuture(
                kafka.send(productOpsTopicName, name, new ProductOpsValue(price, stockDelta)).completable()
        );
    }
}
