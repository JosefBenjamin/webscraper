package app.dto.SourceDTOs;

import java.util.Map;

/**
 * Notice two small but important differences from SourceCreateDTO:
 * Uses Boolean (object type) instead of boolean -> allows null (field not provided).
 * All fields are optional — the service layer checks for null before updating.
 */
public record SourceUpdateDTO(
        String name,
        String baseUrl,
        String allowedPathPattern,
        Map<String, Object> selectors,
        Boolean publicReadable,
        Boolean enabled
) {

}
