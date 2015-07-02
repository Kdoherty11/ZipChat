package repositories.impl;

import models.entities.Device;
import repositories.DeviceRepository;
import utils.DbUtils;

import java.util.Optional;

/**
 * Created by kdoherty on 6/30/15.
 */
public class DeviceRepositoryImpl implements DeviceRepository {

    @Override
    public Optional<Device> findById(long deviceId) {
        return DbUtils.findEntityById(Device.class, deviceId);
    }
}
