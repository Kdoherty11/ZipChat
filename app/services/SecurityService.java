package services;

import com.google.inject.ImplementedBy;
import models.entities.PrivateRoom;
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
    long getTokenUserId();
}
