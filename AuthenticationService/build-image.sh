#!/bin/bash
mvn generate-sources jooq-codegen:generate package
mkdir -p target/dependency
cd target/dependency
jar -xf ../*.jar
cd ../../
docker build --tag redestroyder/authentication-service:0.2.0 .
