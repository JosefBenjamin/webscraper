package app.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "item")
/**
 * This purpose of this class entity:
 * Represents the output of a scrape attempt
 */
public class ScrapedData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "source_id", nullable = false)
    private Source source;

    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "crawl_log_id", nullable = false)
    private CrawlLogger crawlLogger;

    @Column(nullable = false)
    /**
     * @Param url
     * What: The web address that was crawled or where the specific data from.
     * Purpose: It tells us which webpage on a given site the data came from.
     */
    private String url;

    private Instant crawledAt;

    @Column(nullable = false)
    /**
     * @Param hash
     * What: Unique fingerprint of scraped content.
     * Purpose: To avoid duplicates mostly, but can also be used to tell if a webpage changed contents.
     */
    private String hash;

    @Column(columnDefinition = "JSONB") //Hibernates converts String to JSONB (a data type in Postgres)
    /**
     * @Param dataJson
     * What: The actual scraped data, stored as JSON text
     * Example: {"title": "Adjustable Bench", "price": "1499 DKK", "stock": "In stock"}
     * Purpose:keeps the scraped information flexible;
     * each Source can define different fields (price, title, rating, etc.).
     */
    private String dataJson;


    @PrePersist
    public void createdAt(){
        this.crawledAt = Instant.now();
    }


}
