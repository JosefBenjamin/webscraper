package app.dto.SourceDTOs;

import java.time.Instant;
import java.util.Map;

public record SourceDTO(
        String name,
        String baseUrl,
        String allowedPathPattern,
        Map<String, Object> selectors,
        boolean publicReadable,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {
}
