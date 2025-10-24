package app.dao;

import app.entities.Source;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SourceDAO implements ICRUD<Source> {
    private static EntityManagerFactory emf;
    private static SourceDAO instance;


    private SourceDAO(){

    }


    public static SourceDAO getInstance(EntityManagerFactory emf){
        if (emf == null) {
            throw new IllegalArgumentException("EMF can't be null");
        }
        if(instance == null){
            instance = new SourceDAO();
            SourceDAO.emf  = emf;
        }
        return instance;
    }


    //TODO: <-------CRUD OPERATIONS------->

    //TODO: Create

    @Override
    public Source persist(Source newSource){
        if (newSource == null) {
            return null;
        }

        try(EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            try {
                em.persist(newSource);
                em.getTransaction().commit();
                return newSource;
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw new RuntimeException(e);
            }
        }
    }


    //TODO: Read

    @Override
    public Source findById(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            if (id == null) {
                return null;
            }
            return em.find(Source.class, id);
        }
    }


    @Override
    public Set<Source> retrieveAll(){
        try(EntityManager em = emf.createEntityManager()){
            return new HashSet<>(
                em.createQuery("SELECT s FROM Source s", Source.class)
                  .getResultList()
            );
        }
    }


    public boolean existsByOwnerAndName(String ownerUsername, String sourceName){
        if (ownerUsername == null || sourceName == null) {
            return false;
        }

        try(EntityManager em = emf.createEntityManager()){
            Long count = em.createQuery("SELECT COUNT(s) FROM Source s " +
                    "WHERE LOWER(s.user.username) = LOWER(:username) " +
                    "AND LOWER(s.name) = LOWER(:sourceName)", Long.class)
                    .setParameter("username", ownerUsername.trim())
                    .setParameter("sourceName", sourceName.trim())
                    .getSingleResult();

            return count != null && count > 0;
        }
    }


    public Set<Source> findAllSourcesByOwner(String ownerUsername){
        try(EntityManager em = emf.createEntityManager()){
            List<Source> foundSourcesByOwner = em.createQuery("SELECT s " +
                    "FROM Source s " +
                    "WHERE LOWER(s.user.username) = LOWER(:username) " +
                            "ORDER BY s.createdAt DESC", Source.class)
                    .setParameter("username", ownerUsername)
                    .getResultList();

            return new HashSet<>(foundSourcesByOwner);
        }
    }


    public Set<Source> findAllPublicSources(){
        try(EntityManager em = emf.createEntityManager()){
            List<Source> publicSources = em.createQuery(
                    "SELECT s " +
                    "FROM Source s " +
                    "WHERE s.publicReadable = true AND s.enabled = true " +
                    "ORDER BY s.createdAt DESC", Source.class)
                    .getResultList();
            return new HashSet<>(publicSources);
        }
    }


    //TODO: Update

    @Override
    public Source update(Source newSource){
        if (newSource == null) {
            return null;
        }

        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            try{
                Source updatedSource = em.merge(newSource);
                em.getTransaction().commit();
                return updatedSource;
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw new RuntimeException(e);
            }
        }
    }


    //TODO: Delete

    @Override
    public boolean delete(Long id){
        if (id == null) {
            return false;
        }

        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            try {
                Source foundSource = em.find(Source.class, id);
                if(foundSource == null){
                    em.getTransaction().rollback();
                    return false;
                }
                em.remove(foundSource);
                em.getTransaction().commit();
                return true;
            } catch (RuntimeException e) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                throw new RuntimeException(e);
            }
        }
    }



}
