package services;

import com.google.inject.ImplementedBy;
import daos.DeviceDao;
import services.impl.DeviceServiceImpl;

/**
 * Created by kdoherty on 7/1/15.
 */
@ImplementedBy(DeviceServiceImpl.class)
public interface DeviceService extends DeviceDao {

}
