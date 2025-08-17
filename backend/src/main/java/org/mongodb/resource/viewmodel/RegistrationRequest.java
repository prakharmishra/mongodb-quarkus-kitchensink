package org.mongodb.resource.viewmodel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegistrationRequest(
    @NotBlank(message = "First name cannot be blank")
    String firstName,

    @NotBlank(message = "Last name cannot be blank")
    String lastName,

    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    String phoneNumber
) {}
