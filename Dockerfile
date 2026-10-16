# ============================================================
# Stage 1: The Build Stage
# We start with a base image that has all the tools we need to
# build our app: a specific version of Maven and OpenJDK 17.
# We name this stage 'builder' so we can refer to it later.
# ============================================================

FROM maven:3.9-eclipse-temurin-21 AS builder

# Set the working directory inside the container. All subsequent
# commands will be run from this directory.
WORKDIR /app

# Copy the pom.xml file first. This is a key optimization for Docker's layer cache.
# If the pom.xml hasn't changed, Docker will reuse the cached dependency layer,
# making subsequent builds much faster.
COPY pom.xml .

# Download all the project dependencies. This command downloads the dependencies
# specified in pom.xml and stores them in the local Maven repository.
# This creates another cacheable layer.
RUN mvn dependency:go-offline

# Now, copy the rest of the application's source code into the container.
COPY src ./src

# Build the application! This command compiles the source code and packages it
# into an executable "fat" JAR file. We skip the tests because they should be run
# in a CI/CD pipeline, not during the image build process.
RUN mvn clean package -DskipTests
# ==============================================================================
# Stage 2: The Runtime Stage
# This is the final stage that will produce our lean, production-ready image.
# We start from a minimal Java 17 Runtime Environment base image ('jre-slim').
# This image is much smaller than the full JDK, perfect for running the app.
# ==============================================================================
FROM eclipse-temurin:21-jre

# Set the working directory for the runtime environment.
WORKDIR /app

# This is the magic of the multi-stage build. We copy ONLY the built .jar file
# from the 'builder' stage into our new, clean runtime stage.
# We rename it to 'app.jar' for a consistent and predictable filename.
COPY --from=builder /app/target/*.jar app.jar

# Expose port 8080. This is a form of documentation that tells the person
# running the container that the application inside listens on this port.
# It does not actually publish the port to the host machine.
EXPOSE 8080

# This is the command that will be executed when a container is started from this image.
# It tells Java to run our Spring Boot application.
ENTRYPOINT ["java", "-jar", "app.jar"]