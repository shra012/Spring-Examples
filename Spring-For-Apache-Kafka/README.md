# Library Kafka Event Producer.
It is a sample event producer which emmits book data which can be used for library record management. 

## Kafka Commands  

### Basic kafka commands.

1. Create a topic named `test`
```shell
docker exec -it libkafka0 kafka-topics --create --bootstrap-server broker-0:9092 --partitions 3 --replication-factor 3 --topic test
```
2. Producer to produce string values to `test` topic.
```shell
docker exec -it libkafka0 kafka-console-producer --bootstrap-server broker-0:9092 --topic test
```
3. Consumer to consume from topic `test` with consumer group `test.console.1`
```shell
docker exec -it libkafka0 kafka-console-consumer --bootstrap-server broker-0:9092 --group test.console.1 --topic test
```
4. Consumer to consume from topic `test` with consumer group `test.console.1` from the earliest offset. 
```shell
docker exec -it libkafka0 kafka-console-consumer --bootstrap-server broker-0:9092 --group test.console.1 --topic test --from-beginning  
```
5. Consumer which prints Integer `key` and String `value` 
```shell
docker exec -it libkafka0 kafka-console-consumer --bootstrap-server broker-0:9092 --group test.console.1 --from-beginning  --property print.key=true --formatt
er kafka.tools.DefaultMessageFormatter --key-deserializer=org.apache.kafka.common.serialization.IntegerDeserializer --topic library.events  
```
6. Describes the topic `test`, which shows the number of partitions and the replicas (along with In-Sync-Replicas).
```shell
docker exec -it libkafka0 kafka-topics --describe --zookeeper zookeeper:2181 --topic test
```

6. Alter minimum in sync replicas of an existing topic.
```shell
docker exec -it libkafka0 kafka-topics --alter --zookeeper zookeeper:2181 --config min.insync.replicas=2 --topic test

docker exec -it libkafka0 kafka-configs --bootstrap-server broker-0:9092 --entity-type topics --entity-name library.events --alter --add-config min.insync.replicas=2
```

### Basic `kafkacat` commands. 
1. Uses port 9092 which is exposed as `KAFKA_INTER_BROKER_LISTENER_NAME`. It is used for broker communication with in the docker network.
```shell
docker compose exec kafkacat kafkacat -b broker-0:9092 -L
```
2. Uses port 19092 which is exposed to the local machine.
```shell
docker compose exec kafkacat kafkacat -b broker-0:19092 -L
```
*CAUTION: When using any kafka shell scripts inside docker do not use `localhost:19092` always use the docker hostname, as that is the internal broker listener.
example `broker-0:9092`*