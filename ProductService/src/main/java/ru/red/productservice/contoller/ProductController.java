package ru.red.productservice.contoller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.red.productservice.contoller.exception.BadRequestException;
import ru.red.productservice.domain.Product;
import ru.red.productservice.dto.ProductAdditionDTO;
import ru.red.productservice.dto.ProductSubtractionDTO;
import ru.red.productservice.service.ProductService;
import ru.red.productservice.service.ProductsStateService;

@RestController
@RequestMapping("/api/product")
public class ProductController {
    private final ProductService service;
    private final ProductsStateService stateService;

    @Autowired
    public ProductController(ProductService service, ProductsStateService stateService) {
        this.service = service;
        this.stateService = stateService;
    }

    @PatchMapping("/add")
    public Mono<String> addProduct(@RequestBody ProductAdditionDTO dto) {
        return service.updateProduct(dto).map(Object::toString).onErrorMap(IllegalArgumentException.class, BadRequestException::new);
    }

    @PatchMapping("/sub")
    public Mono<String> subProduct(@RequestBody ProductSubtractionDTO dto) {
        return service.updateProduct(dto).map(Object::toString).onErrorMap(IllegalArgumentException.class, BadRequestException::new);
    }

    @GetMapping
    public Mono<Product> fetchByName(@RequestParam("name") String name) {
        return Mono.justOrEmpty(stateService.getByNameWithoutComments(name));
    }
}
