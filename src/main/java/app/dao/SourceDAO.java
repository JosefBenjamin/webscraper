package app.dao;

import app.entities.Source;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.HashSet;
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
        try(EntityManager em = emf.createEntityManager()){
            if(newSource == null){
                return newSource;
            }
            em.getTransaction().begin();
            try{

                em.persist(newSource);
                em.getTransaction().commit();
                return newSource;
            } catch (RuntimeException e) {
                em.getTransaction().rollback();
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


    //TODO: Update

    @Override
    public Source update(Source newSource){
        try(EntityManager em = emf.createEntityManager()){
            if(newSource == null){
                return newSource;
            }
            em.getTransaction().begin();
            try{
                Source updatedSource = em.merge(newSource);
                em.getTransaction().commit();
                return updatedSource;
            } catch (RuntimeException e) {
                em.getTransaction().rollback();
                throw new RuntimeException(e);
            }
        }
    }


    //TODO: Delete

    @Override
    public boolean delete(Long id){
        try(EntityManager em = emf.createEntityManager()){
            if(id == null){
                return false;
            }
            em.getTransaction().begin();
            try {
                Source foundSource = em.find(Source.class, id);
                if(foundSource == null){
                    return false;
                }
                em.remove(foundSource);
                em.getTransaction().commit();
                return true;
            } catch (RuntimeException e) {
                em.getTransaction().rollback();
                throw new RuntimeException(e);
            }
        }
    }



}
