package services;

import com.google.inject.ImplementedBy;
import daos.GenericDao;
import services.impl.GenericServiceImpl;

/**
 * Created by kdoherty on 7/8/15.
 */
@ImplementedBy(GenericServiceImpl.class)
public interface GenericService<T> extends GenericDao<T> {


}
