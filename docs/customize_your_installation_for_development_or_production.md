




# Customize your installation for development or production
 

## Getting started

As explained earlier, when installing the endpoint there are three options: test mode, development mode, and production mode. In test mode, a quick and simple installation is performed, allowing you to try out the endpoint and understand how it works in just a few minutes.

In development and especially production modes, the installation is equally simple, but in these cases it is important to properly configure the parameters that the endpoint will use. By using these parameters, we indicate to the endpoint the name of our institution, our language, whether we want to enable security, and so on.

The installation is quite simple and the installer guides your through the process.  

## Guided installation

Once the project has been cloned from GitHub, the `start_oeapi.sh` script manages how the endpoint is configured and deployed.

Once `start_oeapi.sh` is launched, user is prompted with instalation options:

```console
===========================================
   OEAPI Quickstart Launcher 
===========================================

Please select how you want to start your endpoint:

1) I just want to try it!

2) Customize it for DEVEL

3) Customize it for PRODUCTION

Your choice (1, 2 or 3):
```

If the user chooses option 1, the installer performs a quick setup using the default parameters. No additional questions are asked, and the endpoint starts automatically.

If options 2 or 3 are selected, the installer guides the user through an interactive process to properly configure their endpoint

**1- Configuring Main Values**

This section is dedicated to configuring the institution that will use the endpoint. Here you can specify the name of your institution, your primary language, the institution’s UUID, and other related details.

The UUID of your organization or institution must be unique among all institutions that may use the OEAPI specification. You can generate a random UUID for this purpose, or it may be provided externally—for example, by an alliance or consortium you belong to.

```console
---> University/Organization organization name (default: 'OEAPI Test Organization'):
---> University/Organization short name (default: 'OEAPI_TEST'):
---> Organization UUID code (default 0):
---> Default country [Default: EN]:
---> Default teaching language -3 Char code- (default eng):
```

**2 - Configuring Server settings**

These values determine how the endpoint will be accessed or exposed on the internet. By default, it uses port 57075, and its URL will be http://localhost:57075.

If endpoint is proxied to be seen on internet as https://miOrganization/oeapi this should be the value for endpoint URL.  

```console
---> Server port (default 57075):
---> Default endpoint URL [http://localhost:57075]  (If proxied, put proxied URL):
```

**3 - Configuring Database Settings**


The endpoint manages the tables and handles all data operations. It only needs to know the location of the database that will store the data.

By default, it uses the MySQL server available when the endpoint is deployed in a Docker or similar container. Using the default settings provided in these steps is the most common choice. Other options include pointing the endpoint to a different DBMS, either inside or outside the container, if you prefer to store the data elsewhere.

If you plan to switch between development and production within the same container, you can use different schemas within the same database (for example, the same database URL but different users)


    ---> Database URL (default is use MySQL in Docker: jdbc:mysql://mysqldb/oeapi_qs...):
    ---> Database username (default use Docker: oeapi_qs):
    ---> Database password (default use Docker: oeapi_qs):


**4 - Configuring OEAPI metadata**

This property could be used as the email to contact with the people responsible of the endpoint on your university. It is safe to leave the default value if you do not know what could be that email in your case.

```console
---> Contact email (default ooapiAdmin-spec5-1.0_qs@myUniv.edu): 
```


**5 - Configuring CORS**

The CORS (Cross-Origin Resource Sharing) configuration specifies which sites or domains are allowed to interact with the endpoint. It can be used to enhance security by allowing requests only from trusted domains such as myuniversity.edu or myalliance.edu. If you do not know all the possible sources or clients in advance, you can leave it set to . However, restricting access to trusted origins is considered good practice (a list of sites or domains separated by "," is admitted)

```console
---> Allowed CORS origins (default *):
```

**6 -Configuring Security**

The endpoint includes security based on JWT. A static token can also be used as an additional method to authorize operations.

```console
---> Enable security? (true/false, default: true) :
---> Static JWT token value [default: '74_yJhbGciOi81jn123901nn32788eyJzdWIiOiJ0ZXN0QHVuaXYtdW5pdGEuZXUiLCJyb2xlcyI6WyJST0xFX0FETUlOIl0sImlzcyI6IkNvZGVKYXZhIiwia']:
---> Default user emails (comma-separated) [default: test@example.com,it.support@example.com]:
---> Default user password [92424233c]:
---> Even if security is off, assign a JWT secret (default abcccfghijklmn12039939VWABC123456):
```
**6 -Tiny Dashboard**

A small built‑in frontend is included to help you view and check the data in your OEAPI instance. (You can access it at (default): http://localhost:57075/oeapi-td.html )  


Many of the values required for the endpoint to operate are gathered in other sections; the only optional value you may provide here is the location of an alternate logo.

```console
---> Default logo path or URL [./img/OpenEducationApi_Logo.png]:
```


## Main actions

**Initiate and run Endpoint**

This will configure the endpoint to your needs, deploy it to container and starts it

```console
./start_oeapi.sh
```

**Just build image (when code changes)**

```console
   docker build -t oeapi . 
   docker-compose up -d 
 ```
  

**Switch between dev/prod/default**

You can easily select and switch between previously configured modes using:

```console
Change, for example to prod,  ENV_TARGET=prod  in .env
docker-compose up -d
```

## Technical explanation of the deployment process

For IT technicians, this is how the guided process is technically done.

Once the project has been cloned from GitHub, the start_oeapi.sh script manages how the endpoint is configured and deployed.

If Development or Production mode is selected, the prepare_params.sh script generates the appropriate profiles.

In all modes, Spring Boot is explicitly configured to use application-custom.properties, preventing the default application.properties file from being used as a fallback.

A docker-compose.override.yml that mounts the correct config (test,dev or prod) on container.

A persistent configuration directory survives deployments

Directory structure:
```tree
├── config
│   ├── default
│   │   └── application-custom.properties
│   ├── dev
│   │   └── application-custom.properties
│   ├── prod
│   └── application-custom.properties
├── env-history
├── start_oeapi.sh
├── prepare_params.sh
├── .env
├── docker-compose.yml
└── docker-compose.override.yml
```
