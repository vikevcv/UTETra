# Giai đoạn 1: Build mã nguồn với Maven và JDK 25 (dự án yêu cầu Java 25 trong pom.xml)
FROM maven:3-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
# Tải dependencies trước để tận dụng cache
RUN mvn dependency:go-offline -B
COPY src ./src
# Build bỏ qua test lúc build container (test sẽ giao cho CI)
RUN mvn clean package -DskipTests

# Giai đoạn 2: Tạo runtime image nhẹ nhàng
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
# Copy file build từ stage 1 (dự án build ra file .war trong thư mục target)
COPY --from=build /app/target/*.war app.war

# Render tự động cấp port qua biến môi trường PORT (mặc định Render gán cổng nội bộ 8080)
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-Djava.net.preferIPv4Stack=true", "-XX:+UseSerialGC", "-Xmx320m", "-Xss512k", "-jar", "app.war"]



