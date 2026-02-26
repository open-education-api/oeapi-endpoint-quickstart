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

# Project configuration folder
CONF_SRC="./src/main/resources"

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
echo -ne "${RESET}${BOLD}${YELLOW}Your choice (1, 2 or 3): ${RESET}"
read choice


# --- Lógica de Ejecución ---

case $choice in
  1)
    echo -e "\n${BOLD}${CYAN}Starting Demo mode...${RESET}"
    TARGET_DIR="$CONFIG_TARGET_DIR/test" 
    mkdir -p "$TARGET_DIR" 
    cp "$CONF_SRC/application.properties" "$TARGET_DIR/application-custom.properties"
    echo "ENV_TARGET=test" > .env
    echo "SERVER_PORT=57075" >> .env 
    docker compose up -d
    ;;

  2)
    echo -e "\n${BOLD}${CYAN}Let's customize Development mode...${RESET}"
    ./prepare_params.sh dev    
    docker compose up -d
    ;;
  3)
    echo -e "\n$${BOLD}{CYAN}Let's customize Production mode...${RESET}"
    ./prepare_params.sh pro
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
