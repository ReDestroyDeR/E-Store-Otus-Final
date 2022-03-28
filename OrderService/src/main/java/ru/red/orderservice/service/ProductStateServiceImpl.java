package ru.red.orderservice.service;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;
import ru.red.orderservice.dto.ProductInfoDTO;
import ru.red.product.avro.ProductTableValue;

@Service
public class ProductStateServiceImpl implements ProductStateService {

    private final StreamsBuilderFactoryBean factoryBean;
    private final String stateStoreName;

    private ReadOnlyKeyValueStore<String, ProductTableValue> store;

    public ProductStateServiceImpl(StreamsBuilderFactoryBean factoryBean,
                                   @Value("#{@productTableProcessor.getProductStoreName()}") String name) {
        this.factoryBean = factoryBean;
        this.stateStoreName = name;
        factoryBean.setStateListener(this::whenKafkaStreamsIsRunningListener);
    }

    private void whenKafkaStreamsIsRunningListener(KafkaStreams.State newState, KafkaStreams.State oldState) {
        if (!newState.equals(KafkaStreams.State.RUNNING)) {
            store = null;
            return;
        }

        var kafkaStreams = factoryBean.getKafkaStreams();
        this.store = kafkaStreams.store(
                StoreQueryParameters.fromNameAndType(stateStoreName, QueryableStoreTypes.keyValueStore())
        );
    }

    @Override
    public ProductInfoDTO getByName(String name) {
        var tableValue = store.get(name);
        if (tableValue == null)
            throw new IllegalArgumentException("Product \"" + name + "\" can't be found");

        return new ProductInfoDTO(name, tableValue.getQuantity(), tableValue.getPrice());
    }
}
