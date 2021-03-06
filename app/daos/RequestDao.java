package daos;

import com.google.inject.ImplementedBy;
import daos.impl.RequestDaoImpl;
import models.Request;

import java.util.List;
import java.util.Optional;

/**
 * Created by kdoherty on 6/30/15.
 */
@ImplementedBy(RequestDaoImpl.class)
public interface RequestDao extends GenericDao<Request> {
    List<Request> findPendingRequestsByReceiver(long receiverId);
    Optional<Request> findByUsers(long userId1, long userId2);
}
