package app.entities;

import app.security.entities.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
/**
 * Purpose of this entity class:
 * Interacts with Crawl4AI as the config for the web scraper
 * https://github.com/unclecode/crawl4ai
 */
public class Source {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    @ToString.Exclude
    private User user;

    @OneToMany(mappedBy = "source", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private Set<CrawlLogger> crawlLoggers = new HashSet<>();

    @OneToMany(mappedBy = "source", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private Set<ScrapedData> items = new HashSet<>();


    @Column(nullable = false)
    /**
     * @Param baseUrl
     * What: The main or starting web address the crawler begins from.
     * Purpose: Defines the entry point for the crawl — the URL you hand to Crawl4AI’s arun() method.
     * That’s the page whose HTML is fetched and parsed.
     *
     */
    private String baseUrl;

    @Column(nullable = false)
    /**
     * @Param allowedPathPattern
     * What: A regular expression (regex) defining which URLs within that domain are allowed to be crawled.
     * Example: "^/products/.*" — allows only pages under /products/
     * Purpose: If the crawler finds links on the page,
     * it only follows or processes those whose path matches this pattern.
     * This prevents it from accidentally scraping unrelated sections or external sites.
     */
    private String allowedPathPattern;

    @Column(nullable = false)
    /**
     *
     * @Reference https://docs.crawl4ai.com/extraction/no-llm-strategies
     * @Param selectorsJson
     * What: A JSON configuration describing what to extract from a web page and how.
     * Example: {
     *   "list": ".product-card",
     *   "fields": {
     *     "title": { "selector": ".name" },
     *     "price": { "selector": ".price" },
     *     "link":  { "selector": "a", "attr": "href" }
     *   }
     * }
     * Purpose: tells the crawler which CSS or XPath selectors to use when scraping.
     * Using:  CSS-Based Extraction — Fast schema-based data extraction using XPath and CSS selectors.
     */
    private String selectorsJson;

    @Column(nullable = false)
    private boolean publicReadable;

    private boolean enabled;

    private Instant createdAt;

    private Instant updatedAt;


    @PrePersist //PrePersists gives us the most accurate time for when the source was persisted
    public void onCreate(){
        this.createdAt = Instant.now();
    }

    @PreUpdate //Same as PrePersist, just for updates instead.
    public void onUpdate(){
        this.updatedAt = Instant.now();
    }

    //TODO: Add to collection helper methods (null safe)
    public void addCrawlLogger(CrawlLogger crawlLogger){
        if(crawlLogger == null){
            return;
        }
        crawlLoggers.add(crawlLogger);
        crawlLogger.setSource(this);
    }

    public void addItems(ScrapedData item){
        if(item == null){
            return;
        }
        items.add(item);
        item.setSource(this);
    }

    //TODO: Remove from collection helper methods (null safe)
    public void removeCrawlLogger(CrawlLogger crawlLogger){
        if(crawlLogger == null){
            return;
        }
        crawlLoggers.remove(crawlLogger);
    }

    public void removeItem(ScrapedData item){
        if(item == null){
            return;
        }
        items.remove(item);
    }



}
