package services;

import com.google.inject.ImplementedBy;
import models.entities.Request;
import daos.RequestDao;
import services.impl.RequestServiceImpl;

/**
 * Created by kdoherty on 7/1/15.
 */
@ImplementedBy(RequestServiceImpl.class)
public interface RequestService extends RequestDao {

    String getStatus(long senderId, long receiverId);
    void handleResponse(Request request, Request.Status status);
}