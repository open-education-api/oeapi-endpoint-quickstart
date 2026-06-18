
# Security

As mentioned earlier, this OEAPI endpoint can be updated using REST calls (for example, by sending a JSON object via POST or PUT). While this greatly simplifies course maintenance, you should not forget to secure it to prevent unwanted or unauthorized updates

Understanding in depth how to secure a networked resource is beyond the scope of this guide, and the security officers in your organization should provide the appropriate guidance. Nevertheless, additionally to that, we also offer a minimal set of recommendations that may help you.

The way your endpoint is intended to be used largely determines the security measures you should apply. For example, if it is only accessed from an internal network, you can block all internet access. If it is exposed to the internet, the implementation allows you to configure whether it provides read‑only access to courses or also allows adding courses, and so on.

There are therefore different ways and options for securing your endpoint, which can be applied and combined as needed

## By network configuration

This involves restricting the IP addresses or subnets that can access the endpoint and/or its update methods (POST, PUT, etc.). This is typically done using Apache or Nginx rules, but it can also be achieved through firewall settings.

It should be place behind a reverse proxy which limits the HTTP methods to GET otherwise anyone could perform uploads.

Sample nginx configuration:

  

```bash

...

location / {

limit_except GET {

allow 127.0.0.1;

deny all;

}

proxy_pass http://your_oeapi_url;

}

```

Unless you have a good reason not to, you should also block any internet access to the `*.html` files included in the endpoint installation. These files are intended for internal use and diagnostic checks. (Even if they are exposed, they normally cannot be used to perform any action without proper authorization (which should already be in place)  But in short, do not expose them unless there is a good reason to do so.)


## By using the endpoint’s built-in security features
 
In brief, the built‑in security measures can be divided into two domains: the security mode (read‑only, read/write, etc.) and the actors (who can access).
 
### Configuring Mode:

There are four modes or levels of security available for the endpoint:

| Level      | Description                                                                                   |
|------------|-----------------------------------------------------------------------------------------------|
| **None**       | Read and update operations require no authorization.                                         |
| **Guest**      | Read operations are allowed, but updates are forbidden (not even with the app token).        |
| **Restricted** | Read operations are allowed, and updates are possible but require authorization.             |
| **Private**    | Both read and update operations require authorization.                                      |

You can set the mode that best fits your needs during the installation dialog, or by editing the variable `ooapi.security.mode` in the `application.properties` file.

    # Security modes:
    #   - none: security disabled (ooapi.security.enabled=false)
    #   - guest: Read only access, no updates are allowed in any way
    #   - restricted: Readable with authorized updates (us/pass or app token)
    #   - private: Read and Updates require authorization
    ooapi.security.mode=restricted

Default mode is **restricted**

In the restricted and private modes, authorization is required. In the next section (‘Who’), we describe how this authorization works.

### Configuring "who":

#### Authorizing users

The endpoint can be configured to allow access only to specific users, ensuring that only authorized individuals can perform update operations.  In short, you can enable this by setting a property in the `application.properties` file. The security mechanism is based on JWT (JSON Web Tokens).

Once this option is enabled, you will be required to log in to access the update options (or even Reads on private mode) through the endpoint’s small dashboard, or to authenticate before performing POST, PUT, and similar requests via REST.

#### Using an API token

In addition to authorizing users, you can also use a predefined token to perform updates. This is configured using the variables `app.static.token.*` in the `application.properties` file.

    # Even when security is disabled do no comment this lines
    app.static.token.allow=true
    app.static.token.value=b74c4948-f606-44b5-95bf-07a2d2c06b06
    app.static.token.user=token_user
    app.static.token.role=ROLE_USER


## Summary

All the methods and configurations described in this section are typically combined. You apply certain network‑level security measures, and on top of them you can select the endpoint’s built‑in security features that best fit your needs.
