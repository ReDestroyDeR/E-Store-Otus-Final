#!/bin/bash
mvn install:install-file -Dfile=./lib/reactor-kafka-1.3.9-20220110.102324-3.jar -DgroupId=io.projectreactor.kafka -DartifactId=reactor-kafka -Dversion=1.3.9-SNAPSHOT -Dpackaging=jar
