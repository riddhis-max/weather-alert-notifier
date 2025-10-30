package com.weatheralert.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SubscribeRequest(@Email(message = "Invalid email format") String email,
@NotBlank(message = "City is required") String city
) {
    
}
