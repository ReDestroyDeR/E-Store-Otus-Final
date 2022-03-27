package ru.red.productservice.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.red.product.avro.ProductAdded;
import ru.red.product.avro.ProductSubtracted;
import ru.red.productservice.dto.ProductAdditionDTO;
import ru.red.productservice.dto.ProductSubtractionDTO;
import ru.red.productservice.producer.ProductProducer;
import ru.red.productservice.service.exception.NotEnoughInStockException;

@Log4j2
@Service
public class ProductServiceImpl implements ProductService {
    private final ProductProducer producer;
    private final ProductsStateService stateService;

    @Autowired
    public ProductServiceImpl(ProductProducer producer, ProductsStateService stateService) {
        this.producer = producer;
        this.stateService = stateService;
    }

    @Override
    public Mono<SendResult<String, ProductAdded>> updateProduct(ProductAdditionDTO productAddition) {
        return producer.sendMessage(productAddition)
                .doOnNext(log::info)
                .doOnError(log::warn);
    }

    @Override
    public Mono<SendResult<String, ProductSubtracted>> updateProduct(ProductSubtractionDTO productSubtraction) {
        return validation(productSubtraction.getName(), productSubtraction.getSubtraction())
                .flatMap(ignore -> producer.sendMessage(productSubtraction))
                .doOnNext(log::info)
                .doOnError(log::warn);
    }

    private Mono<Object> validation(String name, int subtraction) {
        var quantity = stateService.getByNameWithoutComments(name).getQuantity();
        if (quantity < subtraction) {
            return Mono.error(new NotEnoughInStockException("%s quantity %s asking for %s"
                    .formatted(name, quantity, subtraction)));
        }
        return Mono.just(new Object());
    }
}
