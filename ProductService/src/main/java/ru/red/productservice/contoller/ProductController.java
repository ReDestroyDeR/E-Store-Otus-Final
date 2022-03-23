package ru.red.productservice.contoller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.red.avro.ProductOpsValue;
import ru.red.productservice.contoller.exception.BadRequestException;
import ru.red.productservice.dto.ProductAdditionDTO;
import ru.red.productservice.dto.ProductSubtractionDTO;
import ru.red.productservice.service.ProductService;

@RestController
@RequestMapping("/api/product")
public class ProductController {
    private final ProductService service;

    @Autowired
    public ProductController(ProductService service) {
        this.service = service;
    }

    @PatchMapping("/add")
    public Mono<SendResult<String, ProductOpsValue>> addProduct(@RequestBody ProductAdditionDTO dto) {
        return service.updateProduct(dto).onErrorMap(IllegalArgumentException.class, BadRequestException::new);
    }

    @PatchMapping("/sub")
    public Mono<SendResult<String, ProductOpsValue>> subProduct(@RequestBody ProductSubtractionDTO dto) {
        return service.updateProduct(dto).onErrorMap(IllegalArgumentException.class, BadRequestException::new);
    }
}
