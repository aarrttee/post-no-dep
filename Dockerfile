FROM openjdk:22
WORKDIR /app
COPY target/post-no-dep-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
