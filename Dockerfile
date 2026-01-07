FROM maven:3-eclipse-temurin-21 AS build
ARG CI_JOB_TOKEN
ENV CI_JOB_TOKEN=$CI_JOB_TOKEN
COPY src /usr/src/app/src  
COPY pom.xml /usr/src/app  
RUN mvn -f /usr/src/app/pom.xml clean package

FROM gcr.io/distroless/java21-debian12:nonroot
COPY --from=build /usr/src/app/target/oeapi_qs.jar /oeapi_qs.jar
WORKDIR /
ENTRYPOINT ["java", "-jar", "oeapi_qs.jar"]
