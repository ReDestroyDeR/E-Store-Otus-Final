package ru.red.orderservice.containers;

import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

public class MongoContainer extends MongoDBContainer {
    private static MongoContainer instance;

    private MongoContainer() {
        super(DockerImageName.parse("mongo:5.0.6"));
    }

    public static MongoContainer getInstance() {
        if (instance == null) {
            instance = new MongoContainer();
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
