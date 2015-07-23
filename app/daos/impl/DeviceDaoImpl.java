package daos.impl;

import models.entities.Device;
import daos.DeviceDao;

/**
 * Created by kdoherty on 6/30/15.
 */
public class DeviceDaoImpl extends GenericDaoImpl<Device> implements DeviceDao {

    public DeviceDaoImpl() {
        super(Device.class);
    }
}
