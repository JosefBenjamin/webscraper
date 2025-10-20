package app.dao;

import java.util.Set;

public interface ICRUD <T>{


    T persist(T entity);

    T findById(Long id);

    Set<T> retrieveAll();

    T update(T entity);

    boolean delete(Long id);






}
