FROM eclipse-temurin:17-jre

RUN mkdir /app
WORKDIR /app

ADD target/portfolio-service-1.0.0-SNAPSHOT.jar /app/portfolio-service.jar

EXPOSE 8080

CMD ["java", "-jar", "portfolio-service.jar"] 