#!/usr/bin/env bash

# --- Styles ---
BOLD=$(tput bold)
DIM=$(tput dim)
UNDERLINE=$(tput smul)
REVERSE=$(tput rev)

RESET=$(tput sgr0)

# Standard 8 colors
BLACK=$(tput setaf 0)
RED=$(tput setaf 1)
GREEN=$(tput setaf 2)
YELLOW=$(tput setaf 3)
BLUE=$(tput setaf 4)
MAGENTA=$(tput setaf 5)
CYAN=$(tput setaf 6)
WHITE=$(tput setaf 7)

echo
echo
echo -e "${RESET}${BOLD}${BLUE}___ Customizing your OEAPI Environment ___${RESET}"
echo

# ============================
# ENVIRONMENT SELECTION
# ============================

# Allow passing dev/prod as first argument
if [[ -n "$1" ]]; then
    ENV_TARGET=$(echo "$1" | tr '[:upper:]' '[:lower:]')
else
    # Fallback to interactive mode only if no argument is provided
    read -p "Choose environment to generate (dev/prod): " ENV_TARGET
    ENV_TARGET=$(echo "$ENV_TARGET" | tr '[:upper:]' '[:lower:]')
fi

# Validate
if [[ "$ENV_TARGET" != "dev" && "$ENV_TARGET" != "prod" ]]; then
    echo -e "${RESET}${BOLD}${RED}Invalid option. Use: dev or prod${RESET}"
    exit 1
fi

echo -e "${RESET}${BOLD}${GREEN}Environment selected: $ENV_TARGET${RESET}"

CONFIG_TARGET_DIR="config"
mkdir -p "$CONFIG_TARGET_DIR"

# -------------------------------
# Main Values
# -------------------------------
echo
echo -e "${RESET}${BOLD}${WHITE}Main Values${RESET}"
echo
echo "This section is dedicated to configuring the institution that will use the endpoint. Here you can specify the name of your institution, your primary language, the institution’s UUID, and other related details."
echo
echo "The UUID of your organization or institution must be unique among all institutions that may use the OEAPI specification. You can generate a random UUID for this purpose, or it may be provided externally—for example, by an alliance or consortium you belong to."
echo

read -p "---> University/Organization organization name (default: 'OEAPI Test Organization'): " ORG_NAME
ORG_NAME=${ORG_NAME:-"OEAPI Test Organization"}

read -p "---> University/Organization short name (default: 'OEAPI_TEST'): " ORG_SHORT
ORG_SHORT=${ORG_SHORT:-"OEAPI_Test"}

read -p "---> Organization UUID code (default 0): " ORG_CODE
ORG_CODE=${ORG_CODE:-0}

read -p "---> Organization URL (default https://example.com ): " ORG_URL
ORG_URL=${ORG_URL:-"https://example.com"}

read -p "---> Default country [NL, IT, FR, ES,... Default: EN]: " INPUT_COUNTRY 
COUNTRY="${INPUT_COUNTRY:-EN}"

read -p "---> Default teaching language - ISO 639-2, 3 Char code (nld, eng, spa, fra, ita,...default eng): " TEACH_LANG
TEACH_LANG=${TEACH_LANG:-eng}


# -------------------------------
# Server settings
# -------------------------------
echo
echo -e "${RESET}${BOLD}${WHITE}Server settings${RESET}"
echo
echo "This values will configure how the endpoint will be accessed or viewed from internet. Default is port 57075 and its URL will be http://localhost:57075"
echo
echo "If endpoint is proxied to be seen on internet as https://miOrganization/oeapi this should be the value for endpoint URL.  "
echo

read -p "---> Server port (default 57075): " SERVER_PORT
SERVER_PORT=${SERVER_PORT:-57075}

read -p "---> Default endpoint URL [http://localhost:57075]  (If proxied, put proxied URL): " INPUT_ENDPOINT
ENDPOINT="${INPUT_ENDPOINT:-http://localhost:57075}"


# -------------------------------
# Database settings
# -------------------------------
echo
echo -e "${RESET}${BOLD}${WHITE}Database settings${RESET}"
echo
echo "The endpoint manages the tables and handles all data operations. It only needs to know the location of the database that will store the data."
echo
echo "By default, it uses the MySQL server available when the endpoint is deployed in a Docker or similar container. Using the default settings provided in these steps is the most common choice. Other options include pointing the endpoint to a different DBMS, either inside or outside the container, if you prefer to store the data elsewhere."
echo
echo "If you plan to switch between development and production within the same container, you can use different schemas within the same database (for example, the same database URL but different users)"
echo

read -p "---> Database URL (default is use MySQL in Docker: jdbc:mysql://mysqldb/oeapi_qs...): " DB_URL
DB_URL=${DB_URL:-jdbc:mysql://mysqldb/oeapi_qs?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC}

read -p "---> Database username (default use Docker: oeapi_qs): " DB_USER
DB_USER=${DB_USER:-oeapi_qs}

read -p "---> Database password (default use Docker: oeapi_qs): " DB_PASS
DB_PASS=${DB_PASS:-oeapi_qs}

# -------------------------------
# OEAPI metadata
# -------------------------------
echo
echo -e "${RESET}${BOLD}${WHITE}OEAPI metadata${RESET}"
echo
echo "This property could be used as the email to contact with the people responsible of the endpoint on your university. It is safe to leave the default value if you do not know what could be that email in your case."
echo 

read -p "---> Contact email (default ooapiAdmin-spec5-1.0_qs@myUniv.edu): " CONTACT_EMAIL
CONTACT_EMAIL=${CONTACT_EMAIL:-ooapiAdmin-spec5-1.0_qs@myUniv.edu}


# -------------------------------
# CORS
# -------------------------------
echo
echo -e "${RESET}${BOLD}${WHITE}CORS Config${RESET}"
echo
echo "The CORS (Cross-Origin Resource Sharing) configuration specifies which sites or domains are allowed to interact with the endpoint. It can be used to enhance security by allowing requests only from trusted domains such as myuniversity.edu or myalliance.edu."
echo
echo "If you do not know all the possible sources or clients in advance, you can leave it set to . However, restricting access to trusted origins is considered good practice (a list of sites or domains separated by “,” is admitted)"
echo

read -p "---> Allowed CORS origins (default *): " CORS_ORIGINS
CORS_ORIGINS=${CORS_ORIGINS:-*}


# -------------------------------
# Security
# -------------------------------
echo
echo -e "${RESET}${BOLD}${WHITE}Security${RESET}"
echo
echo "The endpoint includes security based on JWT. A static token can also be used as an additional method to authorize operations."
echo

read -p "---> Enable security? (true/false, default: true) : " SECURITY_ENABLED
SECURITY_ENABLED=$(echo "${SECURITY_ENABLED:-true}" | tr '[:upper:]' '[:lower:]')

if [[ "$SECURITY_ENABLED" == "true" ]]; then

    read -p "---> Static JWT token value [default: '74_yJhbGciOi81jn123901nn32788eyJzdWIiOiJ0ZXN0QHVuaXYtdW5pdGEuZXUiLCJyb2xlcyI6WyJST0xFX0FETUlOIl0sImlzcyI6IkNvZGVKYXZhIiwia']: " INPUT_TOKEN
    TOKEN="${INPUT_TOKEN:-'74_yJhbGciOi81jn123901nn32788eyJzdWIiOiJ0ZXN0QHVuaXYtdW5pdGEuZXUiLCJyb2xlcyI6WyJST0xFX0FETUlOIl0sImlzcyI6IkNvZGVKYXZhIiwia'}"

    read -p "---> Default user emails (comma-separated) [default: test@example.com,it.support@example.com]: " INPUT_EMAILS
    EMAILS="${INPUT_EMAILS:-'test@example.com,it.support@example.com'}"

    read -p "---> Default user password [92k2k233c]: " INPUT_PASS
    PASS="${INPUT_PASS:-'92k2k233c'}"
fi

read -p "---> Even if security is off, assign a JWT secret (default abcccfghijklmnOOORSTUVWABC123456): " JWT_SECRET
JWT_SECRET=${JWT_SECRET:-abcccfghijklmnOOORSTUVWABC123456}

# -------------------------------
# Tiny Dashboard
# -------------------------------
echo
echo -e "${RESET}${BOLD}${WHITE}Tiny Dashboard${RESET}"
echo
echo "A small built‑in frontend is included to help you view and check the data in your OEAPI instance. (You can access it at (default): http://localhost:57075/oeapi-td.html )"
echo
echo "Many of the values required for the endpoint to operate are gathered in other sections; the only optional value you may provide here is the location of an alternate logo."
echo 

read -p "---> Default logo path [./img/OpenEducationApi_Logo.png]: " INPUT_LOGO
LOGO="${INPUT_LOGO:-./img/OpenEducationApi_Logo.png}"


# -------------------------------
# Write .env file
# -------------------------------
echo
echo
echo -e "${RESET}${BOLD}${CYAN}Writing .env file...${RESET}"
echo "SERVER_PORT=$SERVER_PORT" > .env
echo "ENV_TARGET=$ENV_TARGET" >> .env
echo
cat .env
echo
echo ".env file created."

# -------------------------------
# Create timestamped backup
# -------------------------------
#
echo
mkdir -p env-history
TIMESTAMP=$(date +"%Y%m%d-%H%M%S")
cp .env "env-history/env-$TIMESTAMP"
echo "Backup created: env-history/env-$TIMESTAMP"

# -------------------------------
# Generate application-custom.properties
# -------------------------------
generate_properties() {
    TARGET_DIR="$CONFIG_TARGET_DIR/$1"
    mkdir -p "$TARGET_DIR"

    cat <<EOF > "$TARGET_DIR/application-custom.properties"
server.port=$SERVER_PORT
server.servlet.encoding.charset=UTF-8

spring.datasource.url=$DB_URL
spring.datasource.username=$DB_USER
spring.datasource.password=$DB_PASS
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.sql.init.mode=always
spring.jackson.default-property-inclusion=non-null

ooapi.metadata.contactEmail=$CONTACT_EMAIL
ooapi.metadata.specification=https://openonderwijsapi.nl/
ooapi.metadata.documentation=https://open-education-api.github.io/specification/v5/docs.html

ooapi.config.defaultTeachingLanguage=$TEACH_LANG

ooapi.config.autoCreateByDefault=false
ooapi.config.autoCreateOfferIfNotExists=false
ooapi.config.autoCreateOrgIfNotExists=false
ooapi.config.autoCreateProgramIfNotExists=false
ooapi.config.autoCreateCoordinatorIfNotExists=false

ooapi.config.autoCreateOrg_Name=$ORG_NAME
ooapi.config.autoCreateOrg_Code=$ORG_CODE
ooapi.config.autoCreateOrg_ShortName=$ORG_SHORT
ooapi.config.autoCreateOrg_Type=root

ooapi.cors.allowed.origins=$CORS_ORIGINS

logging.level.ooapi.unita.eu=INFO

ooapi.security.enabled=$SECURITY_ENABLED
app.static.token.allow=true
app.static.token.value=$TOKEN
app.static.token.user=token_user
app.static.token.role=ROLE_USER

ooapi.security.default.users.emails=test@univ-unita.eu,it.support@univ-unita.eu
ooapi.security.default.users.pass=92k2k233c

quickdashboard.config.ooapiDefaultCountry=$COUNTRY
quickdashboard.config.ooapiDefaultLogo=$LOGO
quickdashboard.config.ooapiDefaultShortUnivName=$ORG_SHORT
quickdashboard.config.ooapiDefaultUnivName=$ORG_NAME
quickdashboard.config.ooapiDefaultEndpointURL=$ENDPOINT
quickdashboard.config.ooapiDefaultOrganizationId=$ORG_CODE

# On deployment config/dev or config/prod will be mapped to config on Docker
quickdashboard.auto-create-file.organizations=config/initial_org.json

app.jwt.secret=$JWT_SECRET
EOF

    echo "Generated: $TARGET_DIR/application-custom.properties"
}



generate_initial_org_json() {

    TARGET_DIR="$CONFIG_TARGET_DIR/$1"
    mkdir -p "$TARGET_DIR"

    cat <<EOF > "$TARGET_DIR/initial_org.json"
[{
    "organizationId": "$ORG_CODE",
    "primaryCode": {
        "codeType": "identifier",
        "code": "$ORG_NAME"
    },
    "organizationType": "root",
    "name": [
        {
            "language": "en-GB",
            "value": "$ORG_NAME"
        }
    ],
    "shortName": "$ORG_SHORT",
    "link"     : "$ORG_URL"
}]
EOF

    echo "Generated: $TARGET_DIR/org_initial.json"
}



if [[ "$ENV_TARGET" == "dev" ]]; then
    generate_properties "dev"
    generate_initial_org_json "dev"
fi

if [[ "$ENV_TARGET" == "prod" ]]; then
    generate_properties "prod"
    generate_initial_org_json "prod"
fi

if [[ "$ENV_TARGET" == "test"  ]]; then
    generate_properties "test"
    generate_initial_org_json "test"
fi

echo "Done."



