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
@EqualsAndHashCode
@Table(name = "crawl_log")
public class CrawlLogger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "source_id", nullable = false)
    @ToString.Exclude
    private Source source;

    @ManyToOne
    @JoinColumn(name = "requestedBy_user", nullable = false)
    @ToString.Exclude
    private User user;

    @OneToMany
    @ToString.Exclude
    private Set<ScrapedData> scrapedDataSet = new HashSet<>();

    private CrawlStatus status;

    private String error;

}
