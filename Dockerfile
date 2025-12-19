# Use Java 17
FROM eclipse-temurin:17-jdk-alpine

# Set working directory
WORKDIR /app

# Copy lib folder (MongoDB driver etc.)
COPY lib/ ./lib/

# Copy all Java source files
COPY *.java ./

# Create the package directory structure and move the files into it
RUN mkdir -p com/bookmyshow
RUN mv *.java com/bookmyshow/

# Compile all Java files, placing the output in the 'out' directory
RUN javac -cp "lib/*" -d out com/bookmyshow/*.java

# Expose the port your server runs on
EXPOSE 8000

# Run the backend server using its fully qualified name
CMD ["java", "-cp", "out:lib/*", "com.bookmyshow.BackendServer"]
