package repositories.impl;

import models.entities.Device;
import repositories.DeviceRepository;

/**
 * Created by kdoherty on 6/30/15.
 */
public class DeviceRepositoryImpl extends GenericRepositoryImpl<Device> implements DeviceRepository {

    public DeviceRepositoryImpl() {
        super(Device.class);
    }
}
