# ------------------------
# Stage 1: Build
# ------------------------
FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .mvn mvnw mvnw.cmd ./
RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -DskipTests

# ------------------------
# Stage 2: Runtime
# ------------------------
FROM eclipse-temurin:17-jre
WORKDIR /app

RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-Xmx512m -Xms256m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1
