#!/bin/bash
# keycloak-entrypoint.sh

# This script iterates through all environment variables.
# If a variable ends with '_FILE', it reads the content of the file
# specified by the variable and sets the corresponding non-'_FILE'
# variable with that content.

for var in $(env | grep _FILE= | sed 's/=.*//'); do
  # Strip the '_FILE' suffix to get the new variable name
  base_var=$(echo "$var" | sed 's/_FILE$//')
  
  # Read the content of the file and set the new environment variable
  if [ -f "${!var}" ]; then
    export "$base_var"="$(cat "${!var}")"
    echo "Loaded secret from ${!var} into $base_var"
  else
    echo "Warning: Secret file not found: ${!var}" >&2
  fi
done

# Execute the original Keycloak entrypoint with the start-dev command
/opt/keycloak/bin/kc.sh start-dev