package services.impl;

import com.google.common.base.Preconditions;
import daos.GenericDao;
import services.GenericService;

import java.util.Optional;

/**
 * Created by kdoherty on 7/3/15.
 */
public class GenericServiceImpl<T> implements GenericService<T> {

    private GenericDao<T> tDao;

    public GenericServiceImpl(GenericDao<T> tDao) {
        this.tDao = Preconditions.checkNotNull(tDao);
    }

    @Override
    public void save(Object entity) {
        tDao.save(entity);
    }

    @Override
    public void merge(Object entity) {
        tDao.merge(entity);
    }

    @Override
    public Optional<T> findById(long id) {
        return tDao.findById(id);
    }

    @Override
    public void remove(Object entity) {
        tDao.remove(entity);
    }

}
