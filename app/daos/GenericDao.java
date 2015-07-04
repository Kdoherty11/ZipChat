package daos;

import java.util.Optional;

/**
 * Created by kdoherty on 7/3/15.
 */
public interface GenericDao<T> {
    void save(Object entity);
    Optional<T> findById(long id);
    void remove(Object entity);

}
