## JDK Image ##
FROM openjdk:8-jdk-slim AS build
WORKDIR /home/gradle/src
COPY . .
RUN ./gradlew build -x test
## JRE Image ##
FROM openjdk:8-jre-slim
LABEL maintainer "Shravankumar Nagarajan"
COPY --from=build /home/gradle/src/build/libs/*.jar /app/spring-application.jar
ENTRYPOINT exec java -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -Djava.security.egd=file:/dev/./urandom -Dserver.port=8080 -jar /app/spring-application.jar
