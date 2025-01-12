FROM eclipse-temurin:17-jre

RUN mkdir /app
WORKDIR /app

ADD target/transactions-service-1.0.0-SNAPSHOT.jar /app/transactions-service.jar

EXPOSE 8081

CMD ["java", "-jar", "transactions-service.jar"] 