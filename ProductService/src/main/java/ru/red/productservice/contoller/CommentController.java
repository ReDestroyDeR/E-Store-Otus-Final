package ru.red.productservice.contoller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.SendResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.red.avro.CommentKey;
import ru.red.avro.CommentValue;
import ru.red.productservice.domain.Comment;
import ru.red.productservice.dto.CommentKeyDTO;
import ru.red.productservice.service.CommentService;

@RestController
@RequestMapping("/api/product/comments")
public class CommentController {
    private final CommentService service;

    @Autowired
    public CommentController(CommentService service) {
        this.service = service;
    }

    @PutMapping
    public Mono<SendResult<CommentKey, CommentValue>> putProduct(@RequestBody Comment comment) {
        return service.putComment(comment);
    }

    @DeleteMapping
    public Mono<SendResult<CommentKey, CommentValue>> subProduct(@RequestBody CommentKeyDTO dto) {
        return service.deleteComment(new CommentKey(dto.getProductName(), dto.getAuthorEmail()));
    }
}
