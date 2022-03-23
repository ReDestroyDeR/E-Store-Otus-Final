package ru.red.productservice.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.red.avro.CommentKey;
import ru.red.avro.CommentValue;
import ru.red.productservice.domain.Comment;

@Component("comment-producer")
public class CommentProducer {
    private final KafkaTemplate<CommentKey, CommentValue> kafka;
    private final String commentsTopicName;

    @Autowired
    public CommentProducer(@Qualifier("comment-kafka-template") KafkaTemplate<CommentKey, CommentValue> kafka,
                           @Value("#(@topicConfig.COMMENTS_TOPIC_NAME)") String commentsTopicName) {
        this.kafka = kafka;
        this.commentsTopicName = commentsTopicName;
    }

    public Mono<SendResult<CommentKey, CommentValue>> putComment(Comment comment) {
        return Mono.fromFuture(
                kafka.send(commentsTopicName,
                        new CommentKey(comment.getProductName(), comment.getAuthorEmail()),
                        new CommentValue(comment.getContent(), comment.getRecommend())
                ).completable()
        );
    }

    public Mono<SendResult<CommentKey, CommentValue>> tombstoneComment(CommentKey key) {
        return Mono.fromFuture(kafka.send(commentsTopicName, key, null).completable());
    }
}
