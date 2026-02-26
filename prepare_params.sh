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
read -p "University/Organization organization name (default: 'OEAPI Test Organization'): " ORG_NAME
ORG_NAME=${ORG_NAME:-"OEAPI Test Organization"}

read -p "University/Organization short name (default: 'OEAPI_TEST'): " ORG_SHORT
ORG_SHORT=${ORG_SHORT:-"OEAPI_Test"}

read -p "Organization UUID code (default 0): " ORG_CODE
ORG_CODE=${ORG_CODE:-0}

read -p "Default country [Default: EN]: " INPUT_COUNTRY 
COUNTRY="${INPUT_COUNTRY:-EN}"

read -p "Default teaching language -3 Char code- (default eng): " TEACH_LANG
TEACH_LANG=${TEACH_LANG:-eng}


# -------------------------------
# Server settings
# -------------------------------
echo
echo -e "${RESET}${BOLD}${WHITE}Server settings${RESET}"
echo
read -p "Server port (default 57075): " SERVER_PORT
SERVER_PORT=${SERVER_PORT:-57075}

read -p "Default endpoint URL [http://localhost:57075]  (If proxied, put proxied URL): " INPUT_ENDPOINT
ENDPOINT="${INPUT_ENDPOINT:-http://localhost:57075}"


# -------------------------------
# Database settings
# -------------------------------
echo
echo -e "${RESET}${BOLD}${WHITE}Database settings${RESET}"
echo
read -p "Database URL (default is use MySQL in Docker: jdbc:mysql://mysqldb/oeapi_qs...): " DB_URL
DB_URL=${DB_URL:-jdbc:mysql://mysqldb/oeapi_qs?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC}

read -p "Database username (default use Docker: oeapi_qs): " DB_USER
DB_USER=${DB_USER:-oeapi_qs}

read -p "Database password (default use Docker: oeapi_qs): " DB_PASS
DB_PASS=${DB_PASS:-oeapi_qs}

# -------------------------------
# OEAPI metadata
# -------------------------------
echo
echo -e "${RESET}${BOLD}${WHITE}OEAPI metadata${RESET}"
echo
read -p "Contact email (default ooapiAdmin-spec5-1.0_qs@myUniv.edu): " CONTACT_EMAIL
CONTACT_EMAIL=${CONTACT_EMAIL:-ooapiAdmin-spec5-1.0_qs@myUniv.edu}


# -------------------------------
# CORS
# -------------------------------
echo
echo -e "${RESET}${BOLD}${WHITE}CORS Config${RESET}"
echo
read -p "Allowed CORS origins (default *): " CORS_ORIGINS
CORS_ORIGINS=${CORS_ORIGINS:-*}


# -------------------------------
# Security
# -------------------------------
echo
echo -e "${RESET}${BOLD}${WHITE}Security${RESET}"
echo
read -p "Enable security? (true/false, default: true) : " SECURITY_ENABLED
SECURITY_ENABLED=$(echo "${SECURITY_ENABLED:-true}" | tr '[:upper:]' '[:lower:]')

if [[ "$SECURITY_ENABLED" == "true" ]]; then

    read -p "Static JWT token value [default: '74_yJhbGciOi81jn123901nn32788eyJzdWIiOiJ0ZXN0QHVuaXYtdW5pdGEuZXUiLCJyb2xlcyI6WyJST0xFX0FETUlOIl0sImlzcyI6IkNvZGVKYXZhIiwia']: " INPUT_TOKEN
    TOKEN="${INPUT_TOKEN:-'74_yJhbGciOi81jn123901nn32788eyJzdWIiOiJ0ZXN0QHVuaXYtdW5pdGEuZXUiLCJyb2xlcyI6WyJST0xFX0FETUlOIl0sImlzcyI6IkNvZGVKYXZhIiwia'}"

    read -p "Default user emails (comma-separated) [default: test@example.com,it.support@example.com]: " INPUT_EMAILS
    EMAILS="${INPUT_EMAILS:-'test@example.com,it.support@example.com'}"

    read -p "Default user password [92k2k233c]: " INPUT_PASS
    PASS="${INPUT_PASS:-'92k2k233c'}"
fi

read -p "Even if security is off, assign a JWT secret (default abcccfghijklmnOOORSTUVWABC123456): " JWT_SECRET
JWT_SECRET=${JWT_SECRET:-abcccfghijklmnOOORSTUVWABC123456}

# -------------------------------
# Tiny Dashboard
# -------------------------------
echo
echo -e "${RESET}${BOLD}${WHITE}Tiny Dashboard${RESET}"
echo
read -p "Default logo path [./img/OpenEducationApi_Logo.png]: " INPUT_LOGO
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
    "shortName": "$ORG_SHORT"
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



