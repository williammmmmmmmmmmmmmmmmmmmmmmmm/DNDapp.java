# ... (rest of Stage 2) ...

FROM eclipse-temurin:21-jre-alpine

# Set the working directory in the final image
WORKDIR /usr/app

# Set the environment variable for the port
ENV PORT 10000

# *** CRITICAL: Ensure this EXPOSE line is present ***
EXPOSE 10000

# Copy the built JAR file from the 'build' stage
COPY --from=build /app/target/DNDapp-1.0-SNAPSHOT.jar ./app.jar

# Define the command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
