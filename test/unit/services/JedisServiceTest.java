package unit.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;
import services.JedisService;
import services.impl.JedisServiceImpl;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by kdoherty on 8/2/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class JedisServiceTest {

    private JedisService jedisService;

    @Mock
    private JedisPool jedisPool;

    @Before
    public void setUp() {
        jedisService = new JedisServiceImpl(jedisPool);
    }

    @Test
    public void useJedisResourceGetResourceThrowsJedisConnectionException() {
        doThrow(JedisConnectionException.class).when(jedisPool).getResource();

        try {
            jedisService.useJedisResource(jedis -> {
            });
            fail();
        } catch (JedisConnectionException e) {
            verify(jedisPool, never()).returnBrokenResource(any());
            verify(jedisPool, never()).returnResource(any());
        }

    }

    @Test
    public void useJedisResourceCallbackThrowsJedisConnectionException() {
        Jedis jedis = mock(Jedis.class);
        when(jedisPool.getResource()).thenReturn(jedis);
        try {
            jedisService.useJedisResource(j -> {
                throw new JedisConnectionException("msg");
            });
            fail();
        } catch (JedisConnectionException e) {
            verify(jedisPool).returnBrokenResource(refEq(jedis));
            verify(jedisPool, never()).returnResource(any());
        }
    }

    @Test
    public void useJedisResourceCallbackThrowsExceptionThatIsNotJedisConnectionException() {
        Jedis jedis = mock(Jedis.class);
        when(jedisPool.getResource()).thenReturn(jedis);
        RuntimeException e = new RuntimeException();
        try {
            jedisService.useJedisResource(j -> {
                throw e;
            });
            fail();
        } catch (RuntimeException ex) {
            verify(jedisPool, never()).returnBrokenResource(any());
            verify(jedisPool).returnResource(refEq(jedis));
        }
    }

    @Test
    public void useJedisResourceNoException() {
        Jedis jedis = mock(Jedis.class);
        when(jedisPool.getResource()).thenReturn(jedis);
        jedisService.useJedisResource(j -> assertSame(jedis, j));
        verify(jedisPool, never()).returnBrokenResource(any());
        verify(jedisPool).returnResource(refEq(jedis));
    }


}
