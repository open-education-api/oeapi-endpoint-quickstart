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
podman run -d \
    --name oeapi-endpoint-quickstart-mysql \
    -p 3306:3306 \
    -e MYSQL_ROOT_PASSWORD=x \
    -e MYSQL_DATABASE=oeapi_qs \
    -e MYSQL_USER=oeapi_qs \
    -e MYSQL_PASSWORD=oeapi_qs \
    docker.io/mysql
```

Open MySQL CLI:

```sh
podman exec -it oeapi-endpoint-quickstart-mysql \
    mysql -u oeapi_qs -p oeapi_qs
```

Setup spring database URL:

```sh
export SPRING_DATASOURCE_URL='jdbc:mysql://localhost/oeapi_qs'
```

#### Using MariaDB

Same as above but using `mariadb` instead of `mysql`.  Make sure the following application properties are in place:

```sh
export SPRING_DATASOURCE_URL=jdbc:mariadb://localhost/oeapi_qs
export SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.mariadb.jdbc.Driver
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
