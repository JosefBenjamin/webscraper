package app.entities;

import app.enums.CrawlStatus;
import app.security.entities.User;
import jakarta.persistence.*;
import lombok.*;

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
@Table(name = "crawl_log")
/**
 * The purpose of this entity class:
 * A simple logging of status enum and error messages for each crawl attempt.
 * This entity class does not have functional relevance for the business logic
 */
public class CrawlLogger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(optional = false) //optional setting mean that relationship must exist, will not allow null
    @JoinColumn(name = "source_id", nullable = false)
    @ToString.Exclude
    private Source source;

    @ManyToOne
    @JoinColumn(name = "requested_by_user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @OneToMany(mappedBy = "crawlLogger", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @Builder.Default
    private Set<ScrapedData> items = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CrawlStatus status;

    private String error;

    //TODO: Add to collection helper methods (null safe)
    public void addItem(ScrapedData item){
        if(item == null){
            return;
        }
        items.add(item);
        item.setCrawlLogger(this);
    }

    //TODO: Remove from collection helper methods (null safe)
    public void removeItem(ScrapedData item){
        if (item == null) {
            return;
        }
        items.remove(item);
    }


}
