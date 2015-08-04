package unit.sockets;

import play.libs.F;
import redis.clients.jedis.Jedis;
import services.JedisService;

/**
 * Created by kdoherty on 8/4/15.
 */
public class MockJedisService implements JedisService {

    private Jedis jedis;

    public void setJedis(Jedis jedis) {
        this.jedis = jedis;
    }

    public MockJedisService(Jedis jedis) {
        this.jedis = jedis;
    }

    public MockJedisService() {
        this(new MockJedis());
    }

    @Override
    public void useJedisResource(F.Callback<Jedis> callback) {
        try {
            callback.invoke(jedis);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
