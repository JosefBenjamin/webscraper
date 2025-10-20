package app.security.entities;

import app.entities.CrawlLogger;
import app.entities.Source;
import app.security.hashing.PasswordHasher;
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
@ToString
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "users")
/**
 * The purpose of this entity class:
 * To secure API-endpoints from unauthorized access from bots and hackers.
 * To validate access rights given users privileges (called Role)
 */
public class User implements ISecurityUser {
    private static final PasswordHasher passwordHasher = new PasswordHasher();

    @Id
    @EqualsAndHashCode.Include
    private String username;

    @Column(unique = true, nullable = false)
    @EqualsAndHashCode.Include
    private String email;


    @Column(nullable = false)
    private String password;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "username"),
            inverseJoinColumns = @JoinColumn(name = "role_name", referencedColumnName = "name")
    )
    @Builder.Default
    @ToString.Exclude
    private Set<Role> roles = new HashSet<>();


    @OneToMany(mappedBy = "user", orphanRemoval = true, cascade = CascadeType.MERGE)
    @Builder.Default
    @ToString.Exclude
    private Set<Source> sources = new HashSet<>();

    @OneToMany(mappedBy = "user", orphanRemoval = true, cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private Set<CrawlLogger> crawlLoggers = new HashSet<>();

    //TODO: Prepersist so that it is actually at that very ms the user gets created
    private Instant createdAt;

    @PrePersist
    public void onCreate(){
        this.createdAt = Instant.now();
    }

    //TODO: Add to collection helper methods (null safe)
    public void addCrawlLogger(CrawlLogger crawlLogger){
        if(crawlLogger == null){
            return;
        }
        crawlLoggers.add(crawlLogger);
        crawlLogger.setUser(this);
    }

    public void addSource(Source source){
        if(source == null){
            return;
        }
        sources.add(source);
        source.setUser(this);
    }

    //TODO: Remove from collection helper methods (null safe)
    public void removeCrawlLogger(CrawlLogger crawlLogger){
        if(crawlLogger == null){
            return;
        }
        crawlLoggers.remove(crawlLogger);
        crawlLogger.setUser(null); // in-memory unlink; JPA will DELETE the row due to orphanRemoval
    }

    public void removeSource(Source source){
        if(source == null){
            return;
        }
        sources.remove(source);
        source.setUser(null); // in-memory unlink; JPA will DELETE the row due to orphanRemoval
    }



    public Set<String> getRolesAsStrings() {
        if (roles.isEmpty()) {
            return null;
        }
        Set<String> rolesAsStrings = new HashSet<>();
        roles.forEach((r) -> rolesAsStrings.add(r.getName()));
        return rolesAsStrings;
    }

    //TODO: Constructors
    public User(String email, String userPass) {
        this.email = email;
        this.password = passwordHasher.hashPassFirstTime(userPass);
    }

    public User(String email, Set<Role> roleEntityList) {
        this.email = email;
        this.roles = roleEntityList;
    }


    @Override
    public boolean verifyPass(String plainPassword) {
        return passwordHasher.checkPw(plainPassword, this.password);
    }

    @Override
    public void addRole(Role role) {
        if (role == null) {
            return;
        }
        roles.add(role);
        role.getUsers().add(this);
    }

    public void removeRole(String userRole) {
        roles.stream()
                .filter((r) ->
                {
                    return r.getName().equalsIgnoreCase(userRole);
                })
                .findFirst()
                .ifPresent(role -> {
                    roles.remove(role);
                    role.getUsers().remove(this);
                });
    }

}
