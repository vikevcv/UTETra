# Stage 1: Build source code với Java 25 sử dụng Maven Wrapper của dự án
FROM eclipse-temurin:25-jdk-noble AS build
WORKDIR /app

# Copy Maven Wrapper và pom.xml để tải dependencies
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
# Tải dependencies trước để tận dụng Docker cache
RUN ./mvnw dependency:go-offline -B

# Copy mã nguồn và tiến hành build package
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime image với Java 25
FROM eclipse-temurin:25-jre-noble
WORKDIR /app
COPY --from=build /app/target/*.[jw]ar app.war
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.war"]
