FROM openjdk:23-jdk-slim

WORKDIR /app

COPY target/project-0.0.1-SNAPSHOT.jar project.jar

EXPOSE 8080

CMD ["java", "-jar", "project.jar"]
