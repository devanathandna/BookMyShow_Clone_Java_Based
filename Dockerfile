# Use Java 17
FROM eclipse-temurin:17-jdk-alpine

# Set working directory
WORKDIR /app

# Copy lib folder (MongoDB driver etc.)
COPY lib/ ./lib/

# Copy all Java source files
COPY *.java ./

# Create package directory and move files
RUN mkdir -p com/bookmyshow
RUN mv *.java com/bookmyshow/

# Compile all Java files with lib dependencies
RUN javac -cp "lib/*" -d out com/bookmyshow/*.java

# Expose port
EXPOSE 8000

# Run the backend server
CMD ["java", "-cp", "out:lib/*", "com.bookmyshow.BackendServer"]

