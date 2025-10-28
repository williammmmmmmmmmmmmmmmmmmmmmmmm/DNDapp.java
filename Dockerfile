# Stage 1: Build the Java application using a Maven image compatible with Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the entire project code into the container
COPY . .

# Run the Maven build to compile and package the app into a JAR file
RUN mvn clean install -DskipTests

# Stage 2: Create a smaller, production-ready image
FROM eclipse-temurin:21-jre-alpine

# Set the working directory in the final image
WORKDIR /usr/app

# Set the environment variable for the port (Render default)
ENV PORT 10000

# Copy the built JAR file from the 'build' stage
# CRITICAL: If your JAR name is different from DNDapp-1.0-SNAPSHOT.jar, change it here!
COPY --from=build /app/target/DNDapp-1.0-SNAPSHOT.jar ./app.jar

# Define the command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
