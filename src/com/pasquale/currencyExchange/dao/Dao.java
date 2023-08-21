package com.pasquale.currencyExchange.dao;

import com.pasquale.currencyExchange.exception.DaoException;

import java.util.List;
import java.util.Optional;

public interface Dao<K, T> {

    List<T> findAll() throws DaoException;

    Optional<T> findById(K id) throws DaoException;

    T update(T entity) throws DaoException;

    T save(T entity) throws DaoException;

}
