package ru.red.productservice.service;

import org.springframework.kafka.support.SendResult;
import reactor.core.publisher.Mono;
import ru.red.product.avro.ProductAdded;
import ru.red.product.avro.ProductSubtracted;
import ru.red.productservice.dto.ProductAdditionDTO;
import ru.red.productservice.dto.ProductSubtractionDTO;

public interface ProductService {
    Mono<SendResult<String, ProductAdded>> updateProduct(ProductAdditionDTO productAddition);

    Mono<SendResult<String, ProductSubtracted>> updateProduct(ProductSubtractionDTO productSubtraction);
}
