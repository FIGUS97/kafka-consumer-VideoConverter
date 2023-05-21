FROM amazoncorretto:20-alpine
LABEL authors="Mateusz Kolaczyk"

ENV JAVA_OPTS "-Dspring.profiles.active=DEV -Xms512m -Xmx1g"

WORKDIR producer

COPY ./target/consumer-0.0.1-SNAPSHOT.jar ./

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "consumer-0.0.1-SNAPSHOT.jar"]