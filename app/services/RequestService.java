package services;

import com.google.inject.ImplementedBy;
import models.entities.Request;
import repositories.RequestRepository;
import services.impl.RequestServiceImpl;

/**
 * Created by kdoherty on 7/1/15.
 */
@ImplementedBy(RequestServiceImpl.class)
public interface RequestService extends RequestRepository {

    void handleResponse(Request request, Request.Status status);

}
