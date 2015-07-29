package daos;

import com.google.inject.ImplementedBy;
import daos.impl.RequestDaoImpl;
import models.entities.Request;

import java.util.List;
import java.util.Optional;

/**
 * Created by kdoherty on 6/30/15.
 */
@ImplementedBy(RequestDaoImpl.class)
public interface RequestDao extends GenericDao<Request> {
    public List<Request> findPendingRequestsByReceiver(long receiverId);
    public Optional<Request> findBySenderAndReceiver(long senderId, long receiverId);
}
