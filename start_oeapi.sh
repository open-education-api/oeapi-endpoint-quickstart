#!/bin/bash

#!/bin/bash

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

# Project/src configuration folder
CONF_SRC="./src/main/resources"

# Config root folder
CONFIG_TARGET_DIR="config"
mkdir -p "$CONFIG_TARGET_DIR"

clear

echo -e "${BOLD}${GREEN}===========================================${RESET}"
echo -e "${BOLD}${GREEN}   OEAPI Quickstart Launcher               ${RESET}"
echo -e "${BOLD}${GREEN}===========================================${RESET}"
echo
echo -e "${RESET}${BOLD}${BLUE}Please select how you want to start your endpoint:${RESET}"
echo
echo -e "${BOLD}${WHITE}1${RESET}) I just want to try it!"
echo
echo -e "${BOLD}${WHITE}2${RESET}) Customize it for DEVEL"
echo
echo -e "${BOLD}${WHITE}3${RESET}) Customize it for PRODUCTION"
echo
echo -e "${BOLD}${WHITE}4${RESET}) Start my LAST CONFIGURED Endpoint"
echo
echo -ne "${RESET}${BOLD}${YELLOW}Your choice (1, 2, 3 or 4): ${RESET}"
read choice


# --- Main menu ---

case $choice in
  1)
    echo -e "\n${BOLD}${CYAN}Starting Demo mode...${RESET}"
   
    rm -f .env   # to avoid launching an unfinished install

    # As there is no interactive configuration in this cas, let's handle
    # some params needed to TEST mode to enable it to coexist with DEV or PROD      
    # deployments if present or if you want to switch between them

    TARGET_DIR="$CONFIG_TARGET_DIR/test" 
    mkdir -p "$TARGET_DIR" 

    cp "$CONF_SRC/application.properties" "$TARGET_DIR/application-custom.properties"
    cp "./examples/organizations.json"    "$TARGET_DIR/initial_org.json"
    
    # Change datasource to be follow pattern oeapi_qs_MODE, 
    # So each MODE could have distinct dedicated volume in the container allowing switching and isolation
    sed -i 's/\(spring.datasource.*\)oeapi_qs/\1oeapi_qs_test/' "$TARGET_DIR/application-custom.properties"
    
    # By default no orgs are loaded, let's put a default one. 
    # initial_org.json usually stores a real org for DEV or PROD, which data is captured on guided install.
    # In this case we previously put on initial_org.json use some orgs from ./examples
    echo "quickdashboard.auto-create-file.organizations=${CONFIG_TARGET_DIR}/initial_org.json" >> "$TARGET_DIR/application-custom.properties"

    ENV_TARGET=test
    SERVER_PORT=57075
    echo "ENV_TARGET=$ENV_TARGET" > .env
    echo "SERVER_PORT=$SERVER_PORT" >> .env 

    docker compose up -d
    ;;

  2)
    echo -e "\n${BOLD}${CYAN}Let's customize Development mode...${RESET}"
    rm -f .env   # to avoid launching an unfinished install
    ./prepare_params.sh dev    
    docker compose up -d --force-recreate
    ;;

  3)
    echo -e "\n${BOLD}${CYAN}Let's customize Production mode...${RESET}"
    rm -f .env   # to avoid launching an unfinished install
    ./prepare_params.sh prod
    docker compose up -d --force-recreate
    ;;

  4)
    echo -e "\n${BOLD}${CYAN}Restating your last configured Endpoint...${RESET}"
    echo
    echo -e "Environment is: "
    cat .env
    echo
    docker compose up -d 
    ;;

  *)
    echo -e "\n${BOLD}${RED}Invalid choice. Exiting.${RESET}"
    exit 1
    ;;
esac

echo -e "\n${BOLD}${GREEN} Done! Containers are starting in the background.${RESET}"
echo
echo -e "You can check logs with: ${CYAN}docker compose logs -f oeapi_qs${RESET}"
