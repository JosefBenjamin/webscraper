package app.security.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@ToString(exclude = "users")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Builder
public class Role {

    @Id
    @Column(nullable = false, length = 20)
    private String name;


    @ManyToMany(mappedBy = "roles")
    private Set<User> users = new HashSet<>();

    public Role(String role){
        this.name = role;
    }

}
