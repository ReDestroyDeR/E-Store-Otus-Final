package ru.red.productservice.service;

import org.springframework.kafka.support.SendResult;
import reactor.core.publisher.Mono;
import ru.red.avro.ProductOpsValue;
import ru.red.productservice.dto.ProductAdditionDTO;
import ru.red.productservice.dto.ProductSubtractionDTO;

public interface ProductService {
    Mono<SendResult<String, ProductOpsValue>> updateProduct(ProductAdditionDTO productAddition);

    Mono<SendResult<String, ProductOpsValue>> updateProduct(ProductSubtractionDTO productSubtraction);
}
