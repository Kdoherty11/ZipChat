package services.impl;

import com.google.inject.Inject;
import play.libs.F;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;
import services.JedisService;

/**
 * Created by kdoherty on 8/1/15.
 */
public class JedisServiceImpl implements JedisService {

    private final JedisPool jedisPool;

    @Inject
    public JedisServiceImpl(final JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public void useJedisResource(F.Callback<Jedis> callback) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            callback.invoke(jedis);
        } catch (JedisConnectionException e) {
            if (jedis != null) {
                jedisPool.returnBrokenResource(jedis);
                jedis = null;
            }
            throw new RuntimeException(e);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        } finally {
            if (jedis != null) {
                jedisPool.returnResource(jedis);
            }
        }
    }
}
