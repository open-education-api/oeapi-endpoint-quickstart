#!/usr/bin/env bash

# --- Styles ---
BOLD=$(tput bold)
RESET=$(tput sgr0)
RED=$(tput setaf 1)
GREEN=$(tput setaf 2)
BLUE=$(tput setaf 4)
CYAN=$(tput setaf 6)
WHITE=$(tput setaf 7)

# -------------------------------
# Sanitize (remove CR + control chars, keep UTF-8)
# -------------------------------
sanitize() {
    printf '%s' "$1" | tr -d '\r' | sed 's/[[:cntrl:]]//g'
}

# -------------------------------
# Generic validated prompt
# usage: prompt_validate "message" "default" "regex" "error message"
# -------------------------------
prompt_validate() {
    local message="$1"
    local default="$2"
    local regex="$3"
    local error="$4"
    local input

    while true; do
        read -r -p "$message" input
        input=$(sanitize "$input")

        [[ -z "$input" ]] && input="$default"

        if [[ "$input" =~ $regex ]]; then
            printf '%s' "$input"
            return
        else
            echo -e "${RED}$error${RESET}" >&2
        fi
    done
}

echo
echo -e "${BOLD}${BLUE}___ Customizing your OEAPI Environment ___${RESET}"
echo

# ============================
# ENVIRONMENT SELECTION
# ============================

if [[ -n "$1" ]]; then
    ENV_TARGET=$(sanitize "$1")
    ENV_TARGET=$(echo "$ENV_TARGET" | tr '[:upper:]' '[:lower:]')
else
    ENV_TARGET=$(prompt_validate \
        "Choose environment to generate (dev/prod/test): " \
        "" \
        "^(dev|prod|test)$" \
        "Invalid option. Allowed: dev, prod, test")
fi

echo -e "${GREEN}Environment selected: $ENV_TARGET${RESET}"

CONFIG_TARGET_DIR="config"
mkdir -p "$CONFIG_TARGET_DIR"

TARGET_DIR="$CONFIG_TARGET_DIR/$ENV_TARGET"
mkdir -p "$TARGET_DIR"


# -------------------------------
# Main Values
# -------------------------------
echo
echo -e "${BOLD}${WHITE}Main Values${RESET}"
echo
echo
echo "This section is dedicated to configuring the institution that will use the endpoint. Here you can specify the name of your institution, your primary language, the institution’s UUID, and other related details."
echo
echo "The UUID of your organization or institution must be unique among all institutions that may use the OEAPI specification. You can generate a random UUID for this purpose, or it may be provided externally—for example, by an alliance or consortium you belong to."
echo

ORG_NAME=$(prompt_validate \
    "---> Organization name (default OEAPI Test Organization): " \
    "OEAPI Test Organization" \
    "^.{1,200}$" \
    "Name cannot be empty.")

ORG_SHORT=$(prompt_validate \
    "---> Organization short name (default OEAPI_TEST): " \
    "OEAPI_TEST" \
    "^[A-Za-z0-9_-]{2,50}$" \
    "Short name: letters, numbers, _ or - only.")

ORG_CODE=$(prompt_validate \
    "---> Organization UUID code (default 0): " \
    "0" \
    "^[A-Za-z0-9._-]+$" \
    "Invalid code format.")

ORG_URL=$(prompt_validate \
    "---> Organization URL (default https://example.com): " \
    "https://example.com" \
    "^https?://.+$" \
    "URL must start with http:// or https://")

COUNTRY=$(prompt_validate \
    "---> Default country [NL, CH, IT, FR, ES,...] (default EN): " \
    "EN" \
    "^[A-Z]{2}$" \
    "Country must be 2 letters (uppercase).")

TEACH_LANG=$(prompt_validate \
    "---> Teaching language ISO 639-2 [nld, eng, spa, fra, ita,... ](default eng): " \
    "eng" \
    "^[a-z]{3}$" \
    "Language must be 3-letter ISO code. (lowercase)")

# -------------------------------
# Server settings
# -------------------------------
echo
echo -e "${BOLD}${WHITE}Server settings${RESET}"
echo
echo "This values will configure how the endpoint will be accessed or viewed from internet. Default is port 57075 and its URL will be http://localhost:57075"
echo


SERVER_PORT=$(prompt_validate \
    "---> Server port on container, not proxied (default 57075): " \
    "57075" \
    "^[0-9]{2,5}$" \
    "Port must be numeric (2-5 digits).")

echo "${CYAN}(If the endpoint is proxied to be seen on internet as something like 'https://miOrganization/oeapi' that URL should be the value for endpoint URL.) ${RESET} "

ENDPOINT=$(prompt_validate \
    "---> Endpoint URL (default http://localhost:57075): " \
    "http://localhost:57075" \
    "^https?://.+$" \
    "Invalid URL format.")


# -------------------------------
# Database settings
# -------------------------------
echo
echo -e "${BOLD}${WHITE}Database settings${RESET}"
echo
echo "The endpoint manages the tables and handles all data operations. It only needs to know the location of the database that will store the data."
echo
echo "By default, it uses the MySQL server available when the endpoint is deployed in a Docker or similar container. Using the default settings provided in these steps is the most common choice. Other options include pointing the endpoint to a different DBMS, either inside or outside the container, if you prefer to store the data elsewhere."
echo
echo "If you plan to switch between development and production within the same container, you can use different schemas within the same database (for example, the same database URL but different users)"
echo

DB_URL=$(prompt_validate \
    "---> Database URL (default is use MySQL in Docker: jdbc:mysql://mysqldb/oeapi_qs...): " \
    "jdbc:mysql://mysqldb/oeapi_qs_${ENV_TARGET:-test}?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC" \
    "^jdbc:.*$" \
    "Must start with jdbc:")

DB_USER=$(prompt_validate \
    "---> Database username (default oeapi_qs_${ENV_TARGET:-test}): " \
    "oeapi_qs_${ENV_TARGET:-test}" \
    "^[A-Za-z0-9._-]+$" \
    "Invalid username.")

DB_PASS=$(prompt_validate \
    "---> Database password (default oeapi_qs_${ENV_TARGET:-test}): " \
    "oeapi_qs_${ENV_TARGET:-test}" \
    "^.{1,100}$" \
    "Password cannot be empty.")

DB_DRIVER=$(prompt_validate \
    "---> DBMS Driver (default MySql 'com.mysql.cj.jdbc.Driver' ): " \
    "com.mysql.cj.jdbc.Driver" \
    "^.{1,100}$" \
    "Driver cannot be empty.")


# -------------------------------
# Metadata
# -------------------------------
echo
echo -e "${BOLD}${WHITE}OEAPI metadata${RESET}"
echo
echo "This property could be used as the email to contact with the people responsible of the endpoint on your university. It is safe to leave the default value if you do not know what could be that email in your case."
echo 

CONTACT_EMAIL=$(prompt_validate \
    "---> Contact email (default ooapiAdmin-spec5-1.0_qs@myUniv.edu): " \
    "ooapiAdmin-spec5-1.0_qs@myUniv.edu" \
    "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$" \
    "Invalid email format.")

# -------------------------------
# CORS
# -------------------------------
echo
echo -e "${BOLD}${WHITE}CORS Config${RESET}"
echo
echo "The CORS (Cross-Origin Resource Sharing) configuration specifies which sites or domains are allowed to interact with the endpoint. It can be used to enhance security by allowing requests only from trusted domains such as myuniversity.edu or myalliance.edu."
echo
echo "If you do not know all the possible sources or clients in advance, you can leave it set to *. However, restricting access to trusted origins is considered good practice (a list of sites or domains separated by “,” is admitted)"
echo

CORS_ORIGINS=$(prompt_validate \
    "---> Allowed CORS origins (comma separeted values) (default *): " \
    "*" \
    "^.*$" \
    "Invalid value.")

# -------------------------------
# Security
# -------------------------------
echo
echo -e "${BOLD}${WHITE}Security${RESET}"
echo
echo "The endpoint includes security based on JWT. A static token can also be used as an additional method to authorize operations."
echo

SECURITY_ENABLED=$(prompt_validate \
    "---> Enable security? (true/false, default true): " \
    "true" \
    "^(true|false)$" \
    "Must be true or false.")

echo

if [[ "$SECURITY_ENABLED" == "true" ]]; then

    TOKEN=$(prompt_validate \
        "---> Static App Token value [default: '74_yJhbGciOi81jn123901nn32788eyJzdWIiOiJ0ZXN0QHVuaXYtdW5pdGEuZXUiLCJyb2xlcyI6WyJST0xFX0FETUlOIl0sImlzcyI6IkNvZGVKYXZhIiwia']: " \
        "74_yJhbGciOi81jn123901nn32788eyJzdWIiOiJ0ZXN0QHVuaXYtdW5pdGEuZXUiLCJyb2xlcyI6WyJST0xFX0FETUlOIl0sImlzcyI6IkNvZGVKYXZhIiwia" \
        "^.{10,}$" \
        "Token too short.")

    EMAILS=$(prompt_validate \
        "---> Default user emails (comma-separated) [default: test@example.com,it.support@example.com]: " \
        "test@example.com,it.support@example.com" \
        "^.+$" \
        "Invalid email list.")

    EMAILPASS=$(prompt_validate \
        "---> Default user password (default 92k2k233c): " \
        "92k2k233c" \
        "^.{6,}$" \
        "Password must be at least 6 chars.")
fi

JWT_SECRET=$(prompt_validate \
    "---> JWT secret (default abcccfghijklmnOOORSTUVWABC123456): " \
    "abcccfghijklmnOOORSTUVWABC123456" \
    "^.{16,}$" \
    "Secret must be at least 16 chars.")

# -------------------------------
# Tiny Dashboard
# -------------------------------
echo
echo -e "${BOLD}${WHITE}Tiny Dashboard${RESET}"
echo
echo "A small built‑in frontend is included to help you view and check the data in your OEAPI instance. (You can access it at (default): http://localhost:57075/oeapi-td.html )"
echo
echo "Many of the values required for the endpoint to operate are gathered in other sections; the only optional value you may provide here is the location of an alternate logo."
echo 

LOGO=$(prompt_validate \
    "---> Logo path (default ./img/OpenEducationApi_Logo.png): " \
    "./img/OpenEducationApi_Logo.png" \
    "^.+$" \
    "Invalid path.")

# -------------------------------
# Write .env
# -------------------------------
echo
echo -e "${CYAN}Writing .env file...${RESET}"

cat <<EOF > .env
SERVER_PORT=$SERVER_PORT
ENV_TARGET=$ENV_TARGET
EOF

mkdir -p config-history
TIMESTAMP=$(date +"%Y%m%d-%H%M%S")
cp .env "config-history/env-$ENV_TARGET-$TIMESTAMP"


# -------------------------------
# Generate config
# -------------------------------

generate_properties() {

cat <<EOF > "$TARGET_DIR/application-custom.properties"

server.port=$SERVER_PORT
server.servlet.encoding.charset=UTF-8

spring.datasource.url=$DB_URL
spring.datasource.username=$DB_USER
spring.datasource.password=$DB_PASS
spring.datasource.driver-class-name=$DB_DRIVER

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
app.static.token.allow=$SECURITY_ENABLED
app.static.token.value=$TOKEN
app.static.token.user=token_user
app.static.token.role=ROLE_USER

ooapi.security.default.users.emails=$EMAILS
ooapi.security.default.users.pass=$EMAILPASS

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

# Initial Org -----------------

generate_initial_org_json() {

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


generate_properties $ENV_TARGET
cp "$TARGET_DIR/application-custom.properties" "config-history/application-custom-$ENV_TARGET-$TIMESTAMP.properties"

generate_initial_org_json $ENV_TARGET
cp "$TARGET_DIR/initial_org.json" "config-history/initial_org-$ENV_TARGET-$TIMESTAMP.json"

echo
echo -e "${BOLD}${GREEN}Configuration complete! ${RESET}"
echo
echo