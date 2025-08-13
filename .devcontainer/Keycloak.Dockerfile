# Start with the official Keycloak image
FROM quay.io/keycloak/keycloak:26.3.2

# Copy the custom entrypoint script into a specific directory inside the container
COPY keycloak-entrypoint.sh /opt/keycloak/bin/keycloak-entrypoint.sh

# Make the script executable.
# It ensures the command runs on the file inside the container's filesystem.
USER root
RUN chmod +x /opt/keycloak/bin/keycloak-entrypoint.sh
USER keycloak

# Set the custom script as the new entrypoint for the container
ENTRYPOINT [ "/opt/keycloak/bin/keycloak-entrypoint.sh" ]