package services.impl;

import com.google.inject.Inject;
import models.entities.Device;
import repositories.DeviceRepository;
import services.DeviceService;

/**
 * Created by kdoherty on 7/3/15.
 */
public class DeviceServiceImpl extends GenericServiceImpl<Device> implements DeviceService {

    @Inject
    public DeviceServiceImpl(DeviceRepository deviceRepository) {
        super(deviceRepository);
    }
}
