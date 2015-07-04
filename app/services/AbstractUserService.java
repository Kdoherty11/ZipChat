package services;

import com.google.inject.ImplementedBy;
import daos.AbstractUserDao;
import services.impl.AbstractUserServiceImpl;

/**
 * Created by kdoherty on 7/3/15.
 */
@ImplementedBy(AbstractUserServiceImpl.class)
public interface AbstractUserService extends AbstractUserDao {

}
