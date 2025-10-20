package app.dto.SourceDTOs;

import java.util.Map;

public record SourceCreateDTO(
        String name,
        String baseUrl,
        String allowedPathPattern,
        Map<String, Object> selectors,
        boolean publicReadable,
        boolean enabled
) {
}
