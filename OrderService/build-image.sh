#!/bin/bash
./mvnw clean package
mkdir -p target/dependency
cd target/dependency
jar -xf ../*.jar
cd ../../
docker build --tag redestroyder/order-service:0.0.2 .
