package ru.red.productservice.service;

import lombok.extern.log4j.Log4j2;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;
import ru.red.product.avro.ProductTableValue;
import ru.red.productservice.domain.Product;

@Log4j2
@Service
public class ProductsStateServiceImpl implements ProductsStateService {
    private final StreamsBuilderFactoryBean factoryBean;
    private final String stateStoreName;

    private KafkaStreams.State currentState;
    private ReadOnlyKeyValueStore<String, ProductTableValue> store;

    public ProductsStateServiceImpl(StreamsBuilderFactoryBean factoryBean,
                                    @Value("#{@productProcessor.getProductStockStateStoreName()}") String name) {
        this.factoryBean = factoryBean;
        this.stateStoreName = name;
        factoryBean.setStateListener(this::whenKafkaStreamsIsRunningListener);
    }

    private void whenKafkaStreamsIsRunningListener(KafkaStreams.State newState, KafkaStreams.State oldState) {
        if (!newState.equals(KafkaStreams.State.RUNNING)) {
            this.currentState = newState;
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
    public Product getByNameWithoutComments(String name) {
        if (store == null) {
            throw new IllegalStateException("KafkaStreams is not RUNNING, current state " + currentState.name());
        }

        var value = store.get(name);
        if (value == null) {
            return null;
        }
        var product = new Product();
        product.setName(name);
        product.setQuantity(value.getQuantity());
        product.setPricePerUnit(value.getPrice());
        return product;
    }
}
