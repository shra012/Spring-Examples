# Spring for Apache Kafka.
The repository aims to implement basic spring for apache kafka project.

## Important Kafka Commands
1. To bring up kafka with docker compose.
    ```shell
    docker compose up
    ```
2. To create a topic 
    ```shell
    docker exec -it broker kafka-topics --create --bootstrap-server localhost:9092 --partitions 1 --topic topic-name
    ```
3. To run kafka console consumer
    ```shell
    docker exec -it broker kafka-console-consumer --bootstrap-server localhost:9092 --topic topic-name
    ```