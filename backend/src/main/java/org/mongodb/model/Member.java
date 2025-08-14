package org.mongodb.model;

public record Member(
    String id,
    String name,
    String email,
    String phoneNumber
) {}
