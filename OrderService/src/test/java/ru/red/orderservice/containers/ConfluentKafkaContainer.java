package ru.red.orderservice.containers;

import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

public class ConfluentKafkaContainer extends KafkaContainer {
    private static ConfluentKafkaContainer instance;

    private ConfluentKafkaContainer() {
        super(DockerImageName.parse("confluentinc/cp-kafka:5.4.0"));
        this.withEmbeddedZookeeper();
    }

    public static ConfluentKafkaContainer getInstance() {
        if (instance == null) {
            instance = new ConfluentKafkaContainer();
        }

        return instance;
    }

    /**
     * Starts the container using docker, pulling an image if necessary.
     */
    @Override
    public void start() {
        super.start();
    }
}
