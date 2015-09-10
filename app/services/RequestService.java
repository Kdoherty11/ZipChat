package services;

import com.google.inject.ImplementedBy;
import daos.RequestDao;
import models.Request;
import services.impl.RequestServiceImpl;

/**
 * Created by kdoherty on 7/1/15.
 */
@ImplementedBy(RequestServiceImpl.class)
public interface RequestService extends RequestDao {

    String getStatus(long potentialSenderId, long potentialReceiverId);
    void handleResponse(Request request, Request.Status status);
}
