package app.security.daos;

import app.exceptions.ApiException;
import app.security.entities.Role;
import app.security.entities.User;
import app.security.exceptions.ValidationException;
import app.security.hashing.PasswordHasher;
import dk.bugelhartmann.UserDTO;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;

import java.util.stream.Collectors;

public class SecurityDAO implements ISecurityDAO{

    private final EntityManagerFactory emf;
    private static PasswordHasher passwordHasher = new PasswordHasher();

    public SecurityDAO(EntityManagerFactory emf){
        this.emf = emf;
    }

    private EntityManager getEntityManager(){
        return this.emf.createEntityManager();
    }


    @Override
    public UserDTO getVerifiedUser(String username, String password) throws ValidationException {
        try (EntityManager em = getEntityManager()) {
            User user = em.find(User.class, username);
            if (user == null) {
                throw new EntityNotFoundException("No user found with username: " + username); //RuntimeException
            }
            user.getRoles().size(); // force roles to be fetched from db
            if (!user.verifyPass(password)) {
                throw new ValidationException("Wrong password");
            }
            UserDTO userDTO = new UserDTO(user.getEmail(), user.getRoles().stream()
                    .map((r) -> {
                        return r.getName();
                    }).collect(Collectors.toSet()));
            return userDTO;
        }
    }

    @Override
    public User createUser(String username, String password) {
        try (EntityManager em = getEntityManager()) {
           try {
               User userEntity = em.find(User.class, username);
               if (userEntity != null) {
                   throw new EntityExistsException("User with username: " + username + " already exists");
               }
               userEntity = new User(username, password);
               em.getTransaction().begin();
               Role userRole = em.find(Role.class, "user");
               if (userRole == null) {
                   userRole = new Role("user");
                   em.persist(userRole);
               }
               userEntity.addRole(userRole);
               em.persist(userEntity);
               em.getTransaction().commit();
               return userEntity;
           } catch (ApiException e) {
               if(em.getTransaction().isActive())
               em.getTransaction().rollback();
               throw e;
           }
        }catch (Exception e){
            e.printStackTrace();
            throw new ApiException(400, e.getMessage());
        }
    }

    @Override
    public User addRole(UserDTO userDTO, String newRole) {
        try (EntityManager em = getEntityManager()) {
            if (newRole == null || newRole.isBlank()) {
                throw new IllegalArgumentException("Role name is required");
            }

            String normalizedRole = newRole.trim().toLowerCase();

            User user = em.find(User.class, userDTO.getUsername());
            if (user == null)
                throw new EntityNotFoundException("No user found with username: " + userDTO.getUsername());
            em.getTransaction().begin();
            Role role = em.find(Role.class, normalizedRole);
            if (role == null) {
                role = new Role(normalizedRole);
                em.persist(role);
            }
            user.addRole(role);
            //em.merge(user);
            em.getTransaction().commit();
            return user;
        }
    }

}
