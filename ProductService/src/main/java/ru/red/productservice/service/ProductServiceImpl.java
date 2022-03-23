package ru.red.productservice.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.red.avro.ProductOpsValue;
import ru.red.productservice.dto.ProductAdditionDTO;
import ru.red.productservice.dto.ProductSubtractionDTO;
import ru.red.productservice.producer.ProductProducer;

@Log4j2
@Service
public class ProductServiceImpl implements ProductService {
    private final ProductProducer producer;

    @Autowired
    public ProductServiceImpl(ProductProducer producer) {
        this.producer = producer;
    }

    @Override
    public Mono<SendResult<String, ProductOpsValue>> updateProduct(ProductAdditionDTO productAddition) {
        return producer.sendMessage(productAddition)
                .doOnNext(log::info)
                .doOnError(log::warn);
    }

    @Override
    public Mono<SendResult<String, ProductOpsValue>> updateProduct(ProductSubtractionDTO productSubtraction) {
        return producer.sendMessage(productSubtraction)
                .doOnNext(log::info)
                .doOnError(log::warn);
    }
}
