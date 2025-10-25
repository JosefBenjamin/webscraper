package app.dao;

import app.entities.ScrapedData;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.HashSet;
import java.util.Set;

public class ScrapedDataDAO implements ICRUD<ScrapedData>{

    private static ScrapedDataDAO instance;
    private static EntityManagerFactory emf;

    private ScrapedDataDAO(){

    }


    public static ScrapedDataDAO getInstance(EntityManagerFactory emf){
        if(emf == null){
            throw new IllegalArgumentException("EMF can't be null");
        }
        if(instance == null){
            instance = new ScrapedDataDAO();
            ScrapedDataDAO.emf = emf;
        }
        return instance;
    }

    //TODO: <-------CRUD OPERATIONS------->

    //TODO: Create

    @Override
    public ScrapedData persist(ScrapedData item){
        if (item == null) {
            return null;
        }

        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            try{
                em.persist(item);
                em.getTransaction().commit();
                return item;
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
    public ScrapedData findById(Long id){
        try(EntityManager em = emf.createEntityManager()){
            if(id == null){
                return null;
            }
            return em.find(ScrapedData.class, id);
        }
    }

    public boolean hashExists(Long sourceId, String hash){
        if(sourceId == null || hash == null){
            return false;
        }
        try(EntityManager em = emf.createEntityManager()){
            Long count = em.createQuery("SELECT COUNT(S)" +
                    "FROM ScrapedData s " +
                    "WHERE s.source.id = :sid AND " +
                    "s.hash = :hash", Long.class)
                    .setParameter("sid", sourceId)
                    .setParameter("hash", hash)
                    .getSingleResult();
            return (count > 0) && (count != null);
        }
    }

    @Override
    public Set<ScrapedData> retrieveAll(){
        try(EntityManager em = emf.createEntityManager()){
            return new HashSet<>(
                    em.createQuery("SELECT s FROM ScrapedData s ", ScrapedData.class)
                            .getResultList());
        }
    }


    //TODO: Update

    @Override
    public ScrapedData update(ScrapedData newItem){
        if (newItem == null){
            return null;
        }

        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            try{
                ScrapedData updatedItem = em.merge(newItem);
                em.getTransaction().commit();
                return updatedItem;
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
        if (id == null){
            return false;
        }

        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            try{
                ScrapedData foundItem = em.find(ScrapedData.class, id);
                if(foundItem == null){
                    em.getTransaction().rollback();
                    return false;
                }
                em.remove(foundItem);
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
