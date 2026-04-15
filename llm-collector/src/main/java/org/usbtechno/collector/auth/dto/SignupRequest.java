package org.usbtechno.collector.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SignupRequest {

    @NotBlank(message = "name is required")
    @Size(max = 120, message = "name must be 120 characters or fewer")
    public String name;

    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    @Size(max = 190, message = "email must be 190 characters or fewer")
    public String email;

    @NotBlank(message = "password is required")
    @Size(min = 8, max = 128, message = "password must be between 8 and 128 characters")
    public String password;
}
