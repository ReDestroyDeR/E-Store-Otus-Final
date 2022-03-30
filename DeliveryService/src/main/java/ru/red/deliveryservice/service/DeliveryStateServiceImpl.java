package ru.red.deliveryservice.service;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;
import ru.red.billing.avro.OrderAcknowledgmentKey;
import ru.red.delivery.avro.DeliveryAggregate;

import java.util.HashMap;
import java.util.Map;

@Service
@Profile("kafka-streams")
public class DeliveryStateServiceImpl implements DeliveryStateService {
    private final StreamsBuilderFactoryBean factoryBean;
    private final String stateStoreName;

    private KafkaStreams.State currentState;
    private ReadOnlyKeyValueStore<OrderAcknowledgmentKey, DeliveryAggregate> store;

    public DeliveryStateServiceImpl(StreamsBuilderFactoryBean factoryBean,
                                    @Value("#{@orderProcessor.getDeliveryAggregateStoreName()}") String name) {
        this.factoryBean = factoryBean;
        this.stateStoreName = name;
        factoryBean.setStateListener(this::whenKafkaStreamsIsRunningListener);
    }

    private void whenKafkaStreamsIsRunningListener(KafkaStreams.State newState, KafkaStreams.State oldState) {
        currentState = newState;
        if (!newState.equals(KafkaStreams.State.RUNNING)) {
            store = null;
            return;
        }

        var kafkaStreams = factoryBean.getKafkaStreams();
        assert kafkaStreams != null;
        this.store = kafkaStreams.store(
                StoreQueryParameters.fromNameAndType(stateStoreName, QueryableStoreTypes.keyValueStore())
        );
    }

    @Override
    public DeliveryAggregate getState(String orderId, Long userId) {
        return store.get(new OrderAcknowledgmentKey(orderId, userId));
    }

    @Override
    public Map<CharSequence, DeliveryAggregate> getStates(Long userId) {
        if (currentState != KafkaStreams.State.RUNNING)
            throw new IllegalStateException("Current state %s, not RUNNING".formatted(currentState));

        try (var iterator = store.all()) {
            var aggregates = new HashMap<CharSequence, DeliveryAggregate>();
            while (iterator.hasNext()) {
                var next = iterator.next();
                if (next.key.getUserId() == userId) {
                    aggregates.put(next.key.getOrderId(), next.value);
                }
            }
            return aggregates;
        }
    }
}
