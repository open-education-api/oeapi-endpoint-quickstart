# Installing the Quickstart as a Microservice Using the JAR File (Without Docker)

## Introduction

In some cases, you may prefer to run the application directly from the JAR file instead of deploying it via Docker.

Installing the quickstart endpoint this way also provides additional flexibility, such as:

* Using an external application.properties file, making it easier to change configuration options or behavior without rebuilding the application.
* Using customized static resources (for example, modifying or replacing the tiny dashboard).

### Using an external application.properties

To use an external application.properties, you need to build the application so that the default application.properties file is not included in the generated JAR.

To do this, simply uncomment the following lines in the pom.xml file:

       <resources>
            <resource>
                <directory>src/main/resources</directory>
                <includes>
                    <include>*/**</include>
                </includes>
                **<!-- To use an external application.properties uncomment these lines
                <excludes>
                    <exclude>**/*.properties</exclude>
                </excludes>     
                -->**                       
            </resource>
        </resources>

Additionally, a clean build is recommended to ensure that any application.properties files from previous builds are not inadvertently included in the generated JAR.

When launching the application like 

java -jar oeapi_qs.jar 

Spring Boot will automatically look for an application.properties file in a subdirectory named config under the current working directory.
Simply place your customized application.properties file there.

All configuration values can be customized to fit your needs (language, security settings, etc.). In particular, if you are not using Docker, the database backend configuration must be explicitly defined.

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


