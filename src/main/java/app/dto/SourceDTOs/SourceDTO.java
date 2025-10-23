package app.dto.SourceDTOs;

import java.time.Instant;
import java.util.Map;

//TODO: For reading since createdAt and updatedAt happens at the DB level
public record SourceDTO(
        String name,      //A humanreadable name of what you are crawling
        String baseUrl,   //Initial url path to start the scrape from e.g. mysite.com/products/
        String allowedPathPattern,   //Only allows the crawler to scrape items off a given path like /products/item1, /products/item2, etc.
        Map<String, Object> selectors,  //tells the crawler which CSS or XPath selectors to use when scraping.
        boolean publicReadable,  //A flag that determines whether other users (besides the owner) can view or query the scraped data (ScrapedData) from this source
        boolean enabled,    //Turn on or off the crawl at anything during the crawl
        Instant createdAt,  //When it was created in a proper data time format
        Instant updatedAt   //When it was updated in a proper data time format
) {
}
