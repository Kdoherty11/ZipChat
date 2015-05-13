package models.sockets.messages;

/**
 * Created by kevin on 5/12/15.
 */
public abstract class AbstractSocketMessage {

    final String type;

    public AbstractSocketMessage(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

}
