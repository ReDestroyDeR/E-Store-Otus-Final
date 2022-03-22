package ru.red.authenticationservice.dto;

import lombok.Data;

/**
 * Data Transfer Object used in order to transmit credentials on Network<br>
 * Detached from business logic and thus contains no ID
 */
@Data
public class UserDetachedDTO {
    private String username;
    private String password;
}
