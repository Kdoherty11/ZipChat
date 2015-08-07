package services;

import com.google.inject.ImplementedBy;
import services.impl.KeepAliveServiceImpl;

/**
 * Created by kdoherty on 8/7/15.
 */
@ImplementedBy(KeepAliveServiceImpl.class)
public interface KeepAliveService {

    // Any impl should use these values
    long ID = -10;
    String MSG = "Beat";

    void start(long roomId);
    void stop(long roomId);
}
