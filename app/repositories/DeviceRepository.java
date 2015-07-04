package repositories;

import com.google.inject.ImplementedBy;
import models.entities.Device;
import repositories.impl.DeviceRepositoryImpl;

import java.util.Optional;

/**
 * Created by kdoherty on 6/30/15.
 */
@ImplementedBy(DeviceRepositoryImpl.class)
public interface DeviceRepository extends GenericRepository<Device> {
    public Optional<Device> findById(long deviceId);
}
