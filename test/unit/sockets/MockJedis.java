package unit.sockets;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.*;

/**
 * Created by kdoherty on 8/4/15.
 */
public class MockJedis extends Jedis {

    private static Multimap<String, JedisPubSub> channelMap = HashMultimap.create();
    private static Map<String, Set<String>> setMap = new HashMap<>();

    public MockJedis() {
        super("localhost");
    }

    @Override
    public void subscribe(final JedisPubSub jedisPubSub, String... channels) {
        for (String channel : channels) {
            channelMap.put(channel, jedisPubSub);
        }
    }

    public Collection<JedisPubSub> getSubscribers(final String key) {
        return channelMap.get(key);
    }

    @Override
    public Long publish(final String channel, final String message) {
        channelMap.get(channel).forEach(jedisPubSub -> jedisPubSub.onMessage(channel, message));
        return 1L;
    }

    @Override
    public Boolean sismember(final String key, final String member) {
        Set<String> set = setMap.get(key);
        return set != null && set.contains(member);
    }

    @Override
    public Long sadd(final String key, final String... members) {
        Set<String> set = setMap.get(key);
        if (set == null) {
            Set<String> newSet = new HashSet<>();
            Collections.addAll(newSet, members);
            setMap.put(key, newSet);
        } else {
            Collections.addAll(set, members);
        }
        return 1L;
    }

    @Override
    public Long srem(final String key, final String... members) {
        Set<String> set = setMap.get(key);
        if (set == null) {
            throw new RuntimeException("No set found at key " + key);
        }

        for (String member : members) {
            set.remove(member);
        }

        return 1L;
    }

    @Override
    public Set<String> smembers(final String key) {
        return setMap.get(key);
    }

    public static void clean() {
        channelMap.clear();
        setMap.clear();
    }


}
