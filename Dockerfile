# 1. Use an official Java Runtime as the base image for building
# *** IMPORTANT: Changed version from 17 to 21 to match your pom.xml ***
FROM eclipse-temurin:21-jdk AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the entire project code into the container
COPY . .

# Package the application (runs 'mvn clean install')
# This command is where the "invalid target release: 21" error was occurring
RUN mvn clean install -DskipTests

# 2. Use a smaller, production-ready JRE base image for the final stage
# *** IMPORTANT: Changed version from 17 to 21 to match your pom.xml ***
FROM eclipse-temurin:21-jre-alpine

# Set the working directory in the final image
WORKDIR /usr/app

# Set the environment variable for the port
ENV PORT 10000

# Copy the built JAR file from the 'build' stage
# CRITICAL: Replace 'DNDapp-1.0-SNAPSHOT.jar' with the actual name of your compiled JAR file
COPY --from=build /app/target/DNDapp-1.0-SNAPSHOT.jar ./app.jar

# Define the command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
