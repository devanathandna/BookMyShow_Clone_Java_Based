# Use Java 17
FROM eclipse-temurin:17-jdk-alpine

# Set working directory
WORKDIR /app

# Copy lib folder (MongoDB driver etc.)
COPY lib/ ./lib/

# Copy all Java source files
COPY *.java ./

# Compile all Java files (no package - default package)
RUN javac -cp "lib/*" -d out *.java

# Expose port
EXPOSE 8000

# Run the backend server (no package prefix)
CMD ["java", "-cp", "out:lib/*", "BackendServer"]
