# Use OpenJDK 17 as base image
FROM openjdk:17-jdk-slim

# Install necessary packages (this layer will be cached unless base image changes)
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy Maven wrapper files first (cached unless these files change)
COPY mvnw .
COPY .mvn .mvn

# Make Maven wrapper executable and fix line endings
RUN chmod +x ./mvnw && \
    sed -i 's/\r$//' ./mvnw

# Copy only pom.xml for dependency resolution (cached unless pom.xml changes)
COPY pom.xml .

# Download dependencies (this expensive layer will be cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Create directory for logs (cached after first run)
RUN mkdir -p target

# Copy source code LAST (only this layer and subsequent ones rebuild when code changes)
COPY src ./src

# Build the application (only rebuilds when source code or pom.xml changes)
RUN ./mvnw clean package -DskipTests

# Run the application
CMD ["sh", "-c", "java -jar target/*.jar"]
