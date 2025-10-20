package app.dto.ItemDTOs;

import java.time.Instant;
import java.util.Map;

/**
 * @param id is the
 * @param sourceId
 * @param url the url you scraped from
 * @param createdAt is the exact time from whn you get the data and it is persisted
 * @param data is a hashmap so that the String is price, description, reviews, etc and the Object is the actual data
 */
public record ItemDTO(
        long id,
        long sourceId,
        String url,
        Instant createdAt,
        Map<String, Object> data
) {
}
