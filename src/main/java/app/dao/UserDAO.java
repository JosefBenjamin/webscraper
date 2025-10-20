package app.dao;

import app.security.entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.HashSet;
import java.util.Set;

public class UserDAO {
    private static EntityManagerFactory emf;
    private static UserDAO instance;

    private UserDAO(){

    }

    public static UserDAO getInstance(EntityManagerFactory emf){
        if(emf == null){
            throw new IllegalArgumentException("EMF can't be null");
        }
        if(instance == null){
            instance = new UserDAO();
            UserDAO.emf = emf;
        }
        return instance;
    }

    public User findByUsername(String username) {
        try(EntityManager em = emf.createEntityManager()){
            if(username == null){
                return null;
            }
            String standardizedUsername = username.toLowerCase().trim();
            return em.find(User.class, standardizedUsername);
            }
        }

    public Set<User> retrieveAll() {
    try(EntityManager em = emf.createEntityManager()){
        return new HashSet<>(
                em.createQuery("SELECT u FROM User u ", User.class)
                        .getResultList());
        }
    }


}
