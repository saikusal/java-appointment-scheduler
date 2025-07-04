# --- Final, lean image ---
# Use a minimal Java 17 runtime image
FROM eclipse-temurin:17-jre-focal

# Set the working directory
WORKDIR /app

# Copy the pre-built executable JAR file from the local target directory
COPY target/booking-service-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the application runs on
EXPOSE 8083

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
