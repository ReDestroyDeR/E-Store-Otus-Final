package ru.red.authenticationservice.dto;

import lombok.Data;

/**
 * Data Transfer Object used internally to represent UserRecord<br>
 * Lightweight simplification of Record object<br>
 * Attached to business Logic and has ID Field
 */
@Data
public class UserDTO {
    private Long id;
    private String username;
    private String password;
}
