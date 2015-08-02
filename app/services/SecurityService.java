package services;

import com.google.inject.ImplementedBy;
import models.PrivateRoom;
import services.impl.SecurityServiceImpl;

import java.util.Optional;

/**
 * Created by kdoherty on 7/8/15.
 */
@ImplementedBy(SecurityServiceImpl.class)
public interface SecurityService {

    String generateAuthToken(long userId);
    Optional<Long> getUserId(String jwt);
    boolean isUnauthorized(long userId);
    boolean isUnauthorized(PrivateRoom privateRoom);
    boolean isUnauthorized(String authToken, long userId);
    boolean isUnauthorized(String authToken, PrivateRoom privateRoom);
    long getTokenUserId();
}
