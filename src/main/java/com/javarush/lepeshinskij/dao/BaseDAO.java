package com.javarush.lepeshinskij.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public interface BaseDAO<T, ID extends Serializable> {
    
    T save(T entity);
    
    T update(T entity);
    
    T saveOrUpdate(T entity);
    
    Optional<T> findById(ID id);
    
    T getById(ID id);
    
    List<T> findAll();
    
    List<T> findAll(int offset, int limit);
    
    long count();
    
    boolean existsById(ID id);
    
    boolean deleteById(ID id);
    
    void delete(T entity);
    
    int deleteAll();
    
    void clear();
    
    void flush();
}
