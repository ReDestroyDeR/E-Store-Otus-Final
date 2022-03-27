package ru.red.productservice.service;

import org.springframework.kafka.support.SendResult;
import reactor.core.publisher.Mono;
import ru.red.product.avro.CommentKey;
import ru.red.product.avro.CommentValue;
import ru.red.productservice.domain.Comment;

public interface CommentService {
    Mono<SendResult<CommentKey, CommentValue>> putComment(Comment comment);

    Mono<SendResult<CommentKey, CommentValue>> deleteComment(CommentKey key);
}
