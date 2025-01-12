FROM eclipse-temurin:17-jre

RUN mkdir /app
WORKDIR /app

ADD target/users-service-1.0.0-SNAPSHOT.jar /app/users-service.jar

EXPOSE 8082

CMD ["java", "-jar", "users-service.jar"] 