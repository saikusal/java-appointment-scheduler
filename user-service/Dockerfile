# --- Stage 1: Build the application ---
# Use a Maven image with Java 17 to build the project
FROM maven:3.8.5-openjdk-17 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml file to download dependencies
COPY pom.xml .

# Download all dependencies. This is done as a separate step to leverage Docker's layer caching.
RUN mvn dependency:go-offline

# Copy the rest of the source code
COPY src ./src

# Package the application into a JAR file
RUN mvn package -DskipTests


# --- Stage 2: Create the final, lean image ---
# Use a minimal Java 17 runtime image
FROM eclipse-temurin:17-jre-focal

# Set the working directory
WORKDIR /app

# Copy the executable JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port the application runs on
EXPOSE 8081

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
