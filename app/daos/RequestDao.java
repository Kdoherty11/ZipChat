package daos;

import com.google.inject.ImplementedBy;
import models.entities.Request;
import daos.impl.RequestDaoImpl;

import java.util.List;
import java.util.Optional;

/**
 * Created by kdoherty on 6/30/15.
 */
@ImplementedBy(RequestDaoImpl.class)
public interface RequestDao extends GenericDao<Request> {
    public List<Request> findPendingRequestsByReceiver(long receiverId);
    public String getStatus(long senderId, long receiverId);
    public Optional<Request> findBySenderAndReceiver(long senderId, long receiverId);
}
