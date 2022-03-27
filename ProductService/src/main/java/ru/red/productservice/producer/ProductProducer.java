package ru.red.productservice.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.red.product.avro.ProductAdded;
import ru.red.product.avro.ProductSubtracted;
import ru.red.productservice.dto.ProductAdditionDTO;
import ru.red.productservice.dto.ProductSubtractionDTO;

@Component("product-producer")
public class ProductProducer {
    private final KafkaTemplate<String, ProductAdded> addedTemplate;
    private final KafkaTemplate<String, ProductSubtracted> subtractedTemplate;
    private final String productOpsTopicName;

    @Autowired
    public ProductProducer(KafkaTemplate<String, ProductAdded> addedTemplate,
                           KafkaTemplate<String, ProductSubtracted> subtractedTemplate,
                           @Value("#{@newTopicConfig.PRODUCT_OPS_TOPIC_NAME}") String productOpsTopicName) {
        this.addedTemplate = addedTemplate;
        this.subtractedTemplate = subtractedTemplate;
        this.productOpsTopicName = productOpsTopicName;
    }

    public Mono<SendResult<String, ProductAdded>> sendMessage(ProductAdditionDTO addition) {
        return Mono.fromFuture(
                addedTemplate.send(productOpsTopicName, addition.getName(),
                        new ProductAdded(addition.getPricePerUnit(), addition.getAddition())).completable()
        );
    }

    public Mono<SendResult<String, ProductSubtracted>> sendMessage(ProductSubtractionDTO subtraction) {
        return Mono.fromFuture(
                subtractedTemplate.send(productOpsTopicName, subtraction.getName(),
                        new ProductSubtracted(subtraction.getSubtraction())).completable()
        );
    }
}
