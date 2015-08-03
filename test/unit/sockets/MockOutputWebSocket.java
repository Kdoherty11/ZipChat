package unit.sockets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import play.mvc.WebSocket;

import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@ThreadSafe
public class MockOutputWebSocket {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    protected final BlockingQueue<JsonNode> messageQueue = new LinkedBlockingQueue<>();

    protected final WebSocket.Out<JsonNode> outputSocket = new WebSocket.Out<JsonNode>() {
        @Override
        public void write(JsonNode frame) {
            messageQueue.add(frame);
        }

        @Override
        public void close() {
            try {
                messageQueue.add(objectMapper.readTree("{\"closed\": true}"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    };

    public BlockingQueue<JsonNode> getMessageQueue() {
        return messageQueue;
    }

    public WebSocket.Out<JsonNode> getOutputSocket() {
        return outputSocket;
    }
}
