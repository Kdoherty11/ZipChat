package repositories;

import com.google.inject.ImplementedBy;
import models.entities.Request;
import repositories.impl.RequestRepositoryImpl;

import java.util.List;
import java.util.Optional;

/**
 * Created by kdoherty on 6/30/15.
 */
@ImplementedBy(RequestRepositoryImpl.class)
public interface RequestRepository {
    public Optional<Request> findById(long requestId);
    public List<Request> findPendingRequestsByReceiver(long receiverId);
    public String getStatus(long senderId, long receiverId);
    public Optional<Request> findBySenderAndReceiver(long senderId, long receiverId);
    public void save(Request request);
}
