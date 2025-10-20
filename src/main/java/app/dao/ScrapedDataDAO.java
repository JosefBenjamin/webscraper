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


    public ScrapedDataDAO getInstance(EntityManagerFactory emf){
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
        try(EntityManager em = emf.createEntityManager()){
            if(item == null){
                return item;
            }
            em.getTransaction().begin();
            try{
                em.persist(item);
                em.getTransaction().commit();
                return item;
            } catch (RuntimeException e) {
                em.getTransaction().rollback();
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
        try(EntityManager em = emf.createEntityManager()){
            if(newItem == null){
                return newItem;
            }
            em.getTransaction().begin();
            try{
            ScrapedData updatedItem = em.merge(newItem);
            em.getTransaction().commit();
            return updatedItem;
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
            try{
                ScrapedData foundItem = em.find(ScrapedData.class, id);
                if(foundItem == null){
                    return false;
                }
                em.remove(foundItem);
                em.getTransaction().commit();
                return true;
            } catch (RuntimeException e) {
                em.getTransaction().rollback();
                throw new RuntimeException(e);
            }
        }
    }


}
