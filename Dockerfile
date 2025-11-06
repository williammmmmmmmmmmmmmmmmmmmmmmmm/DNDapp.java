# Stage 1: Build the application using a Maven image compatible with Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the entire project code into the container
COPY . .

# Run the Maven build (will use the shade plugin you added in pom.xml)
RUN mvn clean install -DskipTests

# Stage 2: Create a smaller, production-ready image for running the server
FROM eclipse-temurin:21-jre-alpine

# Set the working directory in the final image
WORKDIR /usr/app

# Set the environment variable for the port
ENV PORT 10000

# Expose the internal container port 
EXPOSE 10000

# Copy the built, runnable JAR file from the 'build' stage
COPY --from=build /app/target/DNDapp-1.0-SNAPSHOT.jar ./app.jar

# Define the command to run the application (starts GameServer.java)
ENTRYPOINT ["java", "-jar", "app.jar"]
