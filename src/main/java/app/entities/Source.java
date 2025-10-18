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
@EqualsAndHashCode
public class Source {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    @ToString.Exclude
    private User user;

    @OneToMany(mappedBy = "source", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private Set<CrawlLogger> crawlLoggers = new HashSet<>();


    @Column(nullable = false)
    private String baseUrl;

    @Column(nullable = false)
    private String allowedPathPattern;

    @Column(nullable = false)
    private String selectorsJson;

    @Column(nullable = false)
    private boolean publicReadable;

    private boolean enabled;

    private Instant createdAt;

    private Instant updatedAt;


    @PrePersist
    public void onCreate(){
        this.createdAt = Instant.now();
    }

    @PrePersist
    public void onUpdate(){
        this.updatedAt = Instant.now();
    }



}
