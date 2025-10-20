package app.dto.CrawlLogDTOs;

public record CrawlLogDTO(
        long id,
        long sourceId,
        long requestedByUserId,
        String status,
        String error,
        Integer itemCount
){

}
