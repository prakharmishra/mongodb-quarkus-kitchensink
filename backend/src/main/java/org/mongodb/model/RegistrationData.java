package org.mongodb.model;

import jakarta.validation.constraints.NotBlank;

public record RegistrationData(
    @NotBlank(message = "userId cannot be blank")
    String userId,
    @NotBlank(message = "username cannot be blank")
    String username,
    @NotBlank(message = "email cannot be blank")
    String email,
    @NotBlank(message = "firstName cannot be blank")
    String firstName,
    @NotBlank(message = "lastName cannot be blank")
    String lastName,

    String createdAt,
    boolean complete
) {}