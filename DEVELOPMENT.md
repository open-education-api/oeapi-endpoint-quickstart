# Development


```sh
export JDK_JAVA_OPTIONS='--add-opens java.base/java.lang=ALL-UNNAMED --enable-native-access=ALL-UNNAMED'
```

## Requirements

### OpenJDK

Install OpenJDK version 18 or higher.

### Database

Run a database using docker (or podman):

```sh
podman run -p 3306:3306 \
    -e MYSQL_ROOT_PASSWORD=x \
    -e MYSQL_DATABASE=oeapi_qs \
    -e MYSQL_USER=oeapi_qs \
    -e MYSQL_PASSWORD=oeapi_qs \
    docker.io/mysql
```

Setup sprint database URL:

```sh
export SPRING_DATABASE_URL='jdbc:mysql://localhost/oeapi_qs'
```

## Running the application

Run the application in development mode:

```sh
mvn spring-boot:run
```

## Running the test suite

```sh
mvn -DskipTests=false test
```

### Running a single test

```sh
mvn -DskipTests=false -Dtest=oeapi.testingweb.OrganizationTest test
```
