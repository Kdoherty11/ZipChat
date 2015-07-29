package daos;

import com.google.inject.ImplementedBy;
import daos.impl.DeviceDaoImpl;
import models.entities.Device;

import java.util.Optional;

/**
 * Created by kdoherty on 6/30/15.
 */
@ImplementedBy(DeviceDaoImpl.class)
public interface DeviceDao extends GenericDao<Device> {
    public Optional<Device> findById(long deviceId);
}
