package factories;

import com.google.common.collect.ImmutableMap;
import models.entities.Message;

import java.util.Map;

/**
 * Created by kevin on 6/23/15.
 */
public class MessageFactory extends GenericFactory<Message> {

    public MessageFactory() {
        super(Message.class);
    }

    @Override
    Map<String, Object> getDefaultProperties() {
        return new ImmutableMap.Builder<String, Object>()
                .put("message", faker.lorem().sentence())
                .build();
    }
}
