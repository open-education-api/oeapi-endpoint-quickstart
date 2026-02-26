# Installing the Quickstart as a Microservice Using the JAR File (Without Docker)

## Introduction

In some cases, you may prefer to run the application directly from the JAR file instead of deploying it via Docker.

Installing the quickstart endpoint this way also provides additional flexibility, such as:

* Using an external application.properties file, making it easier to change configuration options or behavior without rebuilding the application.
* Using customized static resources (for example, modifying or replacing the tiny dashboard).

## Building the JAR

You can build the JAR file from the source code using the IDE of your choice (e.g., NetBeans, Eclipse, etc.) or on the command line.

Before building the JAR, make sure to review the file located at _src/main/resources/application.properties_ and configure the datasource settings to point to your desired DBMS.

Building using the command line goes as follows:

```sh
mvn package
```

If the above fails with the following error:

```
[ERROR] Error executing Maven.
[ERROR] java.lang.IllegalStateException: Unable to load cache item
[ERROR] Caused by: Unable to load cache item
[ERROR] Caused by: Could not initialize class net.sf.cglib.core.MethodWrapper
[ERROR] Caused by: Exception net.sf.cglib.core.CodeGenerationException: java.lang.reflect.InaccessibleObjectException-->Unable to make protected final java.lang.Class java.lang.ClassLoader.defineClass(java.lang.String,byte[],int,int,java.security.ProtectionDomain) throws java.lang.ClassFormatError accessible: module java.base does not "opens java.lang" to unnamed module @13a5fe33 [in thread "main"]
```

add the following environment variable and try again:

```sh
export JDK_JAVA_OPTIONS='--add-opens java.base/java.lang=ALL-UNNAMED --enable-native-access=ALL-UNNAMED'
```

Once you've built the JAR file, and assuming your Java environment is properly set up, you can start the application with the following command (note that the JAR file name may vary):

```bash

nohup java -jar oeapi-qs.jar &

# (if jar is tagged with version, change accordingly)

```

Alternatively, you can provide a single external configuration file that overrides the internal _application.properties_. Do not name your file application.properties unless you want the internal file to be used as a fallback when yours is not found

```bash

export SPRING_CONFIG_LOCATION=./myOrg.properties
nohup java -jar oeapi_qs.jar > myOrg.log 2>&1 &

```

##### Managing Database (DBMS) in application.properties 

If the application is not deployed using Docker, you must configure a database management system (DBMS) to store the endpoint data.

Although the endpoint will automatically create all required tables and initial data, you must provide an existing (empty) database or schema for it to use.

The properties that control which database is used are shown below.
(The example values are MySQL-oriented, but any relational DBMS can be used.)

    # Using other DBMS
    #spring.jpa.hibernate.ddl-auto=update
    #spring.datasource.url=jdbc:mysql://your_server/yourDb
    #spring.datasource.username=someuser
    #spring.datasource.password=somepass
    #spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

### Using customized static resources (for example, modifying or replacing the tiny dashboard).

It is quite easy to configure the endpoint to use static resources different from those packaged inside the JAR file. To do so, simply uncomment and adjust the corresponding lines in the application.properties file.

    #Static HTMLs. (Tiny Dashboard)
    #Alternate location in case you do not want to use the ones in the jar
    #(If they are in more than one location,file:/opt/html/,classpath:/static/)
    #spring.web.resources.static-locations=file:/home/unita/oeapi/static/


