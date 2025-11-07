

FROM maven:3.8.7-openjdk-18-slim AS build  
ARG CI_JOB_TOKEN
ENV CI_JOB_TOKEN=$CI_JOB_TOKEN
COPY src /usr/src/app/src  
COPY pom.xml /usr/src/app  
RUN mvn -f /usr/src/app/pom.xml clean package

#
# PACKAGE STAGE
#
FROM eclipse-temurin:18-jre-alpine
COPY --from=build /usr/src/app/target/oeapi_qs.jar  /usr/app/oeapi_qs.jar
EXPOSE 57075
CMD ["java","-jar","/usr/app/oeapi_qs.jar"]  


