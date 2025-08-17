package org.mongodb.model;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record Member(
    @BsonId
    @JsonSerialize(using = ToStringSerializer.class)
    ObjectId id,

    @NotBlank(message = "Keycloak user ID cannot be blank")
    String userId,

    @NotBlank(message = "Username cannot be blank")
    String username,

    @NotBlank(message = "First name cannot be blank")
    String firstName,

    @NotBlank(message = "Last name cannot be blank")
    String lastName,

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    String email,

    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    String phoneNumber
) {}
