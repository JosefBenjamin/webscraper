package app.dao;

import app.entities.Source;

import java.util.Set;

public interface ICRUD <T>{


    T persist(T entity);

    T findById(Long id);

    Set<T> retrieveAll();

    T update(T entity);

    boolean delete(Long id);



}
