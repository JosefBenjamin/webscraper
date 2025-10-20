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

    private static EntityManagerFactory emf;
    private static SecurityDAO instance;
    private static PasswordHasher passwordHasher = new PasswordHasher();

    private SecurityDAO(){

    }

    public static SecurityDAO getInstance(EntityManagerFactory emf){
        if(emf == null){
            throw new IllegalArgumentException("EMF can't be null");
        }
        if(instance == null){
            instance = new SecurityDAO();
            SecurityDAO.emf = emf;
        }
        return instance;
    }


    @Override
    public UserDTO getVerifiedUser(String username, String password) throws ValidationException {
        try (EntityManager em = emf.createEntityManager()) {
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
        try (EntityManager em = emf.createEntityManager()) {
            String standardizedUsername = username.toLowerCase().trim();
           try {
               User userEntity = em.find(User.class, standardizedUsername);
               if (userEntity != null) {
                   throw new EntityExistsException("User with username: " + username + " already exists");
               }
               userEntity = new User(standardizedUsername, password);
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
        try (EntityManager em = emf.createEntityManager()) {
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

    public User updateUser(User entity) {
        return null;
    }

    public boolean deleteUser(Long id) {
        return false;
    }

}
