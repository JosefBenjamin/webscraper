package app.dto.SourceDTOs;

import java.util.Map;

//TODO: For creating
// selectors is a Map<String,Object> so the API can speak real JSON (easy to read/write).
public record SourceCreateDTO(
        String name,
        String baseUrl,
        String allowedPathPattern,
        Map<String, Object> selectors,
        boolean publicReadable,
        boolean enabled
) {
}
