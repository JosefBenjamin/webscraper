package app.security.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
/**
 * Purpose of this entity class:
 * Adding or modifying a user's API access privilege.
 */
public class Role {

    @Id
    @Column(nullable = false, length = 25)
    @EqualsAndHashCode.Include
    private String name;


    @ManyToMany(mappedBy = "roles")
    @ToString.Exclude
    @Builder.Default
    private Set<User> users = new HashSet<>();

    public Role(String role){
        this.name = role;
    }

}
