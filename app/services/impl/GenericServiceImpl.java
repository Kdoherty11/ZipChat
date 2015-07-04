package services.impl;

import com.google.common.base.Preconditions;
import repositories.GenericRepository;

import java.util.Optional;

/**
 * Created by kdoherty on 7/3/15.
 */
public class GenericServiceImpl<T> implements GenericRepository<T> {

    private GenericRepository<T> repository;

    public GenericServiceImpl(GenericRepository<T> repository) {
        this.repository = Preconditions.checkNotNull(repository);
    }

    @Override
    public void save(Object entity) {
        repository.save(entity);
    }

    @Override
    public Optional<T> findById(long id) {
        return repository.findById(id);
    }

    @Override
    public void remove(Object entity) {
        repository.remove(entity);
    }


}
