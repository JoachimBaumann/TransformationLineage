# Use a lightweight JDK image to run the app
FROM eclipse-temurin:17-jdk-jammy

# Set workdir
WORKDIR /app

# Copy your prebuilt JAR from local filesystem into image
COPY target/*.jar app.jar

# Run it
ENTRYPOINT ["java", "-jar", "app.jar"]
