package websocket.messages;

public class Error extends ServerMessage{
    public Error(ServerMessageType type, String errorMessage) {
        super(type);
    }
}
