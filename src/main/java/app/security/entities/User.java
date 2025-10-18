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
@Table(name = "users")
public class User implements ISecurityUser {
    private static final PasswordHasher passwordHasher = new PasswordHasher();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(unique = true, nullable = false)
    private String email;


    @Column(nullable = false)
    private String password;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_name", referencedColumnName = "name")
    )
    @Builder.Default
    @ToString.Exclude
    private Set<Role> roles = new HashSet<>();


    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @Builder.Default
    @ToString.Exclude
    private Set<Source> sources = new HashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private Set<CrawlLogger> crawlLoggers = new HashSet<>();

    //TODO: Prepersist so that it is actually at that very ms the user gets created
    private Instant createdAt;

    @PrePersist
    public void onCreate(){
        this.createdAt = Instant.now();
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
