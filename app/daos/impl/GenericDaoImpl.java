package daos.impl;

import play.db.jpa.JPA;
import daos.GenericDao;

import java.util.Optional;

/**
 * Created by kdoherty on 7/3/15.
 */
public class GenericDaoImpl<T> implements GenericDao<T> {

    private Class<T> entityClazz;

    public GenericDaoImpl(final Class<T> entityClazz) {
        this.entityClazz = entityClazz;
    }

    @Override
    public void save(Object object) {
        JPA.em().persist(object);
    }

    @Override
    public Optional<T> findById(long id) {
        T entity = JPA.em().find(entityClazz, id);
        return Optional.ofNullable(entity);
    }

    @Override
    public void remove(Object entity) {
        JPA.em().remove(entity);
    }
}
