package daos;

import com.google.inject.ImplementedBy;
import daos.impl.DeviceDaoImpl;
import models.Device;

/**
 * Created by kdoherty on 6/30/15.
 */
@ImplementedBy(DeviceDaoImpl.class)
public interface DeviceDao extends GenericDao<Device> {
}
