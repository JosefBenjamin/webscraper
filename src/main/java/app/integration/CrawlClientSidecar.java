package app.integration;

import app.utils.Utils;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.Map;

public class CrawlClientSidecar {
    private final HttpClient http = HttpClient.newHttpClient(); // Java's own http client
    private final String base = System.getenv().getOrDefault("CRAWLER_BASE", "http://localhost:8000");

    /** Method to start crawl
     *
     * @param url - the page to scrape
     * @param selectors - JSON structure on what to extract
     * @return
     */
    public String crawl(String url, Map<String, Object> selectors) {
        //TODO: create key-value pairs for url and the css selectors
        String payload = Utils.writeToJsonString(Map.of(
                "url", url,
                "schema", selectors // may be null if you just want markdown/title/etc.
        ));
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(base + "/crawl")) //This becomes http://localhost:8000/crawl/
                    .timeout(Duration.ofSeconds(40))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            //The scrape result
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() / 100 == 2){
                return res.body();
            }
            throw new RuntimeException("Crawler error: " + res.statusCode() + " -> " + res.body());
        } catch (Exception e) {
            throw new RuntimeException("Crawler call failed", e);
        }
    }
}
