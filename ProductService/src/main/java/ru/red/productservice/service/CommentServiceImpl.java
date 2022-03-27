package ru.red.productservice.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.red.product.avro.CommentKey;
import ru.red.product.avro.CommentValue;
import ru.red.productservice.domain.Comment;
import ru.red.productservice.producer.CommentProducer;

@Log4j2
@Service
public class CommentServiceImpl implements CommentService {
    private final CommentProducer producer;

    @Autowired
    public CommentServiceImpl(CommentProducer producer) {
        this.producer = producer;
    }

    @Override
    public Mono<SendResult<CommentKey, CommentValue>> putComment(Comment comment) {
        return producer.putComment(comment)
                .doOnNext(log::info)
                .doOnError(log::warn);
    }

    @Override
    public Mono<SendResult<CommentKey, CommentValue>> deleteComment(CommentKey key) {
        return producer.tombstoneComment(key)
                .doOnNext(log::info)
                .doOnError(log::warn);
    }
}
