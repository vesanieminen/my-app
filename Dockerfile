FROM eclipse-temurin:21-jre
ENV FINNHUB_API_KEY=
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
