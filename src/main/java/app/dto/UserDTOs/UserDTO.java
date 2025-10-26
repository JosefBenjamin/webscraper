package app.dto.UserDTOs;

import java.time.Instant;

public record UserDTO(
        Long id,
        String username,
        String role,
        Instant createdAt
) {
}
