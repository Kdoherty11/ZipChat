package services;

import com.google.inject.ImplementedBy;
import play.libs.F;
import redis.clients.jedis.Jedis;
import services.impl.JedisServiceImpl;

/**
 * Created by kdoherty on 8/1/15.
 */
@ImplementedBy(JedisServiceImpl.class)
public interface JedisService {

    void useJedisResource(F.Callback<Jedis> callback);

}
