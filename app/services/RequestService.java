package services;

import com.google.inject.Inject;
import models.entities.Request;
import repositories.RequestRepository;

import java.util.List;
import java.util.Optional;

/**
 * Created by kdoherty on 7/1/15.
 */
public class RequestService implements RequestRepository {

    private final RequestRepository requestRepository;

    @Inject
    public RequestService(final RequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    @Override
    public Optional<Request> findById(long requestId) {
        return requestRepository.findById(requestId);
    }

    @Override
    public List<Request> findPendingRequestsByReceiver(long receiverId) {
        return requestRepository.findPendingRequestsByReceiver(receiverId);
    }

    @Override
    public String getStatus(long senderId, long receiverId) {
        return requestRepository.getStatus(senderId, receiverId);
    }

    @Override
    public Optional<Request> findBySenderAndReceiver(long senderId, long receiverId) {
        return requestRepository.findBySenderAndReceiver(senderId, receiverId);
    }

    @Override
    public void save(Request request) {
        requestRepository.save(request);
    }
}
