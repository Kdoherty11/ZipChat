package repositories;

import com.google.inject.ImplementedBy;
import models.entities.Device;

import java.util.Optional;

/**
 * Created by kdoherty on 6/30/15.
 */
@ImplementedBy(DeviceRepository.class)
public interface DeviceRepository {

    public Optional<Device> findById(long deviceId);
}
