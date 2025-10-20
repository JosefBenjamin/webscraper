package app.dto.UserDTOs;

import java.time.Instant;

public record UserDTO(
        Long id,
        String email,
        String role,
        Instant createdAt
) {
}
