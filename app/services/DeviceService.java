package services;

import com.google.inject.Inject;
import models.entities.Device;
import repositories.DeviceRepository;

import java.util.Optional;

/**
 * Created by kdoherty on 7/1/15.
 */
public class DeviceService implements DeviceRepository {

    private DeviceRepository deviceRepository;

    @Inject
    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Override
    public Optional<Device> findById(long deviceId) {
        return deviceRepository.findById(deviceId);
    }
}
