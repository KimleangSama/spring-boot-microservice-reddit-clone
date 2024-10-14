# Stage 1: Build the application
FROM openjdk:22-slim AS build
WORKDIR /opt/project
COPY pom.xml /opt/project/
COPY src /opt/project/src
RUN apt update && apt install -y maven
RUN mvn -f /opt/project/pom.xml clean package

# Stage 2: Run the application
FROM openjdk:22-slim
ENV TZ=Asia/Phnom_Penh
RUN apt -y install tzdata
COPY --from=build /opt/project/target/config-service-0.0.1.jar config-service-0.0.1.jar
EXPOSE 8080
CMD ["java", "-jar", "config-service-0.0.1.jar"]
