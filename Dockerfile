# Use Java 17 JRE
FROM eclipse-temurin:17-jre

# Copy the executable JAR
COPY target/type_it_backend-1.0-SNAPSHOT.jar /app/app.jar

# Set working directory
WORKDIR /app

# Expose the port (Render will set PORT env)
EXPOSE 8080

# Run the JAR
CMD ["java", "-jar", "app.jar"]
