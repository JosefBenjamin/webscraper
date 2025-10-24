package app.dao;

import app.entities.CrawlLogger;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.HashSet;
import java.util.Set;

public class CrawlLoggerDAO implements ICRUD<CrawlLogger>{

    private static EntityManagerFactory emf;
    private static CrawlLoggerDAO instance;


    private CrawlLoggerDAO(){

    }

    public static CrawlLoggerDAO getInstance(EntityManagerFactory emf){
        if(emf == null){
            throw new IllegalArgumentException("EMF can't be null");
        }
        if (instance == null) {
            instance = new CrawlLoggerDAO();
            CrawlLoggerDAO.emf  = emf;
        }
        return instance;
    }


    //TODO: <-------CRUD OPERATIONS------->

    //TODO: Create

    @Override
    public CrawlLogger persist(CrawlLogger newCrawlLogger){
        if(newCrawlLogger == null){
            return null;
        }

        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            try{
                em.persist(newCrawlLogger);
                em.getTransaction().commit();
                return newCrawlLogger;
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
    public CrawlLogger findById(Long id){
        if(id == null || id <= 0){
            throw new IllegalArgumentException("id can't be null or negative integer");
        }

        try(EntityManager em = emf.createEntityManager()){
            return em.find(CrawlLogger.class, id);
        }
    }


    @Override
    public Set<CrawlLogger> retrieveAll(){
        try(EntityManager em = emf.createEntityManager()){
            return new HashSet<>(
                    em.createQuery("SELECT c FROM CrawlLogger c ", CrawlLogger.class)
                            .getResultList());
        }
    }



    //TODO: Update

    @Override
    public CrawlLogger update(CrawlLogger newCrawlLogger){
        if(newCrawlLogger == null){
            throw new IllegalArgumentException("CrawlLogger cannot be null");
        }

        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            try {
                CrawlLogger updatedCrawlLogger = em.merge(newCrawlLogger);
                em.getTransaction().commit();
                return updatedCrawlLogger;
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
        if(id == null || id <= 0){
            return false;
        }

        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            try{
                CrawlLogger foundLogger = em.find(CrawlLogger.class, id);
                if (foundLogger == null) {
                    em.getTransaction().rollback();
                    return false;
                }
                em.remove(foundLogger);
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
